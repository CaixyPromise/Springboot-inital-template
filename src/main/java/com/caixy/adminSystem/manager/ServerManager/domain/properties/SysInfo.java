package com.caixy.adminSystem.manager.ServerManager.domain.properties;

import com.caixy.adminSystem.utils.NetUtils;

import java.util.Properties;

/**
 * 系统相关信息
 * 
 * 
 */
public class SysInfo
{
    /**
     * 服务器名称
     */
    private String computerName;

    /**
     * 服务器Ip
     */
    private String computerIp;

    /**
     * 项目路径
     */
    private String userDir;

    /**
     * 操作系统
     */
    private String osName;

    /**
     * 系统架构
     */
    private String osArch;

    public String getComputerName()
    {
        return computerName;
    }

    public void setComputerName(String computerName)
    {
        this.computerName = computerName;
    }

    public String getComputerIp()
    {
        return computerIp;
    }

    public void setComputerIp(String computerIp)
    {
        this.computerIp = computerIp;
    }

    public String getUserDir()
    {
        return userDir;
    }

    public void setUserDir(String userDir)
    {
        this.userDir = userDir;
    }

    public String getOsName()
    {
        return osName;
    }

    public void setOsName(String osName)
    {
        this.osName = osName;
    }

    public String getOsArch()
    {
        return osArch;
    }

    public void setOsArch(String osArch)
    {
        this.osArch = osArch;
    }

    public static SysInfo copyOf(Properties props) {
        SysInfo sysInfo = new SysInfo();

        sysInfo.setComputerName(NetUtils.getHostName());
        sysInfo.setComputerIp(NetUtils.getHostIp());
        sysInfo.setOsName(props.getProperty("os.name"));
        sysInfo.setOsArch(props.getProperty("os.arch"));
        sysInfo.setUserDir(props.getProperty("user.dir"));

        return sysInfo;
    }
}
