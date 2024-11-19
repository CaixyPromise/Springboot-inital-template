package com.caixy.adminSystem.annotation.validator;

import com.caixy.adminSystem.aop.validator.NotBeforeCurrentTimeValidator;
import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 不早于当前时间的校验注解
 *
 * @Author CAIXYPROMISE
 * @since 2024/11/20 0:45
 */
@Documented
@Constraint(validatedBy = NotBeforeCurrentTimeValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface NotBeforeCurrentTime
{
    String message() default "The date must not be before the current time";

    boolean allowNull() default false;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}