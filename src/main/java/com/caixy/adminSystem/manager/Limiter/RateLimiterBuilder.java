package com.caixy.adminSystem.manager.Limiter;

import com.caixy.adminSystem.common.ErrorCode;
import com.caixy.adminSystem.exception.BusinessException;
import org.redisson.api.RRateLimiter;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * 限流器配置建造者
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.limiter.RateLimiterBuilder
 * @since 2024-07-17 02:20
 **/
public class RateLimiterBuilder<T>
{
    private final RRateLimiter rateLimiter;
    private Callable<T> onSuccessAction = () -> null;
    private Runnable onFailureAction = () -> {};
    private Supplier<RuntimeException> exceptionSupplier;

    public RateLimiterBuilder(RRateLimiter rateLimiter)
    {
        this.rateLimiter = rateLimiter;
    }

    public RateLimiterBuilder<T> onSuccess(Callable<T> action)
    {
        this.onSuccessAction = action;
        return this;
    }

    public RateLimiterBuilder<T> onFailure(Runnable action)
    {
        this.onFailureAction = action;
        return this;
    }

    public RateLimiterBuilder<T> orElseThrow(Supplier<RuntimeException> exceptionSupplier)
    {
        this.exceptionSupplier = exceptionSupplier;
        return this;
    }

    public T execute()
    {
        boolean accessGranted = rateLimiter.tryAcquire(1);
        if (accessGranted)
        {
            try
            {
                return onSuccessAction.call();
            }
            catch (Exception e)
            {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "访问过于频繁，请稍后再试");
            }
        }
        else
        {
            if (exceptionSupplier != null)
            {
                throw exceptionSupplier.get();
            }
            else
            {
                if (onFailureAction != null)
                {
                    onFailureAction.run();
                }
                else
                {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "访问过于频繁，请稍后再试");
                }
            }
        }
        return null;
    }
}
