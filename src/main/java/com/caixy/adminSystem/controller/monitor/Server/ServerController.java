package com.caixy.adminSystem.controller.monitor.Server;

import com.caixy.adminSystem.common.BaseResponse;
import com.caixy.adminSystem.common.ResultUtils;
import com.caixy.adminSystem.manager.ServerManager.ServerMonitorManager;
import com.caixy.adminSystem.manager.ServerManager.domain.ServerInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 服务接口控制器
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.controller.monitor.Server.ServerController
 * @since 2024/10/20 02:06
 */
@RestController
@RequestMapping("/monitor/server")
public class ServerController
{
    @Resource
    private ServerMonitorManager serverMonitorManager;

    @GetMapping("/")
    public BaseResponse<ServerInfo> getMonitorInfo()
    {
        return ResultUtils.success(serverMonitorManager.getServerInfo());
    }
}
