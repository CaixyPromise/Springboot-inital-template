package com.caixy.adminSystem.common.base.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 静态属性注入spring boot配置注解-属性注解 - 不能用在final字段上。
 *
 * @Author CAIXYPROMISE
 * @since 2025/4/25 4:26
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StaticValue {
    String value(); // 配置文件中的key
    String defaultValue() default ""; // 默认值
    String onSucceed() default "";  // 成功时调用的方法
    boolean parameterized() default true; // 是否需要参数(注入的value)
}
