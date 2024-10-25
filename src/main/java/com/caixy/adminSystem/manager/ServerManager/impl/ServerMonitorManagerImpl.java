package com.caixy.adminSystem.manager.ServerManager.impl;

import com.caixy.adminSystem.manager.ServerManager.ServerMonitorManager;
import com.caixy.adminSystem.manager.ServerManager.domain.ServerInfo;
import com.caixy.adminSystem.manager.ServerManager.domain.properties.FullSystemInfo;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

import java.util.Properties;

/**
 * 系统性能监控器实现
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.ServerManager.impl.ServerMonitorManagerImpl
 * @since 2024/10/22 23:09
 */
@Component
public class ServerMonitorManagerImpl implements ServerMonitorManager
{
    private final SystemInfo systemInfo = new SystemInfo();
    private final HardwareAbstractionLayer hal = systemInfo.getHardware();
    private final Properties props = System.getProperties();


    /**
     * 获取系统信息，简略版
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/26 上午12:44
     */
    @Override
    public ServerInfo getServerInfo()
    {
       return new ServerInfo().copyOf(systemInfo, hal, props);
    }

    /**
     * 获取全量系统信息，用于系统性能监控日志
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/26 上午12:43
     */
    @Override
    public FullSystemInfo getFullServerInfo()
    {
       return new FullSystemInfo().copyOf(systemInfo, hal, props);
    }

}
