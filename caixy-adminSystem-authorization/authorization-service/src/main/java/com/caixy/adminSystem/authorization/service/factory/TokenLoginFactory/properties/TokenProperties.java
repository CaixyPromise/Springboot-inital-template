package com.caixy.adminSystem.authorization.service.factory.TokenLoginFactory.properties;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Token配置类
 *
 * @Author CAIXYPROMISE
 * @since 2025/1/13 2:42
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "login.token")
@ConditionalOnProperty(name = "login.type", havingValue = "TOKEN")
public class TokenProperties
{
    private boolean singleLogin = false;
    private String header = "Authorization";
    private String secret = "CAIXYPROMISE";
    private long expireTime = 60;          // 默认 60 分钟
    private TimeUnit expireTimeUnit = TimeUnit.MINUTES;
    private long refreshTime = 20;         // 默认 20 分钟
    private TimeUnit refreshTimeUnit = TimeUnit.MINUTES;
}