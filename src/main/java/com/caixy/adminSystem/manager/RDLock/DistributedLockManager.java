package com.caixy.adminSystem.manager.RDLock;

import com.caixy.adminSystem.annotation.InjectRedissonClient;
import com.caixy.adminSystem.common.ErrorCode;
import com.caixy.adminSystem.exception.BusinessException;
import com.caixy.adminSystem.model.enums.RDLockKeyEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁的工具类
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.RDLock.DistributedLockManager
 * @since 2024-07-20 01:16
 **/
@Slf4j
@Component
public class DistributedLockManager
{
    @InjectRedissonClient(clientName = "lock", name = "分布式锁")
    private RedissonClient redissonClient;

    /**
     * 尝试在指定时间内获取锁，并在获取成功后执行Supplier提供的操作。
     * 适用于对响应时间敏感的任务，如用户请求处理，短时间任务等。
     *
     * @param lockName     锁的名称
     * @param waitTime     最大等待时间
     * @param leaseTime    锁的持有时间，在此时间后自动释放锁
     * @param unit         时间单位
     * @param supplier     锁获取成功时执行的操作
     * @param errorCode    锁获取失败的错误代码
     * @param errorMessage 锁获取失败的错误信息
     * @return 操作结果，如果在指定时间内未能获取锁，则抛出异常
     */
    private <T> T redissonDistributedLocks(String lockName,
                                          Supplier<T> supplier,
                                          long waitTime,
                                          long leaseTime,
                                          TimeUnit unit,
                                          ErrorCode errorCode,
                                          String errorMessage)
    {
        RLock rLock = redissonClient.getLock(lockName);
        try
        {
            if (rLock.tryLock(waitTime, leaseTime, unit))
            {
                return supplier.get();
            }
            throw new BusinessException(errorCode.getCode(), errorMessage);
        }
        catch (Exception e)
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        finally
        {
            if (rLock.isHeldByCurrentThread())
            {
                outUnlockLog(Thread.currentThread().getId());
                rLock.unlock();
            }
        }
    }

    /**
     * redisson分布式锁 可自定义 waitTime 、leaseTime、TimeUnit
     * 获取锁并执行Runnable提供的操作。此方法不返回结果。
     * 适用于不需要返回值的操作，如简单的状态更新，事件触发等。
     *
     * @param waitTime     等待时间
     * @param leaseTime    使用时间
     * @param unit         时间单位
     * @param lockName     锁名称
     * @param runnable     获取到锁后执行的方法
     * @param errorCode    错误代码
     * @param errorMessage 错误消息
     */
    private void redissonDistributedLocks(String lockName,
                                          Runnable runnable,
                                          long waitTime,
                                          long leaseTime,
                                          TimeUnit unit,
                                          ErrorCode errorCode,
                                          String errorMessage)
    {
        RLock rLock = redissonClient.getLock(lockName);
        errorMessage = StringUtils.isNotBlank(errorMessage) ? errorMessage : errorCode.getMessage();
        try
        {
            if (rLock.tryLock(waitTime, leaseTime, unit))
            {
                runnable.run();
            }
            else
            {
                throw new BusinessException(errorCode.getCode(), errorMessage);
            }
        }
        catch (Exception e)
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, errorMessage);
        }
        finally
        {
            if (rLock.isHeldByCurrentThread())
            {
                outUnlockLog(Thread.currentThread().getId());
                rLock.unlock();
            }
        }
    }


    private void outUnlockLog(long id)
    {
        log.error("unLock: {}", id);
    }

    public <T> T redissonDistributedLocks(RDLockKeyEnum rdLockKeyEnum,
                                          Supplier<T> supplier,
                                          ErrorCode errorCode,
                                          String errorMessage,
                                          String... keyItems)
    {
        return redissonDistributedLocks(rdLockKeyEnum.generateKey(keyItems),
                supplier,
                rdLockKeyEnum.getWaitTime(),
                rdLockKeyEnum.getExpireTime(),
                rdLockKeyEnum.getTimeUnit(),
                errorCode,
                errorMessage);
    }

    public <T> T redissonDistributedLocks(RDLockKeyEnum rdLockKeyEnum, Supplier<T> supplier, String... keyItems)
    {
        return redissonDistributedLocks(rdLockKeyEnum.generateKey(keyItems),
                supplier,
                rdLockKeyEnum.getWaitTime(),
                rdLockKeyEnum.getExpireTime(),
                rdLockKeyEnum.getTimeUnit(),
                ErrorCode.OPERATION_ERROR,
                null);
    }

    public void redissonDistributedLocks(RDLockKeyEnum rdLockKeyEnum,
                                         Runnable runnable,
                                         ErrorCode errorCode,
                                         String errorMessage,
                                         String... keyItems)
    {
        redissonDistributedLocks(rdLockKeyEnum.generateKey(keyItems),
                runnable,
                rdLockKeyEnum.getWaitTime(),
                rdLockKeyEnum.getExpireTime(),
                rdLockKeyEnum.getTimeUnit(),
                errorCode,
                errorMessage);
    }

    public void redissonDistributedLocks(RDLockKeyEnum rdLockKeyEnum,
                                         Runnable runnable,
                                         String... keyItems)
    {
        redissonDistributedLocks(rdLockKeyEnum.generateKey(keyItems),
                runnable,
                rdLockKeyEnum.getWaitTime(),
                rdLockKeyEnum.getExpireTime(),
                rdLockKeyEnum.getTimeUnit(),
                ErrorCode.OPERATION_ERROR,
                null);
    }
}
