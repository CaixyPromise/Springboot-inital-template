package com.caixy.adminSystem.infrastucture.cache.redis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Jackson的redis序列化器
 *
 * @Author CAIXYPROMISE
 * @since 2025/6/29 上午1:49
 */
public class JacksonRedisSerializer<T> implements RedisSerializer<T>
{
    private final ObjectMapper objectMapper;
    private final Class<T> type;

    public JacksonRedisSerializer(Class<T> type) {
        this.type = type;
        this.objectMapper = new ObjectMapper(); // 使用 Jackson 的 ObjectMapper
    }

    @Override
    public byte[] serialize(T t) {
        try {
            if (t == null) {
                return new byte[0];
            }
            return objectMapper.writeValueAsBytes(t); // 将对象序列化为 byte[]
        } catch (Exception e) {
            throw new RuntimeException("Serialization error", e);
        }
    }

    @Override
    public T deserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            // 直接反序列化为目标对象类型
            return objectMapper.readValue(bytes, type);
        } catch (Exception e) {
            throw new RuntimeException("Deserialization error", e);
        }
    }
}