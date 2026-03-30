package com.ghq.edgegateway.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket 信封结构。
 *
 * @param <T> 载荷类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WsEnvelope<T> {

    @JsonProperty("msg_id")
    private String msgId;

    private String type;

    private Long timestamp;

    private T payload;
}
