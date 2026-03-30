package com.ghq.edgegateway.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备状态事件。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceStatusEvent {

    @JsonProperty("device_id")
    private String deviceId;

    private String event;

    private Long timestamp;

    @JsonProperty("client_ip")
    private String clientIp;
}
