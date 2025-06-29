package com.caixy.adminSystem.common.email.domain.enums;

import lombok.Getter;

/**
 * 邮箱验证码类型枚举
 *
 * @Author CAIXYPROMISE
 * @since 2025/4/25 0:28
 */
@Getter
public enum EmailCaptchaTypeEnum
{
    NUMBER(0, "数字验证码"), ALPHA(1, "字母验证码"), MIXTURE(2, "混合验证码");
    private final int code;
    private final String desc;

    EmailCaptchaTypeEnum(int code, String desc)
    {
        this.code = code;
        this.desc = desc;
    }
    public EmailCaptchaTypeEnum getEnumByCode(int code) {
        for (EmailCaptchaTypeEnum value : values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        return null;
    }
}
