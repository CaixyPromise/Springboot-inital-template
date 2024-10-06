package com.caixy.adminSystem.manager.Email.utils;


import com.caixy.adminSystem.config.EmailConfig;
import com.caixy.adminSystem.constant.EmailConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.MessagingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;

/**
 * 邮件工具集
 *
 * @name: com.caixy.adminSystem.utils.EmailUtils
 * @author: CAIXYPROMISE
 * @since: 2024-01-10 19:05
 **/
@Slf4j
public class EmailTemplateUtil
{
    /**
     * 生成电子邮件内容
     *
     * @param captcha       验证码
     * @param emailHtmlPath 电子邮件html路径
     * @return {@link String}
     */
    public static String buildCaptchaEmailContent(String emailHtmlPath, String captcha)
    {
        // 加载邮件html模板
        ClassPathResource resource = new ClassPathResource(emailHtmlPath);
        InputStream inputStream = null;
        BufferedReader fileReader = null;
        StringBuilder buffer = new StringBuilder();
        String line;
        try {
            inputStream = resource.getInputStream();
            fileReader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = fileReader.readLine()) != null)
            {
                buffer.append(line);
            }
        }
        catch (Exception e)
        {
            log.info("发送邮件读取模板失败{}", e.getMessage());
        }
        finally
        {
            if (fileReader != null)
            {
                try
                {
                    fileReader.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            if (inputStream != null)
            {
                try
                {
                    inputStream.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        // 替换html模板中的参数
        return MessageFormat.format(buffer.toString(), captcha,
                EmailConstant.EMAIL_TITLE,
                EmailConstant.EMAIL_TITLE_ENGLISH,
                EmailConstant.PLATFORM_RESPONSIBLE_PERSON,
                EmailConstant.PLATFORM_ADDRESS);
    }


    /**
     * 构建付费成功电子邮件内容
     *
     * @param emailHtmlPath 电子邮件html路径
     * @param orderName     订单名称
     * @param orderTotal    订单总额
     * @return {@link String}
     */
    public static String buildPaySuccessEmailContent(String emailHtmlPath, String orderName, String orderTotal)
    {
        // 加载邮件html模板
        ClassPathResource resource = new ClassPathResource(emailHtmlPath);
        InputStream inputStream = null;
        BufferedReader fileReader = null;
        StringBuilder buffer = new StringBuilder();
        String line;
        try {
            inputStream = resource.getInputStream();
            fileReader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = fileReader.readLine()) != null)
            {
                buffer.append(line);
            }
        }
        catch (Exception e) {
            log.info("发送邮件读取模板失败{}", e.getMessage());
        }
        finally
        {
            if (fileReader != null)
            {
                try {
                    fileReader.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null)
            {
                try {
                    inputStream.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // 替换html模板中的参数
        return MessageFormat.format(buffer.toString(),
                                    orderName,
                                    orderTotal,
                                    EmailConstant.PLATFORM_RESPONSIBLE_PERSON,
                                    EmailConstant.PATH_ADDRESS,
                                    EmailConstant.EMAIL_TITLE);
    }

    /**
     * 发送支付成功电子邮件
     *
     * @param emailAccount 电子邮件帐户
     * @param mailSender   邮件发件人
     * @param emailConfig  电子邮件配置
     * @param orderName    订单名称
     * @param orderTotal   订单总额
     * @throws MessagingException 消息传递异常
     */
    public void sendPaySuccessEmail(String emailAccount, JavaMailSender mailSender, EmailConfig emailConfig, String orderName, String orderTotal)
            throws MessagingException
    {

        SimpleMailMessage message = new SimpleMailMessage();
        // 邮箱发送内容组成
//        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        message.setSubject("【" + EmailConstant.EMAIL_TITLE + "】感谢您的购买，请查收您的订单");
        message.setText(buildPaySuccessEmailContent(EmailConstant.EMAIL_HTML_PAY_SUCCESS_PATH, orderName, orderTotal));
        message.setTo(emailAccount);
        message.setFrom(EmailConstant.EMAIL_TITLE + '<' + emailConfig.getUsername() + '>');
        mailSender.send(message);
    }
}
