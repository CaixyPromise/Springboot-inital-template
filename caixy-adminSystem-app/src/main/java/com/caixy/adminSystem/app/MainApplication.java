package com.caixy.adminSystem.app;

import com.caixy.adminSystem.common.base.utils.NetUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 系统根服务应用
 *
 * @Author CAIXYPROMISE
 */
@SpringBootApplication(
    scanBasePackages = {
            "com.caixy.adminSystem",
    }
)
@MapperScan("com.caixy.adminSystem.business.*.domain.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class MainApplication
{
    public static void main(String[] args)
    {
        String startTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        String serverHost = NetUtils.getHostIp();
        System.setProperty("app.startup-time", startTime);
        System.setProperty("mainClass.basePackage", MainApplication.class.getPackage().getName());
        System.setProperty("mainClass.className", MainApplication.class.getName());
        System.setProperty("app.serverHost", serverHost);
        SpringApplication.run(MainApplication.class, args);
    }
}
