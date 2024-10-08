package com.caixy.adminSystem.manager.Email.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Email发送类型枚举
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.Email.core.EmailSenderEnum
 * @since 2024/10/6 下午6:16
 */
@Getter
@AllArgsConstructor
public enum EmailSenderEnum
{
    REGISTER(1, "注册邮件", "captcha.html.ftl"),
    FORGET_PASSWORD(2, "忘记密码邮件", "captcha.html.ftl"),
    CAPTCHA(3, "验证码邮件", "captcha.html.ftl"),
    PAYMENT_INFO(4, "支付信息", "captcha.html.ftl"),
    ACTIVE_USER(5, "激活用户", "captcha.html.ftl");

    private final Integer code;
    private final String name;
    private final String templateName;

    public static EmailSenderEnum getByCode(Integer code)
    {
        if (code == null)
        {
            return null;
        }
        for (EmailSenderEnum value : values())
        {
            if (value.getCode().equals(code))
            {
                return value;
            }
        }
        return null;
    }
}
