package com.caixy.adminSystem.exception;

import com.caixy.adminSystem.common.ErrorCode;
import lombok.Getter;

/**
 * 自定义异常类
 *
 */
@Getter
public class BusinessException extends RuntimeException
{

    /**
     * 错误码
     */
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

}
