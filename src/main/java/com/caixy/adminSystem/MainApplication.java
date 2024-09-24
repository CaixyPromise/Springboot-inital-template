package com.caixy.adminSystem;

import com.caixy.adminSystem.utils.NetUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 主类（项目启动入口）
 */
// todo 如需开启 Redis，elastic search，须移除 exclude 中的内容
@SpringBootApplication(exclude = {
        ElasticsearchDataAutoConfiguration.class,
        ElasticsearchRestClientAutoConfiguration.class,
        ElasticsearchRepositoriesAutoConfiguration.class
})
@MapperScan("com.caixy.adminSystem.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class MainApplication
{
    @Value("${server.port}")
    private Integer port;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    public static void main(String[] args)
    {
        String startTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        String serverHost = NetUtils.getServerHost();
        System.setProperty("app.startup-time", startTime);
        System.setProperty("app.serverHost", serverHost);
        SpringApplication.run(MainApplication.class, args);
    }
}
