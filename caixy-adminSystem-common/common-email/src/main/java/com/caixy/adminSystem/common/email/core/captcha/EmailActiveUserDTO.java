package com.caixy.adminSystem.common.email.core.captcha;

import com.caixy.adminSystem.common.email.domain.common.BaseEmailCaptchaDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 激活账户DTO
 *
 * @Author CAIXYPROMISE
 * @since 2025/2/11 22:24
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class EmailActiveUserDTO extends BaseEmailCaptchaDTO
{
    /**
    * 激活链接
    */
    private String token;
}
