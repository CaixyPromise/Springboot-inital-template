package com.caixy.adminSystem.manager.ServerManager.domain.properties;

import com.caixy.adminSystem.utils.ArithmeticUtils;
import lombok.Getter;
import lombok.Setter;
import oshi.hardware.CentralProcessor;
import oshi.util.Util;

/**
 * CPU相关信息
 */
@Getter
@Setter
public class CpuInfo {
    /**
     * 核心数
     */
    private int cpuNum;

    // 获取系统使用率
    /**
     * CPU系统使用率
     */
    private double sysUsageRate;

    // 获取用户使用率
    /**
     * CPU用户使用率
     */
    private double userUsageRate;

    // 获取等待率
    /**
     * CPU当前等待率
     */
    private double waitRate;

    // 获取空闲率
    /**
     * CPU当前空闲率
     */
    private double freeRate;

    /**
     * CPU名称
     */
    private String cpuName;

    // 获取整体 CPU 使用率
    @Setter
    private double totalUsageRate;  // 整体 CPU 使用率

    public static CpuInfo copyOf(CentralProcessor processor) {
        CpuInfo cpuInfo = new CpuInfo();

        // CPU信息
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(1000);  // 采样等待时间
        long[] ticks = processor.getSystemCpuLoadTicks();

        // 各个tick类型的增量计算
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long system = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];

        // 总的 CPU 时间
        long totalCpu = user + nice + system + idle + iowait + irq + softirq + steal;

        // 设置 CPU 信息
        cpuInfo.setCpuNum(processor.getLogicalProcessorCount());
        cpuInfo.setCpuName(processor.getProcessorIdentifier().getName());

        // 计算各个使用率（百分比）
        cpuInfo.setSysUsageRate(ArithmeticUtils.round((double) system / totalCpu * 100, 2));
        cpuInfo.setUserUsageRate(ArithmeticUtils.round((double) user / totalCpu * 100, 2));
        cpuInfo.setWaitRate(ArithmeticUtils.round((double) iowait / totalCpu * 100, 2));
        cpuInfo.setFreeRate(ArithmeticUtils.round((double) idle / totalCpu * 100, 2));

        // 计算整体 CPU 使用率（不包括 idle 和 iowait）
        long activeTime = totalCpu - idle - iowait;
        double totalUsageRate = ArithmeticUtils.round((double) activeTime / totalCpu * 100, 2);
        cpuInfo.setTotalUsageRate(totalUsageRate);

        return cpuInfo;
    }
}
