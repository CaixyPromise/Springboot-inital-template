package com.caixy.adminSystem.common.web.resolver.annotation;

import java.lang.annotation.*;

/**
 * 需要注入登录用户信息注解
 *
 * @Author CAIXYPROMISE
 * @since 2025/1/20 1:24
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LoginUser
{
    /**
     * 是否必须登录
     */
    boolean required() default true;
}
