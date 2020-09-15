package com.github.kfcfans.powerjob.common.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kfcfans.powerjob.common.OmsException;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * JSON工具类
 *
 * @author tjq
 * @since 2020/4/16
 */
public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }

    public static String toJSONString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        }catch (Exception ignore) {
        }
        return null;
    }

    public static String toJSONStringUnsafe(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    public static byte[] toBytes(Object obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        }catch (Exception ignore) {
        }
        return null;
    }

    public static <T> T parseObject(String json, Class<T> clz) throws JsonProcessingException {
        return objectMapper.readValue(json, clz);
    }

    public static <T> T parseObject(byte[] b, Class<T> clz) throws Exception {
        return objectMapper.readValue(b, clz);
    }

    public static <T> T parseObjectUnsafe(String json, Class<T> clz) {
        try {
            return objectMapper.readValue(json, clz);
        }catch (Exception e) {
            ExceptionUtils.rethrow(e);
        }
        throw new OmsException("impossible");
    }
}
