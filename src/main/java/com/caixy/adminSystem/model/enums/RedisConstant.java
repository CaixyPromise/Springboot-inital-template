package com.caixy.adminSystem.model.enums;

import lombok.Getter;

@Getter
public enum RedisConstant
{
    CATEGORY_PARENT_BY_KEY("category:parent:", 3600L * 24 * 7);

    ;

    private final String key;
    private final Long expire;

    RedisConstant(String key, Long expire)
    {
        this.key = key;
        this.expire = expire;
    }

    public String generateKey(String... values)
    {
        String joinValue = String.join(":", values);
        if (this.key.charAt(this.key.length() - 1) == ':')
        {
            return this.key + joinValue;
        }
        return this.key + ":" + joinValue;
    }
}
