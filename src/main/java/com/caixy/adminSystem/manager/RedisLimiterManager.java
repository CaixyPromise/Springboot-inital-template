package com.caixy.adminSystem.manager;

import com.caixy.adminSystem.common.ErrorCode;
import com.caixy.adminSystem.config.RedisSessionManager;
import com.caixy.adminSystem.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

/**
 * 专门提供 RedisLimiter 限流基础服务
 *
 * @name: com.caixy.project.manager.RedisLimiterManager
 * @author: CAIXYPROMISE
 * @since: 2024-01-07 21:29
 **/
@Service
@Slf4j
public class RedisLimiterManager
{
    private final RedissonClient redissonClient;

    public RedisLimiterManager(RedisSessionManager redisSessionManager)
    {
        ThrowUtils.throwIf(redisSessionManager == null, ErrorCode.SYSTEM_ERROR, "RedissonClient init fail.");
        // 加载redis限流器客户端
        redissonClient = redisSessionManager.getRedissonClient("limiter");
        log.info("RedisLimiterManager init.");
    }

    /**
     * 执行限流操作
     *
     * @param key 用于标识不同的限流器
     */
    public void doRateLimiter(String key)
    {
        log.info("RedisLimiterManager doRateLimiter key:{}", key);
        // 创建一个限流器
        RRateLimiter rateLimiter= redissonClient.getRateLimiter(key);
        // 尝试设置限流器的规则。这里设定的规则是每秒最多允许2个请求
        // 这里的1指的是获取1个权限，也就是每秒最多处理2个请求
        rateLimiter.trySetRate(RateType.OVERALL, 10, 1, RateIntervalUnit.SECONDS);
        // 尝试获取1个权限，如果获取成功返回true，失败返回false
        boolean canAccess = rateLimiter.tryAcquire(1);
        log.info("RedisLimiterManager doRateLimiter canAccess:{}", canAccess);
        // 如果无法访问，则抛出异常
        ThrowUtils.throwIf(!canAccess, ErrorCode.OPERATION_ERROR,"访问过于频繁，请稍后再试");
    }

}
