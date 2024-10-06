package com.caixy.adminSystem.manager.Limiter;

import com.caixy.adminSystem.annotation.InjectRedissonClient;
import com.caixy.adminSystem.model.enums.RedisLimiterEnum;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 专门提供 RedisLimiter 限流基础服务
 *
 * @name: com.caixy.project.manager.RedisLimiterManager
 * @author: CAIXYPROMISE
 * @since: 2024-01-07 21:29
 **/
@Component
@Slf4j
public class RedisLimiterManager
{
    @InjectRedissonClient(clientName = "limiter", name = "限流器")
    private RedissonClient redissonClient;

    /**
     * 执行限流策略
     *
     * @param redisLimiterEnum 限流器key
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/7/16 上午0:46
     */
    public <T> RateLimiterBuilder<T> doLimit(RedisLimiterEnum redisLimiterEnum, String... items)
    {
        String key = redisLimiterEnum.generateKey(items);
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        rateLimiter.trySetRate(RateType.OVERALL, redisLimiterEnum.getRate(), redisLimiterEnum.getRateInterval(),
                RateIntervalUnit.SECONDS);
        redissonClient.getBucket(key).expire(getDuration(redisLimiterEnum.getRateIntervalUnit()));
        return new RateLimiterBuilder<>(rateLimiter);
    }

    private Duration getDuration(RateIntervalUnit unit)
    {
        switch (unit)
        {
        case SECONDS:
            return Duration.ofMinutes(30); // 为秒级限流设置30分钟的过期时间
        case MINUTES:
            return Duration.ofHours(6); // 为分钟级限流设置6小时的过期时间
        case HOURS:
            return Duration.ofDays(1); // 为小时级限流设置1天的过期时间
        default:
            return Duration.ofHours(1); // 默认为一小时
        }
    }

}
