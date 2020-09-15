package com.github.kfcfans.powerjob.worker.core.ha;

import com.github.kfcfans.powerjob.worker.pojo.request.ProcessorTrackerStatusReportReq;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * 统一管理 ProcessorTracker 的状态
 *
 * @author tjq
 * @since 2020/3/28
 */
public class ProcessorTrackerStatusHolder {

    // ProcessorTracker的address(IP:Port) -> 状态
    private final Map<String, ProcessorTrackerStatus> address2Status;

    public ProcessorTrackerStatusHolder(List<String> allWorkerAddress) {

        address2Status = Maps.newConcurrentMap();
        allWorkerAddress.forEach(address -> {
            ProcessorTrackerStatus pts = new ProcessorTrackerStatus();
            pts.init(address);
            address2Status.put(address, pts);
        });
    }

    public ProcessorTrackerStatus getProcessorTrackerStatus(String address) {
        return address2Status.get(address);
    }

    /**
     * 根据 ProcessorTracker 的心跳更新状态
     */
    public void updateStatus(ProcessorTrackerStatusReportReq heartbeatReq) {
        ProcessorTrackerStatus processorTrackerStatus = address2Status.get(heartbeatReq.getAddress());
        processorTrackerStatus.update(heartbeatReq);
    }

    /**
     * 获取可用 ProcessorTracker 的IP地址
     */
    public List<String> getAvailableProcessorTrackers() {

        List<String> result = Lists.newLinkedList();
        address2Status.forEach((address, ptStatus) -> {
            if (ptStatus.available()) {
                result.add(address);
            }
        });
        return result;
    }

    /**
     * 获取所有 ProcessorTracker 的IP地址（包括不可用状态）
     */
    public List<String> getAllProcessorTrackers() {
        return Lists.newArrayList(address2Status.keySet());
    }

    /**
     * 获取所有失联 ProcessorTracker 的IP地址
     */
    public List<String> getAllDisconnectedProcessorTrackers() {

        List<String> result = Lists.newLinkedList();
        address2Status.forEach((ip, ptStatus) -> {
            if (ptStatus.isTimeout()) {
                result.add(ip);
            }
        });
        return result;
    }
}
