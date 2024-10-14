package com.caixy.adminSystem.manager.Email.core;

import com.caixy.adminSystem.common.BaseCacheableEnum;
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
public enum EmailSenderEnum implements BaseCacheableEnum
{
    /**
     * 注册账号
     */
    REGISTER("register",
            60L * 5,
            1,
            "注册账号",
            "captcha.html.ftl",
            false,
            true),

    /**
     * 激活用户
     */
    ACTIVE_USER("active_user",
            60L * 5,
            5,
            "激活用户",
            "captcha.html.ftl",
            false,
            true),
    /**
     * 修改密码
     */
    RESET_PASSWORD("reset_psw",
            60L * 5,
            3,
            "修改密码",
            "captcha.html.ftl",
            true,
            false),
    /**
     * 修改邮箱
     */
    RESET_EMAIL("reset_email",
            60L * 5,
            10,
            "修改邮箱",
            "captcha.html.ftl",
            true,
            true),

    ;
    private final String key;
    private final Long expire;
    private final Integer code;
    private final String name;
    private final String templateName;
    private final Boolean requireLogin;
    private final Boolean requireToEmail;


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
