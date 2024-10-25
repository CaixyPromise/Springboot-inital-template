package com.caixy.adminSystem.manager.ServerManager.domain;

import com.caixy.adminSystem.manager.ServerManager.domain.properties.*;
import lombok.Data;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

import java.io.Serializable;
import java.util.List;
import java.util.Properties;

/**
 * 服务信息实体类
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.ServerManager.domain.ServerInfo
 * @since 2024/10/22 23:06
 */
@Data
public class ServerInfo implements Serializable
{
    /**
     * cpu信息
     */
    private CpuInfo cpu;
    /**
     * 内存信息
     */
    private MemInfo mem;
    /**
     * jvm信息
     */
    private JvmInfo jvm;
    /**
     * 操作系统信息
     */
    private SysInfo sys;
    /**
     * 系统文件信息
     */
    private List<SysFileInfo> sysFiles;
    /**
     * 系统负载信息
     */
    private SystemLoadInfo systemLoadInfo;

    public ServerInfo copyOf(SystemInfo systemInfo, HardwareAbstractionLayer hal, Properties props)
    {
        setCpu(CpuInfo.copyOf(hal.getProcessor()));
        setMem(MemInfo.copyOf(hal.getMemory()));
        setJvm(JvmInfo.copyOf(props));
        setSys(SysInfo.copyOf(props));
        setSysFiles(SysFileInfo.copyOf(systemInfo));
        setSystemLoadInfo(SystemLoadInfo.copyOf());
        return this;
    }
}
