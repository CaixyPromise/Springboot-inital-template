package com.caixy.adminSystem.common.email.service;

import com.caixy.adminSystem.common.api.user.vo.UserVO;
import com.caixy.adminSystem.common.email.domain.enums.EmailSenderEnum;
import com.caixy.adminSystem.common.email.domain.dto.SendEmailRequest;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;

/**
 * @Name: com.caixy.adminSystem.service.EmailService
 * @Description: 邮箱服务类
 * @Author: CAIXYPROMISE
 * @Date: 2024-01-10 22:00
 **/
public interface EmailService
{
    Boolean doSend(@RequestBody SendEmailRequest sendEmailRequest);
}
