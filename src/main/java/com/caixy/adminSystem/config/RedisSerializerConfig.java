package com.caixy.adminSystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

/**
 * redis序列化构造器
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.config.RedisSerializerConfig
 * @since 2024-06-16 11:06
 **/
@Component
public class RedisSerializerConfig
{
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory)
    {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        // 使用自定义的 GsonRedisSerializer
        GsonRedisSerializer<Object> serializer = new GsonRedisSerializer<>(Object.class);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        return template;
    }
}
