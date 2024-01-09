package com.caixy.adminSystem.manager;

import com.caixy.adminSystem.config.properties.ThreadPoolExecutionProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.caixy.adminSystem.config.properties.ThreadPoolExecutionProperties.createThreadFactory;

/**
 * 线程池管理器
 *
 * @name: com.caixy.adminSystem.manager.ThreadPoolExecutorManager
 * @author: CAIXYPROMISE
 * @since: 2024-01-09 18:12
 **/
@Service
@Slf4j
public class ThreadPoolExecutorManager
{
    @Resource
    private ThreadPoolExecutionProperties properties;

    public  ThreadPoolExecutor getThreadPoolExecutor(String insName)
    {
        ThreadPoolExecutionProperties.ThreadInstanceProperties
                threadInstanceProperties = properties.getInstances().get(insName);
        return new ThreadPoolExecutor(
                  threadInstanceProperties.getCorePoolSize()
                , threadInstanceProperties.getMaxPoolSize()
                , threadInstanceProperties.getKeepAliveSeconds()
                , createTimeUnit(threadInstanceProperties.getTimeUnit())
                , createBlockingQueue(threadInstanceProperties.getTaskQueue(), threadInstanceProperties.getQueueCapacity())
                , createThreadFactory(threadInstanceProperties.getThreadFactory(), insName)
                , createRejectedExecutionHandler(threadInstanceProperties.getRejectionPolicy()));
    }



    private BlockingQueue<Runnable> createBlockingQueue(String queueName, int queueCapacity)
    {
        try
        {
            return (BlockingQueue<Runnable>) Class.forName(queueName)
                    .getConstructor(int.class)
                    .newInstance(queueCapacity);
        } catch (Exception e)
        {
            throw new RuntimeException("Error creating queue for thread pool", e);
        }
    }

    private RejectedExecutionHandler createRejectedExecutionHandler(String rejectionPolicy)
    {
        try
        {
            return (RejectedExecutionHandler) Class.forName(rejectionPolicy).newInstance();
        } catch (Exception e)
        {
            throw new RuntimeException("Error creating rejected execution handler for thread pool", e);
        }
    }

    private TimeUnit createTimeUnit(String timeUnit)
    {
        return TimeUnit.valueOf(timeUnit.toUpperCase());
    }

}
