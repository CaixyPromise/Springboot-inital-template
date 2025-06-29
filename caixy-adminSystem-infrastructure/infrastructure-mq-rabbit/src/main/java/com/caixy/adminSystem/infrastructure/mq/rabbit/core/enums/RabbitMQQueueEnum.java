package com.caixy.adminSystem.infrastructure.mq.rabbit.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.HashMap;
import java.util.Map;


/**
 * @name: com.caixy.adminSystem.model.enums.RabbitMQQueueEnum
 * @description: 普通消息队列信息枚举封装
 * @author: CAIXYPROMISE
 * @date: 2024-06-19 22:30
 **/
@AllArgsConstructor
@Getter
public enum RabbitMQQueueEnum
{
    /*======= 示例 1：普通直连队列 =======*/
    LINK_STATS("link.stats.exchange",       // exchange
            "link.stats.save",           // routingKey
            "link.stats.queue",          // queue
            null,                        // deadLetterQueue
            DelayMode.NONE,              // 普通
            null, null                   // other params
    ),

    /*======= 示例 2：插件式延迟队列 =======*/
    ORDER_DELAY("order.delay.exchange", "order.create", "order.delay.queue", null, DelayMode.PLUGIN,            // 使用 x‑delayed-message
            "direct",                    // delayedType
            null                         // ttl
    ),

    /*======= 示例 3：TTL + DLX 延迟重试 =======*/
    RETRY_60S("retry.exchange", "retry.60s", "retry.queue.60s", "business.queue",            // 过期后投递到此队列
            DelayMode.TTL, null, 60_000L                      // TTL 60s
    );

    private final String exchange;
    private final String routingKey;
    private final String queueName;
    private final String deadLetterQueue;
    private final DelayMode delayMode;
    private final String delayedType;   // 仅 PLUGIN 用
    private final Long ttlMillis;     // 仅 TTL 用

    /* 统一生成 queue arguments */
    public Map<String, Object> queueArgs()
    {
        Map<String, Object> a = new HashMap<>();
        switch (delayMode)
        {
            case PLUGIN:
                a.put("x-delayed-type", delayedType);
                break;
            case TTL:
                if (ttlMillis != null)
                {
                    a.put("x-message-ttl", ttlMillis);
                }
                if (deadLetterQueue != null)
                {
                    a.put("x-dead-letter-exchange", deadLetterQueue + ".dlx");
                    a.put("x-dead-letter-routing-key", deadLetterQueue + ".dlq");
                }
                break;
            default:
                break;
        }
        return a;
    }

    public boolean isDelay()
    {
        // 只要不是 NONE，就视为“延迟队列”
        return (delayMode != null) && (!delayMode.equals(DelayMode.NONE));
    }

    public boolean isPluginDelay()
    {
        return delayMode == DelayMode.PLUGIN;
    }

    public boolean isTtlDelay()
    {
        return delayMode == DelayMode.TTL;
    }
}
