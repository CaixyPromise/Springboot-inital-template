package com.caixy.adminSystem.config;

import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * HTTP流量配置
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.config.HttpTraceActuatorConfiguration
 * @since 2024/10/31 22:13
 */
@Configuration
public class HttpTraceActuatorConfiguration
{
    @Bean
    public HttpTraceRepository httpTraceRepository()
    {
        return new InMemoryHttpTraceRepository();
    }
}