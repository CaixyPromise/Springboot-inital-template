package com.caixy.adminSystem.infrastructure.threadpool;


import com.caixy.adminSystem.infrastructure.threadpool.factory.TaskQueueTaskFactory;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

/**
 * 任务队列管理器，基于ForkJoinPool，任务工厂参见 {@link TaskQueueTaskFactory}
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.ThreadPoolManager.TaskQueueManager
 * @since 2024/10/26 01:46
 */
public class TaskQueueManager
{
    private static final TaskQueueManager INSTANCE = new TaskQueueManager();

    /**
     * 任务队列数量
     */
    private static final int queueCount = 2;

    private final ForkJoinPool forkJoinPool;

    private TaskQueueManager()
    {
        this.forkJoinPool = new ForkJoinPool(queueCount);
    }

    public static TaskQueueManager getInstance()
    {
        return INSTANCE;
    }

    public void submitTask(Runnable task)
    {
        ForkJoinTask<?> forkJoinTask = ForkJoinTask.adapt(task);
        forkJoinPool.execute(forkJoinTask);
    }

    public void submitRecursiveTask(RecursiveAction task)
    {
        forkJoinPool.execute(task);
    }

    public void shutdown()
    {
        forkJoinPool.shutdown();
        awaitTermination();
    }

    /**
     * 等待 ForkJoinPool 的所有任务完成，避免任务丢失或不完整的情况
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/26 上午1:52
     */
    public void awaitTermination()
    {
        try {
            while (!forkJoinPool.isTerminated())
            {
                boolean awaitTermination = forkJoinPool.awaitTermination(1, TimeUnit.SECONDS);
                if (awaitTermination)
                {
                    break;
                }
            }
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            System.err.println("等待线程池任务完成时发生中断: " + e.getMessage());
        }
    }
}
