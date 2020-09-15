package com.github.kfcfans.powerjob.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.function.Supplier;


/**
 * 公共工具类
 *
 * @author tjq
 * @since 2020/3/18
 */
@Slf4j
public class CommonUtils {

    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * 重试执行，仅适用于失败抛出异常的方法
     * @param executor 需要执行的方法
     * @param tryTimes 尝试次数（总执行次数）
     * @param intervalMS 失败后下一次执行的间隔时间
     * @param <T> 执行函数返回值类型
     * @return 函数成功执行后的返回值
     * @throws Exception 执行失败，调用方自行处理
     */
    public static <T> T executeWithRetry(SupplierPlus<T> executor, int tryTimes, long intervalMS) throws Exception {
        if (tryTimes <= 1 || intervalMS <= 0) {
            return executor.get();
        }
        for (int i = 1; i < tryTimes; i++) {
            try {
                return executor.get();
            }catch (Exception e) {
                Thread.sleep(intervalMS);
            }
        }
        return executor.get();
    }

    public static <T> T executeWithRetry0(SupplierPlus<T> executor) throws Exception {
        return executeWithRetry(executor, 3, 100);
    }

    /**
     * 重试执行，仅适用于根据返回值决定是否执行成功的方法
     * @param booleanExecutor 需要执行的方法，其返回值决定了执行是否成功
     * @param tryTimes 尝试执行次数
     * @param intervalMS 失败后下一次执行的间隔时间
     * @return 最终执行结果
     */
    public static boolean executeWithRetryV2(Supplier<Boolean> booleanExecutor, int tryTimes, long intervalMS) {

        if (tryTimes <= 1 || intervalMS <= 0) {
            return booleanExecutor.get();
        }

        for (int i = 1; i < tryTimes; i++) {
            try {
                if (booleanExecutor.get()) {
                    return true;
                }
                Thread.sleep(intervalMS);
            }catch (Exception ignore) {
            }
        }
        return booleanExecutor.get();
    }


    /**
     * 生成数据库查询语句 in 后的条件
     * @param collection eg,["a", "b", "c"]
     * @return eg,('a','b','c')
     */
    public static String getInStringCondition(Collection<String> collection) {
        if (collection == null || collection.isEmpty()) {
            return "()";
        }
        StringBuilder sb = new StringBuilder(" ( ");
        collection.forEach(str -> sb.append("'").append(str).append("',"));
        return sb.replace(sb.length() -1, sb.length(), " ) ").toString();
    }

    public static void executeIgnoreException(SupplierPlus<?> executor) {
        try {
            executor.get();
        }catch (Exception ignore) {
        }
    }

    public static void executeIgnoreException(Meaningless executor) {
        try {
            executor.m();
        }catch (Exception ignore) {
        }
    }


    /**
     * 将大小格式化为 2的N次
     * @param cap 初始大小
     * @return 格式化后的大小，2的N次
     */
    public static int formatSize(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

}
