package com.github.kfcfans.powerjob.server.akka.actors;

import akka.actor.AbstractActor;
import com.github.kfcfans.powerjob.common.model.SystemMetrics;
import com.github.kfcfans.powerjob.common.response.AskResponse;
import com.github.kfcfans.powerjob.server.akka.requests.FriendQueryWorkerClusterStatusReq;
import com.github.kfcfans.powerjob.server.akka.requests.Ping;
import com.github.kfcfans.powerjob.server.service.ha.WorkerManagerService;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 处理朋友们的信息（处理服务器与服务器之间的通讯）
 *
 * @author tjq
 * @since 2020/4/9
 */
@Slf4j
public class FriendActor extends AbstractActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Ping.class, this::onReceivePing)
                .match(FriendQueryWorkerClusterStatusReq.class, this::onReceiveFriendQueryWorkerClusterStatusReq)
                .matchAny(obj -> log.warn("[FriendActor] receive unknown request: {}.", obj))
                .build();
    }

    /**
     * 处理存活检测的请求
     */
    private void onReceivePing(Ping ping) {
        getSender().tell(AskResponse.succeed(System.currentTimeMillis() - ping.getCurrentTime()), getSelf());
    }

    /**
     * 处理查询Worker节点的请求
     */
    private void onReceiveFriendQueryWorkerClusterStatusReq(FriendQueryWorkerClusterStatusReq req) {
        Map<String, SystemMetrics> workerInfo = WorkerManagerService.getActiveWorkerInfo(req.getAppId());
        AskResponse askResponse = AskResponse.succeed(workerInfo);
        getSender().tell(askResponse, getSelf());
    }
}
