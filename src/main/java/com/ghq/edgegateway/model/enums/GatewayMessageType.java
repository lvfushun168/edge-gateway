package com.ghq.edgegateway.model.enums;

import java.util.Arrays;

/**
 * 网关消息类型。
 */
public enum GatewayMessageType {

    AUTH_REQ("auth_req"),
    AUTH_RESP("auth_resp"),
    SYS_CONFIG("sys_config"),
    SYS_CONFIG_ACK("sys_config_ack"),
    CHAT_MSG("chat_msg"),
    CHAT_REPLY("chat_reply"),
    REMOTE_CMD("remote_cmd"),
    REMOTE_CMD_RESULT("remote_cmd_result"),
    PING("ping"),
    PONG("pong");

    private final String code;

    GatewayMessageType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * 根据编码匹配消息类型。
     *
     * @param code 消息编码
     * @return 枚举
     */
    public static GatewayMessageType fromCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
