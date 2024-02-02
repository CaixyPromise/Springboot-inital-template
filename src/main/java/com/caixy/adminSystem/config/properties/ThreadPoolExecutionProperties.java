package com.caixy.adminSystem.config.properties;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池配置解析类
 *
 * @name: com.caixy.adminSystem.config.properties.ThreadPoolExecutionProperties
 * @author: CAIXYPROMISE
 * @since: 2024-01-09 17:33
 **/
@Getter
@Configuration
@ConfigurationProperties(prefix = "thread-pool")
public class ThreadPoolExecutionProperties
{
    private HashMap<String, ThreadInstanceProperties> instances = new HashMap<>();

    public void setInstances(HashMap<String, ThreadInstanceProperties> instances)
    {
        this.instances = instances;
    }

    @Data
    public static class ThreadInstanceProperties
    {
        // 默认核心线程数
        private Integer corePoolSize = 2;
        // 默认最大线程数
        private Integer maxPoolSize = 4;
        // 默认队列容量
        private Integer queueCapacity = 20;
        // 默认队列类型
        private String TaskQueue = "java.util.concurrent.ArrayBlockingQueue";
        // 默认空闲线程存活时间
        private Integer keepAliveSeconds = 60;
        // 默认时间单位
        private String timeUnit = "SECONDS";
        // 默认线程工厂
        private String threadFactory = "DefaultThreadFactory";
        // 默认拒绝策略
        private String rejectionPolicy = "java.util.concurrent.ThreadPoolExecutor.AbortPolicy";
    }


    // 静态方法创建自定义线程工厂
    public static ThreadFactory DefaultThreadFactory(String threadGroupName)
    {
        return new ThreadFactory()
        {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            private final String namePrefix = threadGroupName + "-Thread ";

            @Override
            public Thread newThread(Runnable r)
            {
                return new Thread(r, namePrefix + threadNumber.getAndIncrement());
            }
        };
    }

    public static ThreadFactory createThreadFactory(String threadFactoryName, String threadGroupName)
    {
        try
        {
            if ("DefaultThreadFactory".equals(threadFactoryName))
            {
                return DefaultThreadFactory(threadGroupName);
            }
            else
            {
                return (ThreadFactory) Class.forName(threadFactoryName).newInstance();
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e)
        {
            throw new RuntimeException("Error creating thread factory", e);
        }
    }

}
