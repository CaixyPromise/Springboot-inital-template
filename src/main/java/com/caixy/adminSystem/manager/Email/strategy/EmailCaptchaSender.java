package com.caixy.adminSystem.manager.Email.strategy;

import cn.hutool.core.util.RandomUtil;
import com.caixy.adminSystem.manager.Email.annotation.EmailSender;
import com.caixy.adminSystem.manager.Email.core.EmailSenderEnum;
import com.caixy.adminSystem.manager.Email.core.EmailSenderStrategy;
import com.caixy.adminSystem.manager.Email.models.EmailCaptchaDTO;
import com.caixy.adminSystem.manager.Email.utils.FreeMarkEmailUtil;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 验证码邮件处理器
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.Email.strategy.EmailCaptchaSender
 * @since 2024/10/7 上午12:54
 */
@EmailSender(EmailSenderEnum.CAPTCHA)
@Component
public class EmailCaptchaSender implements EmailSenderStrategy
{

    @Override
    public String getEmailContent(Map<String, Object> params)
    {
        String code = RandomUtil.randomNumbers(6);
        EmailCaptchaDTO emailCaptchaDTO = new EmailCaptchaDTO();
        emailCaptchaDTO.setCaptcha(code);
        return FreeMarkEmailUtil.generateContent(EmailSenderEnum.CAPTCHA.getTemplateName(), emailCaptchaDTO);
    }
}
