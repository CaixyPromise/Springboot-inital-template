package com.caixy.adminSystem.manager.Email;

import com.caixy.adminSystem.config.EmailConfig;
import com.caixy.adminSystem.constant.EmailConstant;
import com.caixy.adminSystem.manager.Email.annotation.EmailSender;
import com.caixy.adminSystem.manager.Email.core.BaseEmailContentDTO;
import com.caixy.adminSystem.manager.Email.core.EmailSenderDTO;
import com.caixy.adminSystem.manager.Email.core.EmailSenderEnum;
import com.caixy.adminSystem.manager.Email.core.EmailSenderStrategy;
import com.caixy.adminSystem.manager.Email.factory.EmailSenderFactory;
import com.caixy.adminSystem.manager.Email.models.EmailCaptchaDTO;
import com.qcloud.cos.model.ciModel.auditing.Conf;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 邮件发送管理器
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.Email.EmailSenderManager
 * @since 2024/10/1 下午3:45
 */
@Slf4j
@Component
@AllArgsConstructor
public class EmailSenderManager
{
    private final JavaMailSender mailSender;

    private final EmailConfig emailConfig;

    private final EmailSenderFactory emailSenderFactory;

    private final ThreadPoolExecutor EMAIL_SENDER_POOL = new ThreadPoolExecutor(
            10,
            20,
            60,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new ThreadPoolExecutor.DiscardPolicy());
    private final HashMap<String, Boolean> resultMap = new HashMap<>();

    /**
     * 异步发送邮件
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/9 上午12:07
     */
    public void doSendBySync(EmailSenderEnum senderEnum, EmailSenderDTO emailSenderDTO, Map<String, Object> params)
    {
        CompletableFuture.runAsync(() -> {
            boolean sendEmail = sendEmail(senderEnum, emailSenderDTO, params);
            log.info("邮件发送结果：{}", sendEmail);
        }, EMAIL_SENDER_POOL);
    }

    /**
     * 阻塞发送邮件
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/9 上午12:07
     */
    public boolean doSend(EmailSenderEnum senderEnum, EmailSenderDTO emailSenderDTO, Map<String, Object> params)
    {
        return sendEmail(senderEnum, emailSenderDTO, params);
    }

    private boolean sendEmail(EmailSenderEnum senderEnum, EmailSenderDTO emailSenderDTO, Map<String, Object> params)
    {
        try
        {
            EmailSenderStrategy strategy = emailSenderFactory.getStrategy(senderEnum);
            String emailContent = strategy.getEmailContent(params);
            if (StringUtils.isBlank(emailContent))
            {
                log.error("邮件内容为空");
                return false;
            }
            if (StringUtils.isBlank(emailSenderDTO.getToEmail()))
            {
                log.error("邮件接收人为空");
                return false;
            }
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            // 邮箱发送内容组成
            helper.setSubject(emailSenderDTO.buildSubjectTitle(senderEnum));
            helper.setText(emailContent, true);
            helper.setTo(emailSenderDTO.getToEmail());
            helper.setFrom(EmailConstant.EMAIL_TITLE + '<' + emailConfig.getUsername() + '>');
            mailSender.send(message);
            return true;
        }
        catch (MessagingException e)
        {
            log.error("邮件发送失败", e);
            return false;
        }
    }


}
