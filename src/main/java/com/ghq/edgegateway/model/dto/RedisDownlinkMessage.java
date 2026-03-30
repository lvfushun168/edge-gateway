package com.ghq.edgegateway.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Redis 下行消息。
 */
@Data
public class RedisDownlinkMessage {

    @JsonProperty("device_id")
    private String deviceId;

    private WsEnvelope<Object> msg;
}
