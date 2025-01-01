package com.caixy.adminSystem.common.base.response;

import java.util.List;

/**
 * 列表请求的Result
 *
 * @Author CAIXYPROMISE
 * @since 2024/11/9 1:29
 */
public class ListResult<T> extends Result<List<T>>
{
    public ListResult(int code, List<T> data, String message)
    {
        super(code, data, message);
    }

    public ListResult(int code, List<T> data)
    {
        super(code, data);
    }

    public ListResult(ErrorCode errorCode)
    {
        super(errorCode);
    }
}
