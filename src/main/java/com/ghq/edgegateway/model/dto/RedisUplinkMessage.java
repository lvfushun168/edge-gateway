package com.ghq.edgegateway.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Redis 上行消息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedisUplinkMessage {

    @JsonProperty("device_id")
    private String deviceId;

    private WsEnvelope<Object> msg;
}
