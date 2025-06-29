package com.caixy.adminSystem.common.email.strategy.captcha;

import com.caixy.adminSystem.common.base.utils.StringUtils;
import com.caixy.adminSystem.common.email.annotation.EmailSender;
import com.caixy.adminSystem.common.email.core.EmailContentGeneratorStrategy;
import com.caixy.adminSystem.common.email.exception.IllegalEmailParamException;
import com.caixy.adminSystem.common.email.domain.enums.BaseEmailSenderEnum;
import com.caixy.adminSystem.common.email.domain.enums.EmailCaptchaBizEnum;
import com.caixy.adminSystem.common.email.core.captcha.EmailActiveUserDTO;
import com.caixy.adminSystem.common.email.utils.FreeMarkEmailUtil;
import org.springframework.stereotype.Component;

/**
 * 发送激活账户邮件
 *
 * @Author CAIXYPROMISE
 * @since 2025/2/11 22:23
 */

@EmailSender(captcha = {EmailCaptchaBizEnum.ACTIVE_USER}) // 激活账户
@Component
public class EmailActiveUserContentGenerator implements EmailContentGeneratorStrategy<EmailActiveUserDTO>
{
    @Override
    public String getEmailContent(EmailActiveUserDTO emailContentDTO, BaseEmailSenderEnum emailSenderEnum)
    {
        if (StringUtils.isAnyBlank(emailContentDTO.getToken(), emailContentDTO.getCaptcha())) {
            throw new IllegalEmailParamException("验证码信息为空");
        }
        return FreeMarkEmailUtil.generateContent(emailSenderEnum.getTemplateName(), emailContentDTO);
    }
}
