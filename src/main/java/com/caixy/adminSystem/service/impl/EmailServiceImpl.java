package com.caixy.adminSystem.service.impl;


import com.caixy.adminSystem.common.ErrorCode;
import com.caixy.adminSystem.config.EmailConfig;
import com.caixy.adminSystem.constant.EmailConstant;
import com.caixy.adminSystem.exception.BusinessException;
import com.caixy.adminSystem.service.EmailService;
import com.caixy.adminSystem.utils.EmailTemplateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 邮箱服务类实现
 *
 * @name: com.caixy.adminSystem.service.impl.EmailServiceImpl
 * @author: CAIXYPROMISE
 * @since: 2024-01-10 22:01
 **/
@Service
@Slf4j
public class EmailServiceImpl implements EmailService
{
    @Resource
    private JavaMailSender mailSender;


    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            10,
            10,
            60L,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(1000),
            new ThreadPoolExecutor.AbortPolicy());

    @Resource
    private EmailConfig emailConfig;

    /**
     * 发送邮箱验证码
     *
     * @param targetEmailAccount 邮箱账号
     * @param captcha            验证码
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/1/10 2:04
     */
    @Override
    public void sendCaptchaEmail(String targetEmailAccount, String captcha)
    {
        // 提交异步任务, 不关心返回值
        CompletableFuture.runAsync(() ->
        {
            try {
                // 发送邮件逻辑
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message);
//        SimpleMailMessage message = new SimpleMailMessage();
                // 邮箱发送内容组成
                helper.setSubject(EmailConstant.EMAIL_SUBJECT);
                helper.setText(EmailTemplateUtil.buildCaptchaEmailContent(EmailConstant.EMAIL_HTML_CONTENT_PATH, captcha), true);
                helper.setTo(targetEmailAccount);
                helper.setFrom(EmailConstant.EMAIL_TITLE + '<' + emailConfig.getUsername() + '>');
                mailSender.send(helper.getMimeMessage());
            }
            catch (MessagingException e)
            {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码发送失败");
            }
        }, threadPoolExecutor);
    }

    /**
     * 发送支付成功信息
     *
     * @param targetEmailAccount 邮箱账号
     * @param orderName          订单名称
     * @param orderTotal         订单金额
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/1/10 2:04
     */
    @Override
    public void sendPaymentSuccessEmail(String targetEmailAccount, String orderName, String orderTotal)
    {
        CompletableFuture.runAsync(() ->
        {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message);
                // 邮箱发送内容组成
                helper.setSubject("【" + EmailConstant.EMAIL_TITLE + "】感谢您的购买，请查收您的订单");
                helper.setText(EmailTemplateUtil.buildPaySuccessEmailContent(EmailConstant.EMAIL_HTML_PAY_SUCCESS_PATH, orderName, orderTotal), true);
                helper.setTo(targetEmailAccount);
                helper.setFrom(EmailConstant.EMAIL_TITLE + '<' + emailConfig.getUsername() + '>');
                mailSender.send(message);
            }
            catch (MessagingException e)
            {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "邮件发送失败");
            }
        }, threadPoolExecutor);
    }
}
