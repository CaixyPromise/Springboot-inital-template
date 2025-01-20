package com.caixy.adminSystem.common.base.constant;

import com.caixy.adminSystem.common.base.utils.StringUtils;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 缓存Key枚举
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.common.BaseCacheEnum
 * @since 2024/10/10 下午6:07
 */
public interface BaseCacheEnum
{
    String getKey();
    Long getExpire();

    default TimeUnit getTimeUnit() {
        return TimeUnit.SECONDS;
    }


    default Long getExpireSeconds() {
        return getExpire() == null ? 0 : getTimeUnit().toSeconds(getExpire());
    }

    default String generateKey(Object... items)
    {
        String key = getKey();
        if (key == null || StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("key cannot be null or empty");
        }
        if (!key.endsWith(":"))
        {
            key = key + ":";
        }
        if (items == null || items.length == 0)
        {
            return key;
        }
        // 将 items 中的每个元素转为字符串并用 ":" 连接
        return key.concat(
                Arrays.stream(items)
                      .map(String::valueOf) // 确保每个元素转为字符串
                      .collect(Collectors.joining(":"))
        );
    }
}
