package com.caixy.adminSystem.manager.ServerManager.domain.properties;

/**
 * 系统负载信息
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.ServerManager.domain.properties.SystemLoadInfo
 * @since 2024/10/23 00:15
 */

import lombok.Getter;
import lombok.Setter;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * 系统负载相关信息
 */
@Getter
@Setter
public class SystemLoadInfo {

    // Getters and Setters
    /**
     * 系统 1 分钟的负载平均值
     */
    private double loadAverage;

    /**
     * 获取系统的负载平均值
     *
     * @return 系统负载信息
     */
    public static SystemLoadInfo copyOf()
    {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        SystemLoadInfo systemLoadInfo = new SystemLoadInfo();
        systemLoadInfo.setLoadAverage(osBean.getSystemLoadAverage());
        return systemLoadInfo;
    }
}