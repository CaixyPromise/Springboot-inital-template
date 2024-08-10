package com.caixy.adminSystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * 邮箱配置
 *
 * @name: com.caixy.adminSystem.config.EmailConfig
 * @author: CAIXYPROMISE
 * @since: 2024-01-10 21:16
 **/
@Configuration
@ConfigurationProperties(prefix = "spring.mail")
@Data
public class EmailConfig
{
    private String host;
    private Integer port;
    private String username;
    private String password;
    private String defaultEncoding;
    private Properties properties;

    @Bean
    public JavaMailSender javaMailSender()
    {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        mailSender.setDefaultEncoding(defaultEncoding);

        if (properties != null)
        {
            mailSender.setJavaMailProperties(asProperties(properties));
        }

        return mailSender;
    }

    // 将 Map 转换为 Properties
    private Properties asProperties(Properties map)
    {
        Properties props = new Properties();
        props.putAll(map);
        return props;
    }
}
