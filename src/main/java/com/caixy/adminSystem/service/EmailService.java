package com.caixy.adminSystem.service;

/**
 * @Name: com.caixy.adminSystem.service.EmailService
 * @Description: 邮箱服务类
 * @Author: CAIXYPROMISE
 * @Date: 2024-01-10 22:00
 **/
public interface EmailService
{

    /**
     * 发送邮箱验证码
     *
     * @param targetEmailAccount 邮箱账号
     * @param captcha      验证码
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/1/10 2:04
     */
    void sendCaptchaEmail(String targetEmailAccount, String captcha);

    /**
     * 发送支付成功信息
     *
     * @param targetEmailAccount 邮箱账号
     * @param orderName    订单名称
     * @param orderTotal   订单金额
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/1/10 2:04
     */
    void sendPaymentSuccessEmail(String targetEmailAccount,
                                 String orderName,
                                 String orderTotal);
}
