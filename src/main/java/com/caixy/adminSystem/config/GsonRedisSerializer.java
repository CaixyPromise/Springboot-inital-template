package com.caixy.adminSystem.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * GsonRedis构造器
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.config.GsonRedisSerializer
 * @since 2024-06-16 11:07
 **/
public class GsonRedisSerializer<T> implements RedisSerializer<T>
{
    private final Gson gson;
    private final Type type;

    public GsonRedisSerializer(Type type)
    {
        this.type = type;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Long.class, (JsonSerializer<Long>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
                .registerTypeAdapter(Long.TYPE, (JsonSerializer<Long>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
                .create();
    }

    @Override
    public byte[] serialize(T t)
    {
        return gson.toJson(t).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public T deserialize(byte[] bytes)
    {
        if (bytes == null || bytes.length == 0)
        {
            return null;
        }
        return gson.fromJson(new String(bytes, StandardCharsets.UTF_8), type);
    }
}

