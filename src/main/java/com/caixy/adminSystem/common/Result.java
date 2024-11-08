package com.caixy.adminSystem.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回类
 *
 * @param <T>
 */
@Data
public class Result<T> implements Serializable
{

    private int code;

    private T data;

    private String message;

    public Result(int code, T data, String message)
    {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public Result(int code, T data)
    {
        this(code, data, "");
    }

    public Result(ErrorCode errorCode)
    {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
