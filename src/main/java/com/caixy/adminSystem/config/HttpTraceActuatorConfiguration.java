package com.caixy.adminSystem.config;

import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Http流量配置
 *
 * @Author COMPROMISE
 * @name com.caixy.adminSystem.config.HttpTraceActuatorConfiguration
 * @since 2024/8/5 上午2:44
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