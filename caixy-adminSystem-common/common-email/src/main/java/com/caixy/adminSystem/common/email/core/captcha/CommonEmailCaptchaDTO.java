package com.caixy.adminSystem.common.email.core.captcha;

import com.caixy.adminSystem.common.email.domain.common.BaseEmailCaptchaDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通用邮件验证码类，对应captcha.html.ftl
 *
 * @Author CAIXYPROMISE
 * @since 2025/4/25 2:55
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class CommonEmailCaptchaDTO extends BaseEmailCaptchaDTO
{
    private String bizName;
}
