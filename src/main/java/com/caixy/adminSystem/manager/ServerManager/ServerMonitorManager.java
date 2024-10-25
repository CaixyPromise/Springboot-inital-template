package com.caixy.adminSystem.manager.ServerManager;

import com.caixy.adminSystem.manager.ServerManager.domain.ServerInfo;
import com.caixy.adminSystem.manager.ServerManager.domain.properties.FullSystemInfo;

/**
 * 系统性能监控器
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.ServerManager.ServerMonitorManager
 * @since 2024/10/22 23:03
 */
public interface ServerMonitorManager
{
    ServerInfo getServerInfo();

    FullSystemInfo getFullServerInfo();
}
