package com.ghq.edgegateway.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghq.edgegateway.exception.BusinessException;
import org.springframework.stereotype.Component;

/**
 * JSON 工具类。
 */
@Component
public class JsonUtil {

    private final ObjectMapper objectMapper;

    public JsonUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 序列化对象。
     *
     * @param value 对象
     * @return JSON 字符串
     */
    public String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(500, "JSON序列化失败");
        }
    }

    /**
     * 反序列化 JSON。
     *
     * @param json JSON 字符串
     * @param typeReference 类型描述
     * @param <T> 目标类型
     * @return 目标对象
     */
    public <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(400, "JSON格式不合法");
        }
    }

    /**
     * 类型转换。
     *
     * @param source 源对象
     * @param targetClass 目标类型
     * @param <T> 目标类型
     * @return 转换结果
     */
    public <T> T convertValue(Object source, Class<T> targetClass) {
        return objectMapper.convertValue(source, targetClass);
    }
}
