package com.caixy.adminSystem.common.email.service;

import com.caixy.adminSystem.common.email.domain.common.BaseEmailContentDTO;
import com.caixy.adminSystem.common.email.domain.dto.SendEmailRequest;
import com.caixy.adminSystem.common.email.domain.enums.BaseEmailSenderEnum;
import com.caixy.adminSystem.common.email.domain.enums.EmailCaptchaBizEnum;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Name: com.caixy.adminSystem.service.EmailService
 * @Description: 邮箱服务类
 * @Author: CAIXYPROMISE
 * @Date: 2024-01-10 22:00
 **/
public interface EmailService
{
    void sendEmail(String toEmail, BaseEmailContentDTO emailContentDTO, BaseEmailSenderEnum senderEnum);

    void verifyCaptcha(EmailCaptchaBizEnum emailSenderEnum, String toEmail, String code);
}
