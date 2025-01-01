package com.caixy.adminSystem.authorization.service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 登录类型配置
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.config.LoginTypeConfiguration
 * @since 2024/10/28 01:36
 */
@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "login")
public class LoginTypeConfiguration
{
    private LoginType type = LoginType.SESSION;
}
