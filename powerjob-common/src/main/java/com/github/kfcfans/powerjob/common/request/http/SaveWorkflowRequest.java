package com.github.kfcfans.powerjob.common.request.http;

import com.github.kfcfans.powerjob.common.TimeExpressionType;
import com.github.kfcfans.powerjob.common.model.PEWorkflowDAG;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * 创建/修改 Workflow 请求
 *
 * @author tjq
 * @since 2020/5/26
 */
@Data
public class SaveWorkflowRequest {

    private Long id;

    // 工作流名称
    private String wfName;
    // 工作流描述
    private String wfDescription;

    // 所属应用ID（OpenClient不需要用户填写，自动填充）
    private Long appId;

    // 点线表示法
    private PEWorkflowDAG pEWorkflowDAG;

    /* ************************** 定时参数 ************************** */
    // 时间表达式类型，仅支持 CRON 和 API
    private TimeExpressionType timeExpressionType;
    // 时间表达式，CRON/NULL/LONG/LONG
    private String timeExpression;

    // 最大同时运行的工作流个数，默认 1
    private Integer maxWfInstanceNum = 1;

    // ENABLE / DISABLE
    private boolean enable = true;

    // 工作流整体失败的报警
    private List<Long> notifyUserIds = Lists.newLinkedList();
}
