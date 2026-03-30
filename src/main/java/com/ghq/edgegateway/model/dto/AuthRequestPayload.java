package com.ghq.edgegateway.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 鉴权请求载荷。
 */
@Data
public class AuthRequestPayload {

    @JsonProperty("device_id")
    private String deviceId;

    private String version;
}
