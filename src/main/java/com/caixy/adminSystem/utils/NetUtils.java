package com.caixy.adminSystem.utils;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 网络工具类
 */
public class NetUtils
{
    public final static String REGX_0_255 = "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)";
    // 匹配 ip
    public final static String REGX_IP = "((" + REGX_0_255 + "\\.){3}" + REGX_0_255 + ")";
    public final static String REGX_IP_WILDCARD = "(((\\*\\.){3}\\*)|(" + REGX_0_255 + "(\\.\\*){3})|(" + REGX_0_255 + "\\." + REGX_0_255 + ")(\\.\\*){2}" + "|((" + REGX_0_255 + "\\.){3}\\*))";
    // 匹配网段
    public final static String REGX_IP_SEG = "(" + REGX_IP + "\\-" + REGX_IP + ")";
    /**
     * 获取客户端 IP 地址
     *
     * @param request
     * @return
     */
    public static String getIpAddress(HttpServletRequest request)
    {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getRemoteAddr();
            if (ip.equals("127.0.0.1"))
            {
                // 根据网卡取本机配置的 IP
                InetAddress inet = null;
                try
                {
                    inet = InetAddress.getLocalHost();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                if (inet != null)
                {
                    ip = inet.getHostAddress();
                }
            }
        }
        // 多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ip != null && ip.length() > 15)
        {
            if (ip.indexOf(",") > 0)
            {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        if (ip == null)
        {
            return "127.0.0.1";
        }
        return ip;
    }

    /**
     * 获取绝对本机的网络路径
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/5/21 下午8:35
     */
    public static String getAbsoluteHost(HttpServletRequest request)
    {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
    }

    /**
     * 获取服务的 Host 地址
     *
     * @return 本机 IP 地址或主机名
     */
    public static String getHostIp()
    {
        String host = "127.0.0.1";
        try
        {
            InetAddress inet = InetAddress.getLocalHost();
            host = inet.getHostAddress();  // 可以使用 inet.getHostName() 获取主机名
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return host;
    }

    /**
     * 获取主机名
     *
     * @return 本地主机名
     */
    public static String getHostName()
    {
        try
        {
            return InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        return "未知";
    }
}
