package com.ghq.edgegateway.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 远程命令结果上行载荷。
 */
@Data
public class RemoteCmdResultPayload {

    @JsonProperty("command_id")
    private String commandId;

    private String status;

    @JsonProperty("exit_code")
    private Integer exitCode;

    private String stdout;

    private String stderr;

    @JsonProperty("stdout_truncated")
    private Boolean stdoutTruncated;

    @JsonProperty("stderr_truncated")
    private Boolean stderrTruncated;

    @JsonProperty("started_at")
    private Long startedAt;

    @JsonProperty("finished_at")
    private Long finishedAt;

    @JsonProperty("duration_ms")
    private Long durationMs;

    private String message;

    @JsonProperty("error_code")
    private String errorCode;

    @JsonProperty("error_message")
    private String errorMessage;
}
