package com.caixy.adminSystem.common.email.core;

import com.caixy.adminSystem.common.email.domain.common.BaseEmailContentDTO;
import com.caixy.adminSystem.common.email.domain.enums.BaseEmailSenderEnum;

/**
 * 邮件发送策略接口
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.common.Email.core.EmailSenderStrategy
 * @since 2024/10/7 上午12:53
 */
public interface EmailContentGeneratorStrategy<T extends BaseEmailContentDTO>
{
    String getEmailContent(T emailContentDTO, BaseEmailSenderEnum emailSenderEnum);
}
