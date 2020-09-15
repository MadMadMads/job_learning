package com.github.kfcfans.powerjob.common;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 时间表达式类型
 *
 * @author tjq
 * @since 2020/3/30
 */
@Getter
@AllArgsConstructor
public enum TimeExpressionType {

    API(1),
    CRON(2),
    FIX_RATE(3),
    FIX_DELAY(4),
    WORKFLOW(5);

    int v;

    public static final List<Integer> frequentTypes = Lists.newArrayList(FIX_RATE.v, FIX_DELAY.v);

    public static TimeExpressionType of(int v) {
        for (TimeExpressionType type : values()) {
            if (type.v == v) {
                return type;
            }
        }
        throw new IllegalArgumentException("unknown TimeExpressionType of " + v);
    }
}
