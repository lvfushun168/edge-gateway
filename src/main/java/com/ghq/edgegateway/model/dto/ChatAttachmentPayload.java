package com.ghq.edgegateway.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 聊天媒体附件载荷。
 */
@Data
public class ChatAttachmentPayload {

    @JsonProperty("media_id")
    private String mediaId;

    @JsonProperty("media_type")
    private String mediaType;

    @JsonProperty("mime_type")
    private String mimeType;

    @JsonProperty("preview_url")
    private String previewUrl;

    @JsonProperty("thumbnail_url")
    private String thumbnailUrl;

    @JsonProperty("local_path")
    private String localPath;

    @JsonProperty("file_size")
    private Long fileSize;

    @JsonProperty("error_code")
    private String errorCode;

    @JsonProperty("error_message")
    private String errorMessage;
}
