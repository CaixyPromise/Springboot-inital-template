package com.caixy.adminSystem.model.vo.captcha;

import lombok.Data;

import java.io.Serializable;

/**
 * 验证码VO
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.model.vo.captcha.CaptchaVO
 * @since 2024-07-15 19:44
 **/
@Data
public class CaptchaVO implements Serializable
{
    private static final long serialVersionUID = 1L;
    /**
     * 验证码图片-base64
     */
    private String codeImage;
    /**
     * 验证码uuid
     */
    private String uuid;
}
