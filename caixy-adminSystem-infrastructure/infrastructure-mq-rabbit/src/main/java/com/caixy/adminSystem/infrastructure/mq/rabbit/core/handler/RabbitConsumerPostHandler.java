package com.caixy.adminSystem.infrastructure.mq.rabbit.core.handler;


import com.caixy.adminSystem.common.base.exception.BusinessException;
import com.caixy.adminSystem.common.base.utils.JsonUtils;
import com.caixy.adminSystem.infrastructure.mq.rabbit.core.annotation.RabbitConsumer;
import com.caixy.adminSystem.infrastructure.mq.rabbit.core.consumer.RabbitMQMessageHandler;
import com.caixy.adminSystem.infrastructure.mq.rabbit.core.enums.RabbitMQQueueEnum;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义注解处理器，用于扫描标注了 @CustomRabbitListener 的消费者 Bean，并创建消息监听容器。
 *
 * @Author CAIXYPROMISE
 * @since 2025/1/13 1:36
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitConsumerPostHandler implements BeanPostProcessor, ApplicationContextAware
{
    private final MessageQueueIdempotentHandler idempotentHandler;
    @Value("${rabbitmq.config.retryOnDeadLetter}")
    private Integer retryOnDeadLetter;

    /** 缓存 <beanName, listener> 供 Registrar 使用 */
    private final Map<String, ChannelAwareMessageListener> listenerCache = new ConcurrentHashMap<>();

    public ChannelAwareMessageListener getListener(String beanName) {
        return listenerCache.get(beanName);
    }
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean,@NonNull String beanName) {
        Class<?> clazz = AopUtils.getTargetClass(bean);
        RabbitConsumer ann = clazz.getAnnotation(RabbitConsumer.class);
        if (ann == null)
            return bean;

        RabbitMQQueueEnum qEnum = ann.value();
        listenerCache.put(beanName, buildListener(bean, qEnum));   // ★ 缓存
        return bean;
    }

    /** 核心：把原来 container.setMessageListener(...) 里的代码抽为一个对象返回 */
    private ChannelAwareMessageListener buildListener(Object consumerBean, RabbitMQQueueEnum qEnum) {
        Class<?> dtoClass = resolveMessageDtoClass(consumerBean);
        String queueName  = qEnum.getQueueName();
        String dlq        = qEnum.getDeadLetterQueue();

        return (message, channel) -> handleMessageFlow(
                message, channel, consumerBean, dtoClass, queueName, dlq);
    }


    /**
     * 核心处理逻辑：从消息中解析数据、做幂等检查、区分死信与正常消息、捕获异常并做相应处理。
     */
    private void handleMessageFlow(Message message,
                                   Channel channel,
                                   Object consumerBean,
                                   Class<?> messageDtoClass,
                                   String queueName,
                                   String deadLetterQueue) throws Exception
    {

        boolean isDeadLetter = isDeadLetterMessage(message);
        String messageId = getOrGenerateMessageId(message);
        Object messageDto = null;

        try
        {
            // 幂等检查: 是否已处理过或正在处理中
            if (!checkAndMarkInProcess(messageId, channel, queueName))
            {
                return; // 如果返回 false，说明已 ack，此次就不再处理
            }

            // 反序列化消息体
            messageDto = deserializeMessage(message, messageDtoClass);

            // 交由业务处理
            invokeBusinessHandler(consumerBean, messageDto, channel, message, messageId, isDeadLetter, deadLetterQueue);

            // 成功：标记完成并 ack
            completeAndAck(messageId, channel, message);

        }
        // 业务异常：直接 ack，不再重试
        catch (BusinessException be)
        {
            handleBusinessException(be, messageId, channel, message);
        }
        // 系统异常或其他未知异常：进行重试或死信等逻辑
        catch (Exception e)
        {
            handleGeneralException(e, messageId, messageDto, channel, message, isDeadLetter, queueName,
                    deadLetterQueue);
        }
    }

    // =============================  1) 幂等与反序列化相关  =============================

    /**
     * 幂等检查：判断消息是否已被处理或正在处理；如果没有，则标记为正在处理。
     *
     * @return true 表示可以继续处理; false 表示已经 ack 过了，直接退出。
     */
    private boolean checkAndMarkInProcess(String messageId, Channel channel, String queueName) throws Exception
    {
        if (idempotentHandler.isMessageBeingConsumed(messageId) || idempotentHandler.isAccomplish(messageId))
        {
            log.info("[Idempotent] Duplicate message, skipping. queue: {}, messageId: {}", queueName, messageId);
            channel.basicAck(channel.getNextPublishSeqNo(), false);
            // 或者 message.getMessageProperties().getDeliveryTag() 也可
            return false;
        }

        boolean setInProcess = idempotentHandler.setMessageInProcess(messageId);
        if (!setInProcess)
        {
            // 并发下已经有人标记
            log.info("[Idempotent] Message is already being processed. queue: {}, messageId: {}", queueName, messageId);
            channel.basicAck(channel.getNextPublishSeqNo(), false);
            return false;
        }
        return true;
    }

    /**
     * 反序列化消息体
     */
    private Object deserializeMessage(Message message, Class<?> messageDtoClass)
    {
        Object messageDto = JsonUtils.byteArrayToJson(message.getBody(), StandardCharsets.UTF_8, messageDtoClass);
        if (messageDto == null)
        {
            throw new RuntimeException("Failed to deserialize message to " + messageDtoClass.getName());
        }
        return messageDto;
    }

    /**
     * 设置消息完成，并进行 ACK
     */
    private void completeAndAck(String messageId, Channel channel, Message message) throws Exception
    {
        idempotentHandler.setAccomplish(messageId);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    // =============================  2) 异常处理相关  =============================

    /**
     * 处理 BusinessException：这是有意的业务中断，直接 ack 并标记完成。
     */
    private void handleBusinessException(BusinessException be,
                                         String messageId,
                                         Channel channel,
                                         Message message) throws Exception
    {
        log.warn("[BusinessException] messageId: {}, ex: {}", messageId, be.getMessage());
        // 标记完成并 Ack
        idempotentHandler.setAccomplish(messageId);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    /**
     * 处理非业务异常：需要重试/死信/丢弃等逻辑
     */
    private void handleGeneralException(Exception e,
                                        String messageId,
                                        Object messageDto,
                                        Channel channel,
                                        Message message,
                                        boolean isDeadLetter,
                                        String queueName,
                                        String deadLetterQueue) throws Exception
    {
        log.error("Error processing message from queue: {}, messageId: {}", queueName, messageId, e);
        // 清理幂等标识
        idempotentHandler.delMessageProcessed(messageId);

        if (isDeadLetter)
        {
            // 判断死信队列重试次数
            boolean discard = isExceedMaxDeadLetterRetry(message, retryOnDeadLetter);
            if (discard)
            {
                log.warn("[DeadLetter] Exceed max retry. Discard. queue: {}, msgId: {}, dto: {}",
                        queueName, messageId, messageDto);
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            }
            else
            {
                log.info("[DeadLetter] Requeue for retry. queue: {}, msgId: {}", queueName, messageId);
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }
        }
        else if (StringUtils.isBlank(deadLetterQueue))
        {
            // 无死信队列，直接丢弃
            log.warn("[No-DLQ] Consumer failed. Discard. queue: {}, msgId: {}, dto: {}",
                    queueName, messageId, messageDto);
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
        }
        else
        {
            // 普通队列 + 配置了死信队列
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }

    // =============================  3) 业务处理调度  =============================

    /**
     * 调用业务 Bean（RabbitMQMessageHandler）的方法，区分死信与正常消息。
     */
    private void invokeBusinessHandler(Object consumerBean,
                                       Object messageDto,
                                       Channel channel,
                                       Message message,
                                       String messageId,
                                       boolean isDeadLetter,
                                       String deadLetterQueue) throws Exception
    {
        @SuppressWarnings("unchecked") RabbitMQMessageHandler<Object> handler = (RabbitMQMessageHandler<Object>) consumerBean;
        if (isDeadLetter && !StringUtils.isBlank(deadLetterQueue))
        {
            handler.handleDeadLetterMessage(messageDto, channel, message, messageId);
        }
        else
        {
            handler.handleMessage(messageDto, channel, message, messageId);
        }
    }

    // =============================  4) 工具方法  =============================

    /**
     * 判断是否为死信消息：检查 headers 中是否包含 x-death。
     */
    private boolean isDeadLetterMessage(Message message)
    {
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        return headers.containsKey("x-death");
    }

    /**
     * 判断是否超过死信队列的最大重试次数
     */
    private boolean isExceedMaxDeadLetterRetry(Message message, int maxRetryCount)
    {
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        if (headers.containsKey("x-death"))
        {
            @SuppressWarnings("unchecked")
            List<Map<String, ?>> xDeathList = (List<Map<String, ?>>) headers.get("x-death");
            if (xDeathList != null && !xDeathList.isEmpty())
            {
                Map<String, ?> xDeath = xDeathList.get(0);
                Object countObj = xDeath.get("count");
                if (countObj instanceof Number)
                {
                    long count = ((Number) countObj).longValue();
                    return count >= maxRetryCount;
                }
            }
        }
        return false;
    }

    /**
     * 获取或生成 messageId
     */
    private String getOrGenerateMessageId(Message message)
    {
        String messageId = message.getMessageProperties().getMessageId();
        if (StringUtils.isBlank(messageId))
        {
            messageId = UUID.randomUUID().toString();
        }
        return messageId;
    }

    /**
     * 从消费者 Bean 的泛型超类里解析消息 DTO 类型。
     */
    private Class<?> resolveMessageDtoClass(Object consumerBean)
    {
        Class<?> consumerClass = AopUtils.getTargetClass(consumerBean);
        Type superClass = consumerClass.getGenericSuperclass();
        if (!(superClass instanceof ParameterizedType))
        {
            throw new IllegalArgumentException(
                    String.format("Class %s must be parameterized with generic type.", consumerClass.getName()));
        }
        Type[] actualTypeArguments = ((ParameterizedType) superClass).getActualTypeArguments();
        if (actualTypeArguments.length != 1)
        {
            throw new IllegalArgumentException(
                    String.format("Class %s must have exactly one generic type parameter.", consumerClass.getName()));
        }
        Class<?> messageDtoClass = (Class<?>) actualTypeArguments[0];
        log.info("Detected message DTO type for bean [{}]: {}", consumerBean.getClass().getSimpleName(),
                messageDtoClass);
        return messageDtoClass;
    }
}
