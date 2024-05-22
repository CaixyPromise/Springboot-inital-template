package com.caixy.adminSystem.config;

import com.caixy.adminSystem.model.enums.FileUploadBizEnum;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * 全局跨域配置
 */
@Configuration
@Slf4j
public class CorsConfig implements WebMvcConfigurer
{
    @Resource
    private LocalFileConfig localFileConfig;

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

    @Override
    public void addResourceHandlers(@NotNull ResourceHandlerRegistry registry)
    {
        for (FileUploadBizEnum bizEnum : FileUploadBizEnum.values())
        {
            String pathPattern = localFileConfig.getStaticPath() + "/" + bizEnum.getRoutePath() + "/**";
            String location =
                    "file:///" + localFileConfig.getRootLocation().toString().replace("\\", "/") + "/" + bizEnum.getValue() + "/";
            log.info("AddResourceHandlers: {} -> {}", pathPattern, location);
            registry.addResourceHandler(pathPattern).addResourceLocations(location);
        }
    }
}
