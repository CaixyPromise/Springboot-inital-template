package com.caixy.adminSystem.infrastructure.threadpool;

import com.caixy.adminSystem.common.base.utils.SpringContextUtils;
import com.caixy.adminSystem.common.base.utils.ThreadUtils;
import com.caixy.adminSystem.infrastructure.threadpool.factory.AsyncTaskFactory;

import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 异步任务管理器，任务工厂参见{@link AsyncTaskFactory}
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.ThreadPoolManager.AsyncManager
 * @since 2024/10/26 01:23
 */
public class AsyncManager
{
    /**
     * 操作延迟10毫秒
     */
    private final int OPERATE_DELAY_TIME = 10;

    /**
     * 异步操作任务调度线程池
     */
    private final ScheduledExecutorService executor = SpringContextUtils.getBean("scheduledExecutorService", ScheduledExecutorService.class);

    /**
     * 单例模式
     */
    private AsyncManager(){}

    private static final AsyncManager me = new AsyncManager();

    public static AsyncManager me()
    {
        return me;
    }

    /**
     * 执行任务
     *
     * @param task 任务
     */
    public void execute(TimerTask task)
    {
        executor.schedule(task, OPERATE_DELAY_TIME, TimeUnit.MILLISECONDS);
    }

    /**
     * 停止任务线程池
     */
    public void shutdown()
    {
        ThreadUtils.shutdownAndAwaitTermination(executor);
    }
}