package com.ghq.edgegateway.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

/**
 * 远程命令下行载荷。
 */
@Data
public class RemoteCmdPayload {

    @JsonProperty("command_id")
    private String commandId;

    private String command;

    private List<String> args;

    @JsonProperty("timeout_sec")
    private Integer timeoutSec;

    @JsonProperty("work_dir")
    private String workDir;

    private String operator;

    private String reason;
}
