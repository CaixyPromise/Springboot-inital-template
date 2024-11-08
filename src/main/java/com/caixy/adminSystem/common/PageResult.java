package com.caixy.adminSystem.common;

import org.apache.poi.ss.formula.functions.T;

import java.util.List;

/**
 * 分页请求的Result
 *
 * @Author CAIXYPROMISE
 * @since 2024/11/9 1:29
 */
public class PageResult<T> extends Result<List<T>>
{
    public PageResult(int code, List<T> data, String message)
    {
        super(code, data, message);
    }

    public PageResult(int code, List<T> data)
    {
        super(code, data);
    }

    public PageResult(ErrorCode errorCode)
    {
        super(errorCode);
    }
}
