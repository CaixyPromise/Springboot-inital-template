package com.caixy.adminSystem.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @name: com.caixy.adminSystem.annotation.CaptchaTypeTarget
 * @description: 验证码类型标注注解
 * @author: CAIXYPROMISE
 * @date: 2024-07-16 03:35
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CaptchaTypeTarget
{
    String value();
}
