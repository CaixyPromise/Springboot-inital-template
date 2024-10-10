package com.caixy.adminSystem.manager.Email.annotation;

import com.caixy.adminSystem.manager.Email.core.EmailSenderEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Email发送者处理器
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.Email.annotation.EmailSender
 * @since 2024/10/6 下午6:14
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
    public @interface EmailSender
{
    EmailSenderEnum[] value();
}
