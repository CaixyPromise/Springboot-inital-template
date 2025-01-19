package com.caixy.adminSystem.infrastructure.dashboard.manager.ServerManager.domain.properties;

import lombok.Getter;
import lombok.Setter;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 进程信息
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.infrastructure.dashboard.manager.ServerManager.domain.properties.ProcessInfo
 * @since 2024/10/23 00:05
 */
@Setter
@Getter
public class ProcessInfo
{
    public static final int DEFAULT_TOP_N = 5;
    // Getters and Setters
    /**
     * 进程 ID
     */
    private int pid;

    /**
     * 进程 CPU 使用率
     */
    private double cpuUsage;

    /**
     * 进程内存占用 (字节)
     */
    private long memoryUsage;

    /**
     * 前TopN进程占用的信息
     */
    private List<ProcessInfo> topNProcesses;

    /**
     * 获取系统中前 N 个 CPU 使用率最高的进程信息
     *
     * @return 包含前 N 个进程信息的列表
     */
    public static List<ProcessInfo> getTopNProcess(int topN, OperatingSystem os)
    {
        // 获取所有进程信息
        List<OSProcess> processes = os.getProcesses();
        return getTopN(processes, topN);
    }

    /**
     * 全量获取进程信息，包括当前进程信息+TOP N进程信息
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/26 上午12:39
     */
    public static ProcessInfo copyOf(SystemInfo systemInfo)
    {
        OperatingSystem operatingSystem = systemInfo.getOperatingSystem();
        ProcessInfo currentProcessInfo = new ProcessInfo();
        // 当前进程PID
        int currentProcessId = operatingSystem.getProcessId();
        currentProcessInfo.setPid(currentProcessId);
        // 获取所有进程信息
        List<OSProcess> processes = getAllProcesses(systemInfo);
        OSProcess currentProcess = getProcessByPid(processes, currentProcessId);
        if (currentProcess != null)
        {
            currentProcessInfo.setCpuUsage(currentProcess.getProcessCpuLoadCumulative() * 100);
            currentProcessInfo.setMemoryUsage(currentProcess.getResidentSetSize());
        }
        currentProcessInfo.setTopNProcesses(getTopNProcess(DEFAULT_TOP_N, operatingSystem));
        return currentProcessInfo;
    }

    // 获取当前进程信息
    public static OSProcess getProcessByPid(List<OSProcess> processes, int findPid)
    {
        return  processes.stream()
                         .filter(p -> p.getProcessID() == findPid)
                         .findFirst()
                         .orElse(null);
    }

    public static List<ProcessInfo> getTopN(List<OSProcess> processes, int topN)
    {
        // 按 CPU 使用率排序，取前 N 个进程
        return processes.stream()
                        .sorted((p1, p2) -> Double.compare(p2.getProcessCpuLoadCumulative(),
                                p1.getProcessCpuLoadCumulative()))
                        .limit(topN)
                        .map(process -> {
                            ProcessInfo processInfo = new ProcessInfo();
                            processInfo.setPid(process.getProcessID());
                            processInfo.setCpuUsage(process.getProcessCpuLoadCumulative() * 100);
                            processInfo.setMemoryUsage(process.getResidentSetSize());
                            return processInfo;
                        })
                        .collect(Collectors.toList());
    }

    public static List<OSProcess> getAllProcesses(SystemInfo systemInfo)
    {
        return systemInfo.getOperatingSystem().getProcesses();
    }
}
