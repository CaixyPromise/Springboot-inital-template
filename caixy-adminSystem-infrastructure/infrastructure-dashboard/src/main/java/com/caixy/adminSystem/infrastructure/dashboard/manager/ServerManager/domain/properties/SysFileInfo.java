package com.caixy.adminSystem.infrastructure.dashboard.manager.ServerManager.domain.properties;

import com.caixy.adminSystem.common.base.utils.ArithmeticUtils;
import oshi.SystemInfo;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统文件相关信息
 * 
 *
 */
public class SysFileInfo
{
    /**
     * 盘符路径
     */
    private String dirName;

    /**
     * 盘符类型
     */
    private String sysTypeName;

    /**
     * 文件类型
     */
    private String typeName;

    /**
     * 总大小
     */
    private String total;

    /**
     * 剩余大小
     */
    private String free;

    /**
     * 已经使用量
     */
    private String used;

    /**
     * 资源的使用率
     */
    private double usage;

    public String getDirName()
    {
        return dirName;
    }

    public void setDirName(String dirName)
    {
        this.dirName = dirName;
    }

    public String getSysTypeName()
    {
        return sysTypeName;
    }

    public void setSysTypeName(String sysTypeName)
    {
        this.sysTypeName = sysTypeName;
    }

    public String getTypeName()
    {
        return typeName;
    }

    public void setTypeName(String typeName)
    {
        this.typeName = typeName;
    }

    public String getTotal()
    {
        return total;
    }

    public void setTotal(String total)
    {
        this.total = total;
    }

    public String getFree()
    {
        return free;
    }

    public void setFree(String free)
    {
        this.free = free;
    }

    public String getUsed()
    {
        return used;
    }

    public void setUsed(String used)
    {
        this.used = used;
    }

    public double getUsage()
    {
        return usage;
    }

    public void setUsage(double usage)
    {
        this.usage = usage;
    }

    public static List<SysFileInfo> copyOf(SystemInfo systemInfo)
    {
        FileSystem fileSystem = systemInfo.getOperatingSystem().getFileSystem();
        List<OSFileStore> fileStores = fileSystem.getFileStores();

        return fileStores.stream().map(fileStore -> {
            SysFileInfo sysFileInfo = new SysFileInfo();
            long free = fileStore.getUsableSpace();
            long total = fileStore.getTotalSpace();
            long used = total - free;
            sysFileInfo.setDirName(fileStore.getMount());
            sysFileInfo.setSysTypeName(fileStore.getType());
            sysFileInfo.setTypeName(fileStore.getName());
            sysFileInfo.setTotal(convertFileSize(total));
            sysFileInfo.setFree(convertFileSize(free));
            sysFileInfo.setUsed(convertFileSize(used));
            sysFileInfo.setUsage(ArithmeticUtils.mul(ArithmeticUtils.div(used, total, 4), 100));
            return sysFileInfo;
        }).collect(Collectors.toList());
    }

    /**
     * 字节转换
     *
     * @param size 字节大小
     * @return 转换后值
     */
    private static String convertFileSize(long size)
    {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        if (size >= gb)
        {
            return String.format("%.1f GB", (float) size / gb);
        }
        else if (size >= mb)
        {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        }
        else if (size >= kb)
        {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        }
        else
        {
            return String.format("%d B", size);
        }
    }
}
