package com.github.kfcfans.powerjob.server.service;

import com.github.kfcfans.powerjob.server.persistence.core.model.UserInfoDO;
import com.github.kfcfans.powerjob.server.persistence.core.repository.UserInfoRepository;
import com.github.kfcfans.powerjob.server.web.request.ModifyUserInfoRequest;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户服务
 *
 * @author tjq
 * @since 2020/6/12
 */
@Service
public class UserService {

    @Resource
    private UserInfoRepository userInfoRepository;

    /**
     * 保存/修改 用户
     * @param request 请求
     */
    public void save(ModifyUserInfoRequest request) {
        UserInfoDO userInfoDO = new UserInfoDO();
        BeanUtils.copyProperties(request, userInfoDO);
        userInfoDO.setGmtCreate(new Date());
        userInfoDO.setGmtModified(userInfoDO.getGmtCreate());
        userInfoRepository.saveAndFlush(userInfoDO);
    }

    /**
     * 根据用户ID字符串获取用户信息详细列表
     * @param userIds 逗号分割的用户ID信息
     * @return 用户信息详细列表
     */
    public List<UserInfoDO> fetchNotifyUserList(String userIds) {
        if (StringUtils.isEmpty(userIds)) {
            return Lists.newLinkedList();
        }
        // 去重
        Set<Long> userIdList = Splitter.on(",").splitToList(userIds).stream().map(Long::valueOf).collect(Collectors.toSet());
        List<UserInfoDO> res = userInfoRepository.findByIdIn(Lists.newLinkedList(userIdList));
        res.forEach(x -> x.setPassword(null));
        return res;
    }
}
