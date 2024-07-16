package com.caixy.adminSystem.service;

import com.caixy.adminSystem.model.vo.captcha.CaptchaVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 验证码服务类
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.service.CaptchaService
 * @since 2024-07-16 04:08
 **/
public interface CaptchaService
{
    /**
     * 随机获取一个验证码服务类
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/7/16 上午4:09
     */
    CaptchaVO getAnyCaptcha(HttpServletRequest request);

    /**
     * 获取指定类型的验证码服务类
     */
    CaptchaVO getCaptchaByType(HttpServletRequest request, String type);
}
