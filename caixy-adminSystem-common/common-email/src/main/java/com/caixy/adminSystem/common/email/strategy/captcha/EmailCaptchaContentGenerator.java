package com.caixy.adminSystem.common.email.strategy.captcha;

import com.caixy.adminSystem.common.email.annotation.EmailSender;
import com.caixy.adminSystem.common.email.core.EmailContentGeneratorStrategy;
import com.caixy.adminSystem.common.email.exception.IllegalEmailParamException;
import com.caixy.adminSystem.common.email.domain.enums.BaseEmailSenderEnum;
import com.caixy.adminSystem.common.email.domain.enums.EmailCaptchaBizEnum;
import com.caixy.adminSystem.common.email.domain.common.BaseEmailCaptchaDTO;
import com.caixy.adminSystem.common.email.utils.FreeMarkEmailUtil;
import org.springframework.stereotype.Component;

/**
 * 验证码邮件处理器
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.common.Email.strategy.captcha.EmailCaptchaSender
 * @since 2024/10/7 上午12:54
 */
@EmailSender(captcha = {                           // 分别处理以下业务的发送：
    EmailCaptchaBizEnum.RESET_PASSWORD,  // 修改密码
    EmailCaptchaBizEnum.ACTIVE_USER,        // 注册账号
    EmailCaptchaBizEnum.RESET_EMAIL,     // 修改邮箱
})
@Component
public class EmailCaptchaContentGenerator implements EmailContentGeneratorStrategy<BaseEmailCaptchaDTO>
{

    @Override
    public String getEmailContent(BaseEmailCaptchaDTO emailCaptchaDTO, BaseEmailSenderEnum emailSenderEnum)
    {
        String code = emailCaptchaDTO.getCaptcha();
        if (code == null)
        {
            throw new IllegalEmailParamException("验证码信息为空");
        }
        return FreeMarkEmailUtil.generateContent(emailSenderEnum.getTemplateName(), emailCaptchaDTO);
    }
}
