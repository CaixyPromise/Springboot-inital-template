package com.caixy.adminSystem.model.enums;

import lombok.Getter;

@Getter
public enum RedisConstant
{

    CATEGORY_PARENT_BY_KEY("category:parent:", -1L),

    /**
     * 验证码缓存，5分钟
     */
    CAPTCHA_CODE("captcha:", 60L * 5),

    ;

    private final String key;
    private final Long expire;

    RedisConstant(String key, Long expire)
    {
        this.key = key.endsWith(":") ? key : key + ":";
        this.expire = expire;
    }

    public String generateKey(String... items)
    {
        return key.concat(String.join(":", items));
    }
}
