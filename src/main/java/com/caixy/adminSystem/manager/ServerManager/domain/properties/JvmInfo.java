package com.caixy.adminSystem.manager.ServerManager.domain.properties;


import com.caixy.adminSystem.utils.ArithmeticUtils;
import com.caixy.adminSystem.utils.DateUtils;
import lombok.Getter;
import lombok.Setter;

import java.lang.management.*;
import java.util.List;
import java.util.Properties;


/**
 * JVM相关信息
 */
@Setter
public class JvmInfo
{
    /**
     * 当前JVM占用的内存总数(M)
     */
    private double currentUsageMem;

    /**
     * JVM最大可用内存总数(M)
     */
    private double maxMemSize;

    /**
     * JVM空闲内存(M)
     */
    private double free;

    /**
     * 已使用的堆内存(M)
     */

    private double usedHeapMemory;

    /**
     * 非堆内存(M)
     */
    private double nonHeapMemory;

    /**
     * 垃圾回收次数
     *  获取垃圾回收次数
     */
    @Getter
    private long gcCount;

    /**
     * 垃圾回收总时间(ms)
     */
    @Getter
    private long gcTime;

    /**
     * 当前加载的类数量
     */
    @Getter
    private int loadedClassCount;

    /**
     * 总共加载的类数量
     */
    @Getter
    private long totalLoadedClassCount;

    /**
     * 已卸载的类数量
     */
    @Getter
    private long unloadedClassCount;

    /**
     * 当前活跃线程数
     */
    @Getter
    private int threadCount;

    /**
     * 峰值线程数
     */
    @Getter
    private int peakThreadCount;

    /**
     * 总启动线程数
     */
    @Getter
    private long totalStartedThreadCount;
   
    /**
     * JDK版本
     */
    @Getter
    private String version;

    /**
     * JDK路径
     */
    @Getter
    private String home;



    public double getCurrentUsageMem()
    {
        return ArithmeticUtils.div(currentUsageMem, (1024 * 1024), 2);
    }

    public double getMaxMemSize()
    {
        return ArithmeticUtils.div(maxMemSize, (1024 * 1024), 2);
    }

    public double getFree()
    {
        return ArithmeticUtils.div(free, (1024 * 1024), 2);
    }

    public double getUsed()
    {
        return ArithmeticUtils.div(currentUsageMem - free, (1024 * 1024), 2);
    }

    public double getUsage()
    {
        return ArithmeticUtils.mul(ArithmeticUtils.div(currentUsageMem - free, currentUsageMem, 4), 100);
    }

    /**
     * 获取JDK名称
     */
    public String getName()
    {
        return ManagementFactory.getRuntimeMXBean().getVmName();
    }

    /**
     * JDK启动时间
     */
    public String getStartTime()
    {
        return DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, DateUtils.getServerStartDate());
    }

    /**
     * JDK运行时间
     */
    public String getRunTime()
    {
        return DateUtils.timeDistance(DateUtils.getNowDate(), DateUtils.getServerStartDate());
    }

    /**
     * 运行参数
     */
    public String getInputArgs()
    {
        return ManagementFactory.getRuntimeMXBean().getInputArguments().toString();
    }

    /**
     * 设置内存信息
     */
    public void setMemoryInfo() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();

        this.currentUsageMem = heapMemoryUsage.getCommitted(); // 已提交内存
        this.maxMemSize = heapMemoryUsage.getMax(); // 最大堆内存
        this.free = heapMemoryUsage.getCommitted() - heapMemoryUsage.getUsed(); // 空闲堆内存
        this.usedHeapMemory = heapMemoryUsage.getUsed(); // 已使用堆内存
        this.nonHeapMemory = nonHeapMemoryUsage.getUsed(); // 非堆内存
    }

    /**
     * 设置垃圾收集信息
     */
    public void setGCInfo() {
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        long totalGcCount = 0;
        long totalGcTime = 0;
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            totalGcCount += gcBean.getCollectionCount();
            totalGcTime += gcBean.getCollectionTime();
        }
        this.gcCount = totalGcCount;
        this.gcTime = totalGcTime;
    }

    /**
     * 设置线程信息
     */
    public void setThreadInfo() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        this.threadCount = threadMXBean.getThreadCount();
        this.peakThreadCount = threadMXBean.getPeakThreadCount();
        this.totalStartedThreadCount = threadMXBean.getTotalStartedThreadCount();
    }

    /**
     * 设置类加载信息
     */
    public void setClassLoadingInfo() {
        ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
        this.loadedClassCount = classLoadingMXBean.getLoadedClassCount();
        this.totalLoadedClassCount = classLoadingMXBean.getTotalLoadedClassCount();
        this.unloadedClassCount = classLoadingMXBean.getUnloadedClassCount();
    }

    /**
     * 获取已使用的堆内存 (M)
     */
    public double getUsedHeapMemory() {
        return ArithmeticUtils.div(usedHeapMemory, (1024 * 1024), 2);
    }

    /**
     * 获取非堆内存大小 (M)
     */
    public double getNonHeapMemory() {
        return ArithmeticUtils.div(nonHeapMemory, (1024 * 1024), 2);
    }

    public static JvmInfo copyOf(Properties props) {
        JvmInfo jvmInfo = new JvmInfo();
        jvmInfo.setCurrentUsageMem(Runtime.getRuntime().totalMemory());
        jvmInfo.setMaxMemSize(Runtime.getRuntime().maxMemory());
        jvmInfo.setFree(Runtime.getRuntime().freeMemory());
        jvmInfo.setVersion(props.getProperty("java.version"));
        jvmInfo.setHome(props.getProperty("java.home"));
        jvmInfo.setMemoryInfo();    // 获取堆内存和非堆内存的详细信息
        jvmInfo.setGCInfo();        // 获取垃圾收集信息
        jvmInfo.setThreadInfo();     // 获取线程信息
        jvmInfo.setClassLoadingInfo(); // 获取类加载信息
        return jvmInfo;
    }
}
