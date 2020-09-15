package com.github.kfcfans.powerjob.server.service.workflow;

import com.alibaba.fastjson.JSONObject;
import com.github.kfcfans.powerjob.common.OmsException;
import com.github.kfcfans.powerjob.common.TimeExpressionType;
import com.github.kfcfans.powerjob.common.request.http.SaveWorkflowRequest;
import com.github.kfcfans.powerjob.common.response.WorkflowInfoDTO;
import com.github.kfcfans.powerjob.server.common.SJ;
import com.github.kfcfans.powerjob.server.common.constans.SwitchableStatus;
import com.github.kfcfans.powerjob.server.common.utils.CronExpression;
import com.github.kfcfans.powerjob.server.common.utils.WorkflowDAGUtils;
import com.github.kfcfans.powerjob.server.persistence.core.model.WorkflowInfoDO;
import com.github.kfcfans.powerjob.server.persistence.core.repository.WorkflowInfoRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * Workflow 服务
 *
 * @author tjq
 * @since 2020/5/26
 */
@Service
public class WorkflowService {

    @Resource
    private WorkflowInstanceManager workflowInstanceManager;
    @Resource
    private WorkflowInfoRepository workflowInfoRepository;

    /**
     * 保存/修改DAG工作流
     * @param req 请求
     * @return 工作流ID
     * @throws Exception 异常
     */
    public Long saveWorkflow(SaveWorkflowRequest req) throws Exception {

        if (!WorkflowDAGUtils.valid(req.getPEWorkflowDAG())) {
            throw new OmsException("illegal DAG");
        }

        Long wfId = req.getId();
        WorkflowInfoDO wf;
        if (wfId == null) {
            wf = new WorkflowInfoDO();
            wf.setGmtCreate(new Date());
        }else {
            wf = workflowInfoRepository.findById(wfId).orElseThrow(() -> new IllegalArgumentException("can't find workflow by id:" + wfId));
        }

        BeanUtils.copyProperties(req, wf);
        wf.setGmtModified(new Date());
        wf.setPeDAG(JSONObject.toJSONString(req.getPEWorkflowDAG()));
        wf.setStatus(req.isEnable() ? SwitchableStatus.ENABLE.getV() : SwitchableStatus.DISABLE.getV());
        wf.setTimeExpressionType(req.getTimeExpressionType().getV());

        if (req.getNotifyUserIds() != null) {
            wf.setNotifyUserIds(SJ.commaJoiner.join(req.getNotifyUserIds()));
        }

        // 计算 NextTriggerTime
        if (req.getTimeExpressionType() == TimeExpressionType.CRON) {
            CronExpression cronExpression = new CronExpression(req.getTimeExpression());
            Date nextValidTime = cronExpression.getNextValidTimeAfter(new Date());
            wf.setNextTriggerTime(nextValidTime.getTime());
        }else {
            wf.setTimeExpression(null);
        }

        WorkflowInfoDO newEntity = workflowInfoRepository.saveAndFlush(wf);
        return newEntity.getId();
    }

    /**
     * 获取工作流元信息
     * @param wfId 工作流ID
     * @param appId 应用ID
     * @return 对外输出对象
     */
    public WorkflowInfoDTO fetchWorkflow(Long wfId, Long appId) {
        WorkflowInfoDO wfInfo = permissionCheck(wfId, appId);
        WorkflowInfoDTO dto = new WorkflowInfoDTO();
        BeanUtils.copyProperties(wfInfo, dto);
        return dto;
    }

    /**
     * 删除工作流（软删除）
     * @param wfId 工作流ID
     * @param appId 所属应用ID
     */
    public void deleteWorkflow(Long wfId, Long appId) {
        WorkflowInfoDO wfInfo = permissionCheck(wfId, appId);
        wfInfo.setStatus(SwitchableStatus.DELETED.getV());
        wfInfo.setGmtModified(new Date());
        workflowInfoRepository.saveAndFlush(wfInfo);
    }

    /**
     * 禁用工作流
     * @param wfId 工作流ID
     * @param appId 所属应用ID
     */
    public void disableWorkflow(Long wfId, Long appId) {
        WorkflowInfoDO wfInfo = permissionCheck(wfId, appId);
        wfInfo.setStatus(SwitchableStatus.DISABLE.getV());
        wfInfo.setGmtModified(new Date());
        workflowInfoRepository.saveAndFlush(wfInfo);
    }

    /**
     * 启用工作流
     * @param wfId 工作流ID
     * @param appId 所属应用ID
     */
    public void enableWorkflow(Long wfId, Long appId) {
        WorkflowInfoDO wfInfo = permissionCheck(wfId, appId);
        wfInfo.setStatus(SwitchableStatus.ENABLE.getV());
        wfInfo.setGmtModified(new Date());
        workflowInfoRepository.saveAndFlush(wfInfo);
    }

    /**
     * 立即运行工作流
     * @param wfId 工作流ID
     * @param appId 所属应用ID
     * @return 该 workflow 实例的 instanceId（wfInstanceId）
     */
    public Long runWorkflow(Long wfId, Long appId) {

        WorkflowInfoDO wfInfo = permissionCheck(wfId, appId);
        Long wfInstanceId = workflowInstanceManager.create(wfInfo);

        // 正式启动任务
        workflowInstanceManager.start(wfInfo, wfInstanceId);
        return wfInstanceId;
    }

    private WorkflowInfoDO permissionCheck(Long wfId, Long appId) {
        WorkflowInfoDO wfInfo = workflowInfoRepository.findById(wfId).orElseThrow(() -> new IllegalArgumentException("can't find workflow by id: " + wfId));
        if (!wfInfo.getAppId().equals(appId)) {
            throw new OmsException("Permission Denied!can't delete other appId's workflow!");
        }
        return wfInfo;
    }
}
