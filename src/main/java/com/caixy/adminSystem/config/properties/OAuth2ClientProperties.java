package com.caixy.adminSystem.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * OAuth2服务端配置读取类
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.config.properties.OAuth2ClientProperties
 * @since 2024/8/2 下午3:11
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "oauth2")
public class OAuth2ClientProperties
{
    private HashMap<String, OAuth2Client> instance = new HashMap<>();

    @Data
    public static class OAuth2Client
    {
        /**
         * 验证的服务端地址
         */
        private String authServerUrl;

        /**
         * 获取accessToken地址
         */
        private String accessTokenUrl;

        /**
         * 获取信息地址
         */
        private String fetchInfoUrl;

        /**
         * 客户端id
         */
        private String clientId;
        /**
         * 客户端密钥
         */
        private String clientSecret;
        /**
         * 授权码模式回调地址
         */
        private String callBackUrl;
    }
}
