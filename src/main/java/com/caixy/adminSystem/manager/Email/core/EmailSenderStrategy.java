package com.caixy.adminSystem.manager.Email.core;

import java.util.Map;

/**
 * 邮件发送策略接口
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.Email.core.EmailSenderStrategy
 * @since 2024/10/7 上午12:53
 */
public interface EmailSenderStrategy
{
    String getEmailContent(Map<String, Object> params);
}
