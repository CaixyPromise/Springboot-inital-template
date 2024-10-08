package com.caixy.adminSystem.manager.Email.factory;

import com.caixy.adminSystem.manager.Email.annotation.EmailSender;
import com.caixy.adminSystem.manager.Email.core.BaseEmailContentDTO;
import com.caixy.adminSystem.manager.Email.core.EmailSenderDTO;
import com.caixy.adminSystem.manager.Email.core.EmailSenderEnum;
import com.caixy.adminSystem.manager.Email.core.EmailSenderStrategy;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Email发送类工厂
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.Email.factory.EmailSenderFactory
 * @since 2024/10/8 上午1:07
 */
@Service
@AllArgsConstructor
public class EmailSenderFactory
{
    private static final Map<EmailSenderEnum, EmailSenderStrategy> strategies = new HashMap<>();

    private final ApplicationContext applicationContext;

    @PostConstruct
    public void init()
    {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(EmailSender.class);

        for (Object bean : beans.values())
        {
            // 获取类上的 @EmailSenderStrategy 注解
            EmailSender annotation = bean.getClass().getAnnotation(EmailSender.class);
            EmailSenderEnum emailSenderEnum = annotation.value();
            // 注册到策略 Map 中
            strategies.put(emailSenderEnum, (EmailSenderStrategy) bean);
        }
    }

    // 获取对应的邮件发送策略
    public EmailSenderStrategy getStrategy(EmailSenderEnum emailSenderEnum)
    {
        EmailSenderStrategy strategy = strategies.get(emailSenderEnum);
        if (strategy == null)
        {
            throw new IllegalArgumentException("No strategy found for email type: " + emailSenderEnum);
        }
        return strategy;
    }
}
