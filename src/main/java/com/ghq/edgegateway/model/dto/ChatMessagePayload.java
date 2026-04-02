package com.ghq.edgegateway.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Data;

/**
 * 聊天下行载荷。
 */
@Data
public class ChatMessagePayload {

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("user_id")
    private String userId;

    private String role;

    private String text;

    private Boolean stream;

    private Map<String, Object> metadata;
}
