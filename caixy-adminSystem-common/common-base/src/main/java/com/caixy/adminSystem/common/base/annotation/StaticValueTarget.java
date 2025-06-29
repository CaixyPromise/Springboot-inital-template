package com.caixy.adminSystem.common.base.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 静态属性注入spring boot配置注解-标记哪些类要被扫描
 *
 * @Author CAIXYPROMISE
 * @since 2025/4/25 4:27
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface StaticValueTarget {}
