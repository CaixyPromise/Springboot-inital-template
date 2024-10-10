package com.caixy.adminSystem.model.enums;

import com.caixy.adminSystem.common.BaseCacheableEnum;
import lombok.Getter;

@Getter
public enum RedisKeyEnum implements BaseCacheableEnum
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

    RedisKeyEnum(String key, Long expire)
    {
        this.key = key.endsWith(":") ? key : key + ":";
        this.expire = expire;
    }
}
