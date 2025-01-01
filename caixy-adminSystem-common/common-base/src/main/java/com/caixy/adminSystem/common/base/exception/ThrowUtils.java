package com.caixy.adminSystem.common.base.exception;


import com.caixy.adminSystem.common.base.response.ErrorCode;

import java.util.function.Supplier;

/**
 * 抛业务异常工具类
 */
public class ThrowUtils
{

    /**
     * 条件成立则抛异常
     *
     * @param condition 条件
     * @param runtimeException 异常
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException)
    {
        if (condition)
        {
            throw runtimeException;
        }
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition 条件
     * @param errorCode 错误码
     * @see BusinessException
     */
    public static void throwIf(boolean condition, ErrorCode errorCode)
    {
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition 条件
     * @param errorCode 错误码
     * @param message 错误报错信息
     * @see BusinessException
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message)
    {
        throwIf(condition, new BusinessException(errorCode, message));
    }

    /**
     * 传入lambda表达式，条件成立则抛异常
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @version 2024/11/23 15:44
     */
    public static void throwIf(Supplier<Boolean> condition, ErrorCode errorCode, String message) {
        throwIf(condition.get(), errorCode, message);
    }

    /**
     * 传入lambda表达式，条件成立则抛异常
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @version 2024/11/23 15:44
     */
    public static void throwIf(Supplier<Boolean> condition, ErrorCode errorCode) {
        throwIf(condition.get(), errorCode);
    }
}
