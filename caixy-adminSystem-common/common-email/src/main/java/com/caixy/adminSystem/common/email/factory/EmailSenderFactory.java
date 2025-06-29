package com.caixy.adminSystem.common.email.factory;

import com.caixy.adminSystem.common.email.annotation.EmailSender;
import com.caixy.adminSystem.common.email.domain.common.BaseEmailContentDTO;
import com.caixy.adminSystem.common.email.domain.enums.BaseEmailSenderEnum;
import com.caixy.adminSystem.common.email.core.EmailContentGeneratorStrategy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;


import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Email发送类工厂
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.common.Email.factory.EmailSenderFactory
 * @since 2024/10/8 上午1:07
 */
@Slf4j
@Service
@AllArgsConstructor
public class EmailSenderFactory
{
    private static final Map<BaseEmailSenderEnum, EmailContentGeneratorStrategy> strategies = new HashMap<>();

    private final ApplicationContext applicationContext;

    @PostConstruct
    public void init()
    {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(EmailSender.class);

        for (Object bean : beans.values())
        {
            // 获取类上的 @EmailSenderStrategy 注解
            EmailSender annotation = bean.getClass().getAnnotation(EmailSender.class);
            // 获取注解中的所有枚举值
            BaseEmailSenderEnum[] captchaBizEnums = annotation.captcha();
            BaseEmailSenderEnum[] textBizEnums = annotation.text();
            // 将枚举值与策略类注册到 Map 中
            Stream.concat(Arrays.stream(captchaBizEnums), Arrays.stream(textBizEnums))
                .forEach((item) -> {
                    if (strategies.containsKey(item)) {
                        log.warn("重复注册 EmailSender 策略 [{}]，已存在的策略将被覆盖", item.getName());
                    }
                    strategies.put(item, (EmailContentGeneratorStrategy) bean);
                });
        }
    }

    // 获取对应的邮件发送策略
    @SuppressWarnings("unchecked")
    public <T extends BaseEmailContentDTO> EmailContentGeneratorStrategy<T> getStrategy(BaseEmailSenderEnum emailSenderEnum)
    {
        EmailContentGeneratorStrategy<T> strategy = strategies.get(emailSenderEnum);
        if (strategy == null)
        {
            throw new IllegalArgumentException("No strategy found for email type: " + emailSenderEnum);
        }
        return strategy;
    }

}
