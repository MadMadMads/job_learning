package com.github.kfcfans.powerjob.common.model;

import com.github.kfcfans.powerjob.common.OmsSerializable;
import lombok.Data;

/**
 * 系统指标
 *
 * @author tjq
 * @since 2020/3/25
 */
@Data
public class SystemMetrics implements OmsSerializable, Comparable<SystemMetrics> {

    // CPU核心数量
    private int cpuProcessors;
    // CPU负载（需要处以核心数）
    private double cpuLoad;

    // 内存（单位 GB）
    private double jvmUsedMemory;
    private double jvmMaxMemory;
    // 内存占用（0.X，非百分比）
    private double jvmMemoryUsage;

    // 磁盘（单位 GB）
    private double diskUsed;
    private double diskTotal;
    // 磁盘占用（0.X，非百分比）
    private double diskUsage;

    // 缓存分数
    private int score;

    @Override
    public int compareTo(SystemMetrics that) {
        return this.calculateScore() - that.calculateScore();
    }

    /**
     * 计算得分情况，内存 then CPU then 磁盘（磁盘必须有1G以上的剩余空间）
     * @return 得分情况
     */
    public int calculateScore() {

        if (score > 0) {
            return score;
        }

        double availableCPUCores = cpuProcessors * cpuLoad;
        double availableMemory = jvmMaxMemory - jvmUsedMemory;

        // Windows下无法获取CPU可用核心数，值固定为-1
        cpuLoad = Math.max(0, cpuLoad);

        return (int) (availableMemory * 2 + availableCPUCores);
    }

    /**
     * 该机器是否可用
     * @param minCPUCores 判断标准之最低可用CPU核心数量
     * @param minMemorySpace 判断标准之最低可用内存
     * @param minDiskSpace 判断标准之最低可用磁盘空间
     * @return 是否可用
     */
    public boolean available(double minCPUCores, double minMemorySpace, double minDiskSpace) {

        double currentCpuCores = Math.max(cpuLoad * cpuProcessors, 0);
        double currentMemory = jvmMaxMemory - jvmUsedMemory;
        double currentDisk = diskTotal - diskUsed;
        return currentCpuCores >= minCPUCores && currentMemory >= minMemorySpace && currentDisk >= minDiskSpace;
    }
}
