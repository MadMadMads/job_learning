package com.github.kfcfans.powerjob.server.web.response;

import com.alibaba.fastjson.JSONObject;
import com.github.kfcfans.powerjob.common.OmsConstant;
import com.github.kfcfans.powerjob.common.model.PEWorkflowDAG;
import com.github.kfcfans.powerjob.server.persistence.core.model.WorkflowInstanceInfoDO;
import lombok.Data;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;

/**
 * 工作流实例视图层展示对象
 *
 * @author tjq
 * @since 2020/5/31
 */
@Data
public class WorkflowInstanceInfoVO {

    // workflowInstanceId（任务实例表都使用单独的ID作为主键以支持潜在的分表需求）
    private String wfInstanceId;

    private String workflowId;
    // 工作流名称，通过 workflowId 查询获取
    private String workflowName;

    // workflow 状态（WorkflowInstanceStatus）
    private Integer status;

    private PEWorkflowDAG pEWorkflowDAG;
    private String result;

    // 实际触发时间（需要格式化为人看得懂的时间）
    private String actualTriggerTime;
    // 结束时间（同理，需要格式化）
    private String finishedTime;

    public static WorkflowInstanceInfoVO from(WorkflowInstanceInfoDO wfInstanceDO, String workflowName) {
        WorkflowInstanceInfoVO vo = new WorkflowInstanceInfoVO();
        BeanUtils.copyProperties(wfInstanceDO, vo);

        vo.setWorkflowName(workflowName);
        vo.setPEWorkflowDAG(JSONObject.parseObject(wfInstanceDO.getDag(), PEWorkflowDAG.class));

        // JS精度丢失问题
        vo.setWfInstanceId(String.valueOf(wfInstanceDO.getWfInstanceId()));
        vo.setWorkflowId(String.valueOf(wfInstanceDO.getWorkflowId()));

        // 格式化时间
        vo.setActualTriggerTime(DateFormatUtils.format(wfInstanceDO.getActualTriggerTime(), OmsConstant.TIME_PATTERN));
        if (wfInstanceDO.getFinishedTime() == null) {
            vo.setFinishedTime(OmsConstant.NONE);
        }else {
            vo.setFinishedTime(DateFormatUtils.format(wfInstanceDO.getFinishedTime(), OmsConstant.TIME_PATTERN));
        }

        return vo;
    }
}
