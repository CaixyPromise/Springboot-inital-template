package com.caixy.adminSystem.aop.validator;

import com.caixy.adminSystem.annotation.validator.AfterCurrentTime;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 校验一个日期时间字段是否晚于当前时间校验器
 *
 * @Author CAIXYPROMISE
 * @since 2024/11/20 1:06
 */
public class AfterCurrentTimeValidator implements ConstraintValidator<AfterCurrentTime, Object>
{
    private boolean allowNull; // 是否允许为空

    @Override
    public void initialize(AfterCurrentTime constraintAnnotation)
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
            return !dateTime.isAfter(now);
        }
        else if (value instanceof Date)
        {
            LocalDateTime dateTime = ((Date) value).toInstant()
                                                   .atZone(ZoneId.systemDefault()).toLocalDateTime();
            return !dateTime.isAfter(now);
        }

        throw new IllegalArgumentException("Unsupported type for validation");
    }
}