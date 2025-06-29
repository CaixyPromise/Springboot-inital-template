package com.caixy.adminSystem.common.base.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 将spring boot Bean注入到静态方法内。
 *
 * @Author CAIXYPROMISE
 * @since 2025/4/25 4:48
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StaticInject {
    Class<?> value();
}
