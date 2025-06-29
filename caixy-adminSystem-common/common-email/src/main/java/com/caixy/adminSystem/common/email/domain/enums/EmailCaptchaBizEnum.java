package com.caixy.adminSystem.common.email.domain.enums;

import cn.hutool.core.util.RandomUtil;
import com.caixy.adminSystem.common.base.constant.BaseCacheEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

/**
 * 验证码发送业务枚举
 *
 * @Author CAIXYPROMISE
 * @since 2025/4/25 0:56
 */
@Getter
@AllArgsConstructor
public enum EmailCaptchaBizEnum implements BaseCacheEnum, BaseEmailSenderEnum
{
    RESET_EMAIL(
            0,
            "modify_email",
            "common-captcha.html.ftl",
            "reset_email",
            5L,
            TimeUnit.MINUTES,
            EmailCaptchaTypeEnum.NUMBER,
            6
            ),
    ACTIVE_USER(
            5,
            "activate_user",
            "active-account.html.ftl",
            "active_user",
            5L,
            TimeUnit.MINUTES,
            EmailCaptchaTypeEnum.NUMBER,
            6),
    RESET_PASSWORD(10,
            "reset_password",
            "common-captcha.html.ftl",
            "reset_psw",
            5L,
            TimeUnit.MINUTES,
            EmailCaptchaTypeEnum.NUMBER,
            6),
    ;
    private final Integer code;
    private final String name;
    private final String templateName;
    private final String key;
    private final Long expire;
    private final TimeUnit timeUnit;
    private final EmailCaptchaTypeEnum captchaTypeEnum;
    private final Integer captchaLength;

    public String generateCaptchaCode() {
        switch (captchaTypeEnum)
        {
            case NUMBER:
                return RandomUtil.randomNumbers(captchaLength);
            case ALPHA:
                return RandomUtil.randomStringUpper(captchaLength);
            case MIXTURE:
                return RandomUtil.randomString(captchaLength);
            default:
                throw new IllegalArgumentException("Unknown captcha type: " + captchaTypeEnum);
        }
    }
}
