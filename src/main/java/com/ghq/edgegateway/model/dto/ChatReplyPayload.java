package com.ghq.edgegateway.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 聊天上行载荷。
 */
@Data
public class ChatReplyPayload {

    @JsonProperty("session_id")
    private String sessionId;

    private String role;

    private String text;

    @JsonProperty("chunk_seq")
    private Integer chunkSeq;

    @JsonProperty("is_final")
    private Boolean isFinal;

    @JsonProperty("is_end")
    private Boolean isEnd;

    @JsonProperty("finish_reason")
    private String finishReason;

    @JsonProperty("error_code")
    private String errorCode;

    @JsonProperty("error_message")
    private String errorMessage;

    private List<ChatAttachmentPayload> attachments;
}
