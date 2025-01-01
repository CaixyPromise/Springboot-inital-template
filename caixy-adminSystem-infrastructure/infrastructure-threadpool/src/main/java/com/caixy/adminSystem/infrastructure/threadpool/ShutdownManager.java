package com.caixy.adminSystem.infrastructure.threadpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * 关闭线程池管理器
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.ThreadPoolManager.ShutdownManager
 * @since 2024/10/26 01:30
 */
@Component
public class ShutdownManager
{
    private static final Logger logger = LoggerFactory.getLogger("ThreadPool-Manager");

    @PreDestroy
    public void destroy()
    {
        shutdownAsyncManager();
        logger.info("====关闭任务队列线程池====");
        TaskQueueManager.getInstance().shutdown();
    }

    /**
     * 停止异步执行任务
     */
    private void shutdownAsyncManager()
    {
        try
        {
            logger.info("====关闭后台任务任务线程池====");
            AsyncManager.me().shutdown();
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
    }
}
