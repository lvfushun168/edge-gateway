package com.ghq.edgegateway.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Data;

/**
 * 配置下行载荷。
 */
@Data
public class SysConfigPayload {

    private String action;

    @JsonProperty("config_version")
    private Long configVersion;

    private Map<String, Object> config;
}
