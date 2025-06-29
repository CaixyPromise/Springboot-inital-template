package com.caixy.adminSystem.common.base.constant;

import com.caixy.adminSystem.common.base.utils.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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
    TimeUnit getTimeUnit();

    default Long getExpireSeconds() {
        return getExpire() == null ? 0 : getTimeUnit().toSeconds(getExpire());
    }

    default String generateKey(Object... items) {
        String key = getKey();
        if (key == null || StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("key cannot be null or empty");
        }
        if (!key.endsWith(":")) {
            key = key + ":";
        }
        if (items == null || items.length == 0) {
            return key;
        }

        // 将 items 转为字符串，进行 Base64 编码，并用 ":" 连接
        String combinedKey = key.concat(
                Arrays.stream(items)
                        .map(item -> encodeItem(String.valueOf(item))) // 编码 item
                        .collect(Collectors.joining(":"))
        );

        // 如果 key 长度超出限制，使用 hash
        return combinedKey.length() > 1024 ? hashKey(combinedKey) : combinedKey;
    }

    /**
     * 对单个 item
     */
    static String encodeItem(String item) {
        // 替换特殊字符，保持可读性
        return item.replace(":", "%3A")
                .replace("/", "%2F")
                .replace("?", "%3F")
                .replace("&", "%26")
                .replace("=", "%3D")
                .replace("#", "%23");
    }

    /**
     * 对长 key 进行 MD5 哈希
     */
    static String hashKey(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to hash key: " + key, e);
        }
    }
}
