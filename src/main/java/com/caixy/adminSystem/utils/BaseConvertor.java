package com.caixy.adminSystem.utils;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * 基础转换器，提供反射与条件操作的基类方法
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.utils.BaseConvertor
 * @since 2024/8/9 上午2:04
 */
public interface BaseConvertor<T>
{
    default T copyPropertiesWithStrategy(T source, T target, Set<String> ignoreFields, FieldCondition condition)
    {
        return copyFields(source, target, ignoreFields, condition);
    }

    default T copyFields(T source, T target, Set<String> ignoreFields, FieldCondition condition)
    {
        Field[] fields = source.getClass().getDeclaredFields();
        for (Field field : fields)
        {
            if (ignoreFields != null && ignoreFields.contains(field.getName()))
            {
                continue;
            }
            field.setAccessible(true);
            try
            {
                Object sourceValue = field.get(source);
                Object targetValue = field.get(target);
                if (condition.shouldCopy(sourceValue, targetValue))
                {
                    field.set(target, sourceValue);
                }
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException("系统错误，操作失败，操作字段失败");
            }
        }
        return target;  // 返回修改后的对象
    }

    interface FieldCondition
    {
        boolean shouldCopy(Object sourceValue, Object targetValue);
    }
}