package com.caixy.adminSystem.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @name: com.caixy.adminSystem.annotation.RedissionClient
 * @description: 注入redission客户端
 * @author: CAIXYPROMISE
 * @date: 2024-07-20 00:41
 **/
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectRedissonClient
{
    String clientName();
    String name();
}
