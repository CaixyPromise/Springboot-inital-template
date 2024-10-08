package com.caixy.adminSystem.manager.Email.models;

import com.caixy.adminSystem.manager.Email.core.BaseEmailContentDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Email验证码DTO
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.Email.models.EmailCaptchaDTO
 * @since 2024/10/7 上午12:37
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class EmailCaptchaDTO extends BaseEmailContentDTO
{
    /**
     * 验证码
     */
    private String captcha;

    private static final long serialVersionUID = 1L;
}