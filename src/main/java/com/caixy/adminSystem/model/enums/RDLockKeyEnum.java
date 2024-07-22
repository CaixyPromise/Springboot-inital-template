package com.caixy.adminSystem.model.enums;

import lombok.Getter;

import java.util.concurrent.TimeUnit;

/**
 * @name: com.caixy.adminSystem.model.enums.RDLockKeyEnum
 * @description: 分布式锁Key枚举
 * @author: CAIXYPROMISE
 * @date: 2024-07-20 02:19
 **/
@Getter
public enum RDLockKeyEnum
{
    USER_LOCK("user_lock", 10, 10, TimeUnit.SECONDS),

    ;
    private final String lockName;
    private final long waitTime;
    private final long expireTime;
    private final TimeUnit timeUnit;

    RDLockKeyEnum(String lockName, long waitTime, long expireTime, TimeUnit timeUnit)
    {
        this.lockName = lockName.endsWith(":") ? lockName : lockName + ":";
        this.waitTime = waitTime;
        this.expireTime = expireTime;
        this.timeUnit = timeUnit;
    }

    public String generateKey(String... items)
    {
        if (items == null || items.length == 0)
        {
            return lockName;
        }
        return lockName.concat(String.join(":", items));
    }
}
