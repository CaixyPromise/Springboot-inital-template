package com.caixy.adminSystem.exception;

import com.caixy.adminSystem.common.ErrorCode;

/**
 * 抛异常工具类
 *
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
}
