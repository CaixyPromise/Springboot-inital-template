package com.caixy.adminSystem.manager.ServerManager.domain.properties;

import com.caixy.adminSystem.utils.ArithmeticUtils;
import oshi.hardware.GlobalMemory;

/**
 * 內存相关信息
 */
public class MemInfo
{
    /**
     * 内存总量
     */
    private double total;

    /**
     * 已用内存
     */
    private double used;

    /**
     * 剩余内存
     */
    private double free;

    /**
     * 使用率
     */
    private double usage;

    public double getTotal()
    {
        return ArithmeticUtils.div(total, (1024 * 1024 * 1024), 2);
    }

    public void setTotal(long total)
    {
        this.total = total;
    }

    public double getUsed()
    {
        return ArithmeticUtils.div(used, (1024 * 1024 * 1024), 2);
    }

    public void setUsed(long used)
    {
        this.used = used;
    }

    public double getFree()
    {
        return ArithmeticUtils.div(free, (1024 * 1024 * 1024), 2);
    }

    public void setFree(long free)
    {
        this.free = free;
    }

    public double getUsage()
    {
        return ArithmeticUtils.mul(ArithmeticUtils.div(used, total, 4), 100);
    }

    public static MemInfo copyOf(GlobalMemory memory)
    {
        MemInfo memInfo = new MemInfo();

        memInfo.setTotal(memory.getTotal());
        memInfo.setUsed(memory.getTotal() - memory.getAvailable());
        memInfo.setFree(memory.getAvailable());

        return memInfo;
    }
}
