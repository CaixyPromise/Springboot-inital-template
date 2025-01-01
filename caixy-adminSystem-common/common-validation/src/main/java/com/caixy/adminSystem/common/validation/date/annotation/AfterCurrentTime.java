package com.caixy.adminSystem.common.validation.date.annotation;


import com.caixy.adminSystem.common.validation.date.AfterCurrentTimeValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 校验一个日期时间字段是否晚于当前时间
 *
 * @Author CAIXYPROMISE
 * @since 2024/11/20 1:05
 */
@Documented
@Constraint(validatedBy = AfterCurrentTimeValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface AfterCurrentTime {
    String message() default "The date must be after the current time";

    Class<?>[] groups() default {};

    boolean allowNull() default false;

    Class<? extends Payload>[] payload() default {};
}