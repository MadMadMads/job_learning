package com.github.kfcfans.powerjob.server.web.controller;

import com.github.kfcfans.powerjob.common.OmsConstant;
import com.github.kfcfans.powerjob.common.response.ResultDTO;
import com.github.kfcfans.powerjob.server.akka.OhMyServer;
import com.github.kfcfans.powerjob.server.common.constans.ContainerSourceType;
import com.github.kfcfans.powerjob.server.common.constans.ContainerStatus;
import com.github.kfcfans.powerjob.server.common.utils.ContainerTemplateGenerator;
import com.github.kfcfans.powerjob.server.common.utils.OmsFileUtils;
import com.github.kfcfans.powerjob.server.persistence.core.model.AppInfoDO;
import com.github.kfcfans.powerjob.server.persistence.core.model.ContainerInfoDO;
import com.github.kfcfans.powerjob.server.persistence.core.repository.AppInfoRepository;
import com.github.kfcfans.powerjob.server.persistence.core.repository.ContainerInfoRepository;
import com.github.kfcfans.powerjob.server.service.ContainerService;
import com.github.kfcfans.powerjob.server.web.request.GenerateContainerTemplateRequest;
import com.github.kfcfans.powerjob.server.web.request.SaveContainerInfoRequest;
import com.github.kfcfans.powerjob.server.web.response.ContainerInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 容器信息控制层
 *
 * @author tjq
 * @since 2020/5/15
 */
@Slf4j
@RestController
@RequestMapping("/container")
public class ContainerController {

    @Value("${server.port}")
    private int port;

    @Resource
    private ContainerService containerService;
    @Resource
    private AppInfoRepository appInfoRepository;
    @Resource
    private ContainerInfoRepository containerInfoRepository;

    @GetMapping("/downloadJar")
    public void downloadJar(String version, HttpServletResponse response) throws IOException {
        File file = containerService.fetchContainerJarFile(version);
        if (file.exists()) {
            OmsFileUtils.file2HttpResponse(file, response);
        }
    }

    @PostMapping("/downloadContainerTemplate")
    public void downloadContainerTemplate(@RequestBody GenerateContainerTemplateRequest req, HttpServletResponse response) throws IOException {
        File zipFile = ContainerTemplateGenerator.generate(req.getGroup(), req.getArtifact(), req.getName(), req.getPackageName(), req.getJavaVersion());
        OmsFileUtils.file2HttpResponse(zipFile, response);
    }

    @PostMapping("/jarUpload")
    public ResultDTO<String> fileUpload(@RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return ResultDTO.failed("empty file");
        }
        return ResultDTO.success(containerService.uploadContainerJarFile(file));
    }

    @PostMapping("/save")
    public ResultDTO<Void> saveContainer(@RequestBody SaveContainerInfoRequest request) {
        containerService.save(request);
        return ResultDTO.success(null);
    }

    @GetMapping("/delete")
    public ResultDTO<Void> deleteContainer(Long appId, Long containerId) {
        containerService.delete(appId, containerId);
        return ResultDTO.success(null);
    }

    @GetMapping("/list")
    public ResultDTO<List<ContainerInfoVO>> listContainers(Long appId) {
        List<ContainerInfoVO> res = containerInfoRepository.findByAppId(appId).stream().map(ContainerController::convert).collect(Collectors.toList());
        return ResultDTO.success(res);
    }

    @GetMapping("/listDeployedWorker")
    public ResultDTO<String> listDeployedWorker(Long appId, Long containerId, HttpServletResponse response) {
        AppInfoDO appInfoDO = appInfoRepository.findById(appId).orElseThrow(() -> new IllegalArgumentException("can't find app by id:" + appId));
        String targetServer = appInfoDO.getCurrentServer();

        if (StringUtils.isEmpty(targetServer)) {
            return ResultDTO.failed("No workers have even registered！");
        }

        // 转发 HTTP 请求
        if (!OhMyServer.getActorSystemAddress().equals(targetServer)) {
            String targetIp = targetServer.split(":")[0];
            String url = String.format("http://%s:%d/container/listDeployedWorker?appId=%d&containerId=%d", targetIp, port, appId, containerId);
            try {
                response.sendRedirect(url);
                return ResultDTO.success(null);
            }catch (Exception e) {
                return ResultDTO.failed(e);
            }
        }
        return ResultDTO.success(containerService.fetchDeployedInfo(appId, containerId));
    }

    private static ContainerInfoVO convert(ContainerInfoDO containerInfoDO) {
        ContainerInfoVO vo = new ContainerInfoVO();
        BeanUtils.copyProperties(containerInfoDO, vo);
        if (containerInfoDO.getLastDeployTime() == null) {
            vo.setLastDeployTime("N/A");
        }else {
            vo.setLastDeployTime(DateFormatUtils.format(containerInfoDO.getLastDeployTime(), OmsConstant.TIME_PATTERN));
        }
        ContainerStatus status = ContainerStatus.of(containerInfoDO.getStatus());
        vo.setStatus(status.name());
        ContainerSourceType sourceType = ContainerSourceType.of(containerInfoDO.getSourceType());
        vo.setSourceType(sourceType.name());
        return vo;
    }
}
