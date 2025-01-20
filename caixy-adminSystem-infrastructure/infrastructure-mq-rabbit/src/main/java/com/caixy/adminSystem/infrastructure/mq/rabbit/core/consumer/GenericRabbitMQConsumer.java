package com.caixy.adminSystem.infrastructure.mq.rabbit.core.consumer;

import com.rabbitmq.client.Channel;
import com.caixy.adminSystem.infrastructure.mq.rabbit.core.enums.RabbitMQQueueEnum;
import com.caixy.adminSystem.infrastructure.mq.rabbit.core.manager.RabbitMQManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;

import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * 通用的消息消费类
 *
 * @param <T> 消息 DTO 的类型
 */
@Slf4j
public abstract class GenericRabbitMQConsumer<T> implements RabbitMQMessageHandler<T>
{
    @Resource
    protected RabbitMQManager rabbitMQUtils;

    private final Class<T> messageType;

    private static final String RETRY_COUNT_KEY = "x-retry-count";
    private static final String X_DEATH_HEADER = "x-death";

    @SuppressWarnings("unchecked")
    public GenericRabbitMQConsumer()
    {
        Type superClass = getClass().getGenericSuperclass();
        log.info("superClass: {}", superClass);
        if (superClass instanceof ParameterizedType)
        {
            Type[] actualTypeArguments = ((ParameterizedType) superClass).getActualTypeArguments();
            log.info("actualTypeArguments: {}", (Object) actualTypeArguments);
            if (actualTypeArguments[0] instanceof Class<?>)
            {
                this.messageType = (Class<T>) actualTypeArguments[0];
            }
            else
            {
                throw new IllegalArgumentException("Unable to determine the generic type because it's not a class.");
            }
        }
        else
        {
            throw new IllegalArgumentException("Class is not parameterized with generic type.");
        }
    }

    /**
     * 判断是否为死信消息
     *
     * @param message 消息对象
     * @return 是否为死信消息
     */
    protected boolean isDeadLetterMessage(Message message)
    {
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        log.info("header is: {}", headers);
        return headers.containsKey(X_DEATH_HEADER);
    }

    /**
     * 确认消息
     *
     * @param channel    RabbitMQ 的 Channel 对象
     * @param rawMessage 原始消息对象
     */
    protected void confirmMessage(Channel channel, Message rawMessage)
    {
        try
        {
            channel.basicAck(rawMessage.getMessageProperties().getDeliveryTag(), false);
        }
        catch (IOException e)
        {
            log.error("Failed to confirm message: {}", rawMessage.getMessageProperties().getMessageId(), e);
        }
    }

    /**
     * 丢弃消息
     *
     * @param channel    RabbitMQ 的 Channel 对象
     * @param rawMessage 原始消息对象
     */
    protected void discardMessage(Channel channel, Message rawMessage)
    {
        try
        {
            // 设置 requeue 为 false 将不会把消息放回队列，实际上是丢弃了消息
            channel.basicReject(rawMessage.getMessageProperties().getDeliveryTag(), false);
        }
        catch (IOException e)
        {
            log.error("Failed to discard message: {}", rawMessage.getMessageProperties().getMessageId(), e);
        }
    }

    /**
     * 拒绝消息，并重新入队
     *
     * @param channel           RabbitMQ 通道对象
     * @param rawMessage        原始消息对象
     * @param rabbitMQQueueEnum 队列枚举信息
     * @param maxRetries        最大重试次数
     * @throws IOException 异常
     */
    protected void rejectAndRetryOrDiscard(Channel channel,
                                           Message rawMessage,
                                           Object message,
                                           RabbitMQQueueEnum rabbitMQQueueEnum,
                                           int maxRetries) throws IOException
    {
        long deliveryTag = rawMessage.getMessageProperties().getDeliveryTag();
        Integer retryCount = getRetryCount(rawMessage);

        if (retryCount < maxRetries)
        {
            // 增加重试次数并重新发送消息到延迟队列
            int newRetryCount = retryCount + 1;
            rabbitMQUtils.sendDelayedMessageWithRetry(rabbitMQQueueEnum, message, newRetryCount);
            // 丢弃原始消息，源消息
            discardMessage(channel, rawMessage);
        }
        else
        {
            log.info("达到最大重试次数，拒绝消息，不重新入队（消息会进入死信队列或被丢弃）");
            // 达到最大重试次数，拒绝消息，不重新入队（消息会进入死信队列或被丢弃）
            rejectMessage(channel, rawMessage);
        }
    }

    /**
     * 获取重试次数
     *
     * @param rawMessage 原始消息对象
     * @return 重试次数
     */
    protected Integer getRetryCount(Message rawMessage)
    {
        return (Integer) rawMessage.getMessageProperties().getHeaders().getOrDefault(RETRY_COUNT_KEY, 0);
    }

    /**
     * 拒绝消息，并放入死信队列
     *
     * @param channel RabbitMQ 的 Channel 对象
     * @param message 消息对象
     * @throws IOException 异常
     */
    protected void rejectMessage(Channel channel, Message message) throws IOException
    {
        channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
    }
}
