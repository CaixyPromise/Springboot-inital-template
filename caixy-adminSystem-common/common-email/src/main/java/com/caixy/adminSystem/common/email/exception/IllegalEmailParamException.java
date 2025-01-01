package com.caixy.adminSystem.common.email.exception;

import com.caixy.adminSystem.common.base.response.ErrorCode;
import com.caixy.adminSystem.common.base.exception.BusinessException;

/**
 * 无效邮件参数内容错误
 * 用于获取参数失败或不符合预期时抛出错误
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.Email.exception.IllegalEmailParamException
 * @since 2024/10/10 下午5:39
 */
public class IllegalEmailParamException extends BusinessException
{
    public IllegalEmailParamException(String message)
    {
        super(ErrorCode.PARAMS_ERROR,  message);
    }
}
