package com.ghq.edgegateway.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 配置回执上行载荷。
 */
@Data
public class SysConfigAckPayload {

    @JsonProperty("config_version")
    private Long configVersion;

    private String status;

    private Boolean applied;

    @JsonProperty("applied_at")
    private Long appliedAt;

    @JsonProperty("daemon_version")
    private String daemonVersion;

    private String message;

    @JsonProperty("error_code")
    private String errorCode;

    @JsonProperty("error_message")
    private String errorMessage;
}
