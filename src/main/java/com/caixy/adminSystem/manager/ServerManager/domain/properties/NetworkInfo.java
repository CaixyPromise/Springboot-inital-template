package com.caixy.adminSystem.manager.ServerManager.domain.properties;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 网络信息
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.ServerManager.domain.properties.NetworkInfo
 * @since 2024/10/22 23:31
 */
@Setter
@Getter
@Data
public class NetworkInfo
{

    /**
     * 网络接口名称
     */
    private String name;

    /**
     * 接收的字节数
     */
    private long bytesRecv;

    /**
     * 发送的字节数
     */
    private long bytesSent;

    /**
     * 接收的包数
     */
    private long packetsRecv;

    /**
     * 发送的包数
     */
    private long packetsSent;

    /**
     * 接收和发送的错误数
     */
    private long errors;

    /**
     * 通过 OSHI 获取所有网络接口信息
     *
     * @return 包含所有网络接口信息的列表
     */
    public static List<NetworkInfo> copyOf(HardwareAbstractionLayer hal)
    {
        List<NetworkIF> networkIFs = hal.getNetworkIFs();
        return networkIFs.stream().map(net -> {
            NetworkInfo networkInfo = new NetworkInfo();
            networkInfo.setName(net.getDisplayName());
            networkInfo.setBytesRecv(net.getBytesRecv());
            networkInfo.setBytesSent(net.getBytesSent());
            networkInfo.setPacketsRecv(net.getPacketsRecv());
            networkInfo.setPacketsSent(net.getPacketsSent());
            networkInfo.setErrors(net.getInErrors() + net.getOutErrors());
            return networkInfo;
        }).collect(Collectors.toList());
    }
}
