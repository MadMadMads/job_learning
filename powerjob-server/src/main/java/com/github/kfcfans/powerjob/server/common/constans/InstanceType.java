package com.github.kfcfans.powerjob.server.common.constans;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务实例类型
 *
 * @author tjq
 * @since 2020/5/29
 */
@Getter
@AllArgsConstructor
public enum  InstanceType {

    NORMAL(1),
    WORKFLOW(2);

    private final int v;

}
