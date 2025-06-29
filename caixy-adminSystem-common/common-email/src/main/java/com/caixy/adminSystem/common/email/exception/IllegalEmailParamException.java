package com.caixy.adminSystem.common.email.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 无效邮件参数内容错误
 * 用于获取参数失败或不符合预期时抛出错误
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.common.Email.exception.IllegalEmailParamException
 * @since 2024/10/10 下午5:39
 */
@Slf4j
public class IllegalEmailParamException extends IllegalArgumentException
{
    public IllegalEmailParamException(String message)
    {
        super(message);
        log.error("无效邮件参数内容错误: {}", message);
    }
}
