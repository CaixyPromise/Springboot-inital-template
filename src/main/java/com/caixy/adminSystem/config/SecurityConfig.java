package com.caixy.adminSystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import javax.annotation.Resource;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig
{
    @Resource
    private Environment env;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception
    {
        if (Arrays.asList(env.getActiveProfiles()).contains("prod")) {
            // 生产环境限定
            String innerIp = "172.18.0.0/16"; // docker配置
            http.csrf(AbstractHttpConfigurer::disable)
                .authorizeRequests(authz -> authz
                        // 生产环境保护actuator端点
                        .antMatchers("/actuator/**").hasIpAddress(innerIp)
                        .anyRequest().permitAll()
                );
        } else {
            // 开发环境完全开放
            http.csrf(AbstractHttpConfigurer::disable)
                .authorizeRequests(authz -> authz
                        .anyRequest().permitAll()
                );
        }

        return http.build();
    }
}
