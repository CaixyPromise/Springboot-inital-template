package com.caixy.adminSystem.aop.validator;


import com.caixy.adminSystem.annotation.validator.NotBeforeCurrentTime;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 不早于当前时间的校验器
 *
 * @Author CAIXYPROMISE
 * @since 2024/11/20 0:45
 */
public class NotBeforeCurrentTimeValidator implements ConstraintValidator<NotBeforeCurrentTime, Object>
{
    private boolean allowNull; // 是否允许为空

    @Override
    public void initialize(NotBeforeCurrentTime constraintAnnotation)
    {
        this.allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context)
    {
        if (value == null)
        {
            return allowNull;
        }

        LocalDateTime now = LocalDateTime.now();

        if (value instanceof LocalDateTime)
        {
            LocalDateTime dateTime = (LocalDateTime) value;
            return !dateTime.isBefore(now);
        }
        else if (value instanceof Date)
        {
            LocalDateTime dateTime = ((Date) value).toInstant()
                                                   .atZone(ZoneId.systemDefault()).toLocalDateTime();
            return !dateTime.isBefore(now);
        }

        throw new IllegalArgumentException("Unsupported type for validation");
    }
}