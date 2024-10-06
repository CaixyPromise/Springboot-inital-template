package com.caixy.adminSystem.config;

import com.caixy.adminSystem.manager.OAuth.annotation.InjectOAuthConfig;
import com.caixy.adminSystem.config.properties.OAuth2ClientProperties;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.Optional;

/**
 * OAuth2配置类
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.config.OAuth2ClientConfig
 * @since 2024/8/2 下午3:10
 */
@Data
@Configuration
public class OAuth2ClientConfig implements BeanPostProcessor
{
    private static final Logger log = LoggerFactory.getLogger(OAuth2ClientConfig.class);
    @Resource
    private OAuth2ClientProperties oAuth2Properties;

    @Override
    public Object postProcessBeforeInitialization(@NotNull Object bean, @NotNull String beanName) throws BeansException
    {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, @NotNull String beanName)
    {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields)
        {
            if (field.isAnnotationPresent(InjectOAuthConfig.class))
            {
                InjectOAuthConfig annotation = field.getAnnotation(InjectOAuthConfig.class);
                field.setAccessible(true);
                try
                {
                    OAuth2ClientProperties.OAuth2Client oAuth2Client = oAuth2Properties.getInstance(annotation.clientName());
                    Optional<OAuth2ClientProperties.OAuth2Client> optionalOAuth2Client = Optional.ofNullable(oAuth2Client);
                    if (optionalOAuth2Client.isPresent())
                    {
                        field.set(bean, oAuth2Client);
                        log.info("注入OAuth2配置成功: {}", field.getName());
                    }
                    else {
                        throw new RuntimeException("OAuth2配置注入失败，未找到对应的配置信息");
                    }
                }
                catch (IllegalAccessException e)
                {
                    log.error("注入OAuth2配置失败: {}", e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }
        return bean;
    }

    @PostConstruct
    public void init()
    {
        System.out.println("OAuth2 Properties Loaded: " + oAuth2Properties.getInstance().size());
        if (oAuth2Properties.getInstance() != null)
        {
            oAuth2Properties.getInstance().forEach((key, client) ->
            {
                System.out.println("Client [" + key + "] ID: " + client.getClientId());
                System.out.println("Client Secret: " + client.getClientSecret());
                System.out.println("Callback URL: " + client.getCallBackUrl());
            });
        }
        else
        {
            System.out.println("OAuth2 properties instance is null");
        }
    }

}
