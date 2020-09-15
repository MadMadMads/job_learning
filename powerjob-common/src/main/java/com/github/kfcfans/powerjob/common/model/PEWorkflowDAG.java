package com.github.kfcfans.powerjob.common.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Point & Edge DAG 表示法
 * 点 + 线，易于表达和传播
 *
 * @author tjq
 * @since 2020/5/26
 */
@Data
@NoArgsConstructor
public class PEWorkflowDAG {

    // DAG 图（点线表示法）
    private List<Node> nodes;
    private List<Edge> edges;

    // 点
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Node {
        private Long jobId;
        private String jobName;

        // 运行时参数，图定义不需要
        @JsonSerialize(using= ToStringSerializer.class)
        private Long instanceId;
        private Integer status;
        private String result;

        public Node(Long jobId, String jobName) {
            this.jobId = jobId;
            this.jobName = jobName;
        }
    }

    // 边 jobId -> jobId
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Edge {
        private Long from;
        private Long to;
    }

    public PEWorkflowDAG(@Nonnull List<Node> nodes, @Nullable List<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges == null ? Lists.newLinkedList() : edges;
    }
}
