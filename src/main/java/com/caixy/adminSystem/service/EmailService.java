package com.caixy.adminSystem.service;

import com.caixy.adminSystem.manager.Email.core.EmailSenderEnum;
import com.caixy.adminSystem.model.dto.email.SendEmailRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * @Name: com.caixy.adminSystem.service.EmailService
 * @Description: 邮箱服务类
 * @Author: CAIXYPROMISE
 * @Date: 2024-01-10 22:00
 **/
public interface EmailService
{

    Boolean sendEmail(SendEmailRequest sendEmailRequest, EmailSenderEnum senderEnum, HttpServletRequest request);
}
