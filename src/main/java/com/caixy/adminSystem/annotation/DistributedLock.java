package com.caixy.adminSystem.annotation;

import com.caixy.adminSystem.model.enums.RDLockKeyEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>分布式锁注解，用于方法级别。当调用该方法时，系统将尝试获取指定的分布式锁。</p>
 * <p>参数可以通过SpEL表达式从方法运行时动态解析。</p>
 * @name: com.caixy.adminSystem.annotation.DistributedLock
 * @author: CAIXYPROMISE
 * @date: 2024-07-20 01:50
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock
{
    RDLockKeyEnum lockKeyEnum();
    String args() default "";
}
