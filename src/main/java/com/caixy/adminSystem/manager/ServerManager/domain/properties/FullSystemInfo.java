package com.caixy.adminSystem.manager.ServerManager.domain.properties;

import com.caixy.adminSystem.manager.ServerManager.domain.ServerInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

import java.util.List;
import java.util.Properties;

/**
 * 完整的系统信息
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.ServerManager.domain.properties.FullSystemInfo
 * @since 2024/10/26 00:16
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FullSystemInfo extends ServerInfo
{
    /**
     * 网络信息
     */
    private List<NetworkInfo> networkInfos;

    /**
     * 磁盘 I/O 信息
     */
    private List<DiskIOInfo> diskIOInfos;

    /**
     * 系统文件信息
     */
    private List<SysFileInfo> sysFiles;

    /**
     * 系统进程信息
     */
    private List<ProcessInfo> processInfos;

    @Override
    public FullSystemInfo copyOf(SystemInfo systemInfo, HardwareAbstractionLayer hal, Properties props) {
        super.copyOf(systemInfo, hal, props);
        setNetworkInfos(NetworkInfo.copyOf(hal));
        setDiskIOInfos(DiskIOInfo.copyOf(systemInfo));
        setSystemLoadInfo(SystemLoadInfo.copyOf());
        setSysFiles(SysFileInfo.copyOf(systemInfo));
        setProcessInfos(ProcessInfo.getTopN(ProcessInfo.getAllProcesses(systemInfo), ProcessInfo.DEFAULT_TOP_N));
        return this;
    }
}
