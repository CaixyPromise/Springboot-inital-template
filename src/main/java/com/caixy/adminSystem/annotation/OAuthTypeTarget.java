package com.caixy.adminSystem.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * OAuth类型注解
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.annotation.OAuthTypeTarget
 * @since 2024/8/3 下午4:04
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OAuthTypeTarget
{
    String clientName();
}
