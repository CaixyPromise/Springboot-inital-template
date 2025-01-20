package com.caixy.adminSystem.infrastructure.mq.rabbit.core.enums;

import com.caixy.adminSystem.common.base.constant.BaseCacheEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeUnit;

/**
 * 消息队列缓存key
 *
 * @Author CAIXYPROMISE
 * @since 2025/1/20 16:36
 */
@Getter
@AllArgsConstructor
public enum RabbitMQCacheEnum implements BaseCacheEnum
{
    /**
     * 消息队列消费完成幂等性
     */
    IDEMPOTENCY_ACCOMPLISH_KEY("rabbit:idempotency:accomplish", 1L, TimeUnit.DAYS),
    /**
     * 消息队列消费中状态幂等性
     */
    IDEMPOTENCY_PROCESSING_KEY("rabbit:idempotency:processing", 1L, TimeUnit.HOURS),
    ;

    private final String key;
    private final Long expire;
    private final TimeUnit timeUnit;
    RabbitMQCacheEnum(String key, Long expire)
    {
        this.key = key;
        this.expire = expire;
        this.timeUnit = TimeUnit.SECONDS;
    }
}
