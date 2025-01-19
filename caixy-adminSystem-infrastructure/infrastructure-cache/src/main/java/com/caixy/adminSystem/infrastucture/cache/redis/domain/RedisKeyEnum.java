package com.caixy.adminSystem.infrastucture.cache.redis.domain;

import com.caixy.adminSystem.common.base.constant.BaseCacheEnum;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

@Getter
public enum RedisKeyEnum implements BaseCacheEnum
{

    CATEGORY_PARENT_BY_KEY("category:parent:", -1L),

    /**
     * 验证码缓存，5分钟
     */
    CAPTCHA_CODE("captcha:", 60L * 5),

    /**
     * github OAuth验证信息缓存，5分钟
     */
    GITHUB_OAUTH("github_oauth:", 60L * 5),

    RESET_PASSWORD("reset_psw", 60L * 5),


    ;

    private final String key;
    private final Long expire;
    private final TimeUnit timeUnit = TimeUnit.SECONDS;

    RedisKeyEnum(String key, Long expire)
    {
        this.key = key.endsWith(":") ? key : key + ":";
        this.expire = expire;
    }
}
