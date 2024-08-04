package com.caixy.adminSystem.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注入OAuth服务端配置
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.annotation.InjectOAuthConfig
 * @since 2024/8/3 上午2:04
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectOAuthConfig
{
    String clientName();
    String name() default "";
}
