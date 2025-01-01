package com.caixy.adminSystem.common.web.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * XSS配置类
 *
 * @Author CAIXYPROMISE
 * @since 2024/12/31 20:23
 */
@Configuration
@ConfigurationProperties(prefix = "xss")
@Data
public class XssProperties
{
    private boolean enabled;
    private String excludes;
    private String urlPatterns;
}