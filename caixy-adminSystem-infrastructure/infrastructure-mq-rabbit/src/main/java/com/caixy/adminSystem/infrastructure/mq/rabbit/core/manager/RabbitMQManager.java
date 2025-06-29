package com.caixy.adminSystem.infrastructure.mq.rabbit.core.manager;

import com.caixy.adminSystem.common.base.utils.MapUtils;
import com.caixy.adminSystem.infrastructure.mq.rabbit.core.enums.RabbitMQQueueEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * RabbitMq工具类
 *
 * @Author CAIXYPROMISE
 * @since 2025/1/13 0:28
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQManager
{

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送延迟消息
     *
     * @author CAIXYPROMISE
     */
    /*──────────────────────── 发送延迟消息 ────────────────────────*/
    public void sendDelayedMessage(RabbitMQQueueEnum queueEnum, Object payload) {

        switch (queueEnum.getDelayMode()) {
            case PLUGIN:
                if (queueEnum.getTtlMillis() == null) {
                    log.warn("发送失败: PLUGIN 模式必须在枚举中指定 ttlMillis(x-delay)");
                    return;
                }
                Map<String,Object> hdr = MapUtils.of("x-delay", queueEnum.getTtlMillis());
                convertAndSend(queueEnum, payload, hdr, null);
                break;

            case TTL:
                // 队列本身已配置 x-message-ttl，无需额外 header
                convertAndSend(queueEnum, payload, MapUtils.newHashMap(), null);
                break;
            case NONE:
                log.warn("队列 [{}] 为 NONE 模式，已退回普通发送。", queueEnum.getQueueName());
                sendMessage(queueEnum, payload);
                break;
        }
    }
    /**
     *  发送延迟消息 + 重试计数
     *
     * @author CAIXYPROMISE
     */
    public void sendDelayedMessageWithRetry(RabbitMQQueueEnum queueEnum,
                                            Object payload,
                                            int retryCount) {

        Map<String,Object> hdr = new HashMap<>();
        hdr.put("x-retry-count", retryCount);

        if (queueEnum.isPluginDelay()) {
            hdr.put("x-delay", queueEnum.getTtlMillis());
        } // TTL 模式不需要 x-delay

        convertAndSend(queueEnum, payload, hdr, null);
        log.info("发送延迟消息，队列：{}，retryCount={}，payload={}",
                queueEnum.getQueueName(), retryCount, payload);
    }

    /**
     * 发送普通消息
     *
     * @author CAIXYPROMISE
     */
    public void sendMessage(RabbitMQQueueEnum queueEnum, Object payload) {
        convertAndSend(queueEnum, payload, MapUtils.newHashMap(), null);
    }

    /**
     * 共用的实际发送方法
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @version 2025/5/28 4:40
     */
    private void convertAndSend(RabbitMQQueueEnum qEnum,
                                Object payload,
                                Map<String,Object> headers,
                                MessagePostProcessor extraProcessor) {

        CorrelationData cd = new CorrelationData(UUID.randomUUID().toString());

        rabbitTemplate.convertAndSend(
                qEnum.getExchange(),
                qEnum.getRoutingKey(),
                payload,
                msg -> {
                    // 统一设置 JSON & 自定义头
                    msg.getMessageProperties().setContentType(MessageProperties.CONTENT_TYPE_JSON);
                    headers.forEach((k,v) -> msg.getMessageProperties().setHeader(k, v));
                    return extraProcessor == null ? msg : extraProcessor.postProcessMessage(msg);
                },
                cd
        );
        log.debug("Rabbit 发送成功：ID={}, exchange={}, rk={}, queue={}",
                cd.getId(), qEnum.getExchange(), qEnum.getRoutingKey(), qEnum.getQueueName());
    }
}
