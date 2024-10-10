package com.caixy.adminSystem.manager.Email.strategy;

import com.caixy.adminSystem.manager.Email.annotation.EmailSender;
import com.caixy.adminSystem.manager.Email.core.EmailSenderEnum;
import com.caixy.adminSystem.manager.Email.core.EmailSenderStrategy;
import com.caixy.adminSystem.manager.Email.exception.IllegalEmailParamException;
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
@EmailSender(EmailSenderEnum.RESET_PASSWORD)
@Component
public class EmailCaptchaSender implements EmailSenderStrategy
{

    @Override
    public String getEmailContent(Map<String, Object> params)
    {
        Object code = params.get("code");
        if (code == null) {
            throw new IllegalEmailParamException("验证码信息为空");
        }
        // 创建邮箱内容实体类
        EmailCaptchaDTO emailCaptchaDTO = new EmailCaptchaDTO();
        emailCaptchaDTO.setCaptcha(code.toString());
        return FreeMarkEmailUtil.generateContent(EmailSenderEnum.RESET_PASSWORD.getTemplateName(), emailCaptchaDTO);
    }
}
