package com.caixy.adminSystem.common.web.config;

import com.caixy.adminSystem.common.web.aop.LoginUserInfoInterceptor;
import com.caixy.adminSystem.common.web.resolver.LoginUserInfoParameterResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 全局跨域配置
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer
{
    private final LoginUserInfoParameterResolver loginUserInfoParameterResolver;
    private final LoginUserInfoInterceptor loginUserInfoInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(loginUserInfoInterceptor).addPathPatterns("/**")
                .excludePathPatterns(
                        "/doc.html",
                        "/swagger-ui.html",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/favicon.ico");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers)
    {
        resolvers.add(loginUserInfoParameterResolver);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry)
    {
        // 覆盖所有请求
        registry.addMapping("/**")
                // 允许发送 Cookie
                .allowCredentials(true)
                // 放行哪些域名（必须用 patterns，否则 * 会和 allowCredentials 冲突）
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*");
    }
}
