package com.caixy.adminSystem.infrastructure.dashboard.manager.ServerManager.domain.properties;

import lombok.Getter;
import lombok.Setter;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;


import java.util.List;
import java.util.stream.Collectors;

/**
 * 磁盘 I/O 信息类
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.infrastructure.dashboard.manager.ServerManager.domain.properties.DiskIOInfo
 * @since 2024/10/23 00:16
 */
@Setter
@Getter
public class DiskIOInfo
{

    // Getters and Setters
    /**
     * 磁盘读取字节数
     */
    private long readBytes;

    /**
     * 磁盘写入字节数
     */
    private long writeBytes;

    /**
     * 获取磁盘 I/O 性能信息
     *
     * @return 磁盘 I/O 信息的列表
     */
    public static List<DiskIOInfo> copyOf(SystemInfo systemInfo)
    {
        List<HWDiskStore> diskStores = systemInfo.getHardware().getDiskStores();

        return diskStores.stream().map(disk -> {
            DiskIOInfo diskIOInfo = new DiskIOInfo();
            diskIOInfo.setReadBytes(disk.getReadBytes());
            diskIOInfo.setWriteBytes(disk.getWriteBytes());
            return diskIOInfo;
        }).collect(Collectors.toList());
    }
}
