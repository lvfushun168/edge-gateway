package com.ghq.edgegateway.service;

import com.ghq.edgegateway.exception.BusinessException;
import com.ghq.edgegateway.model.dto.AuthRequestPayload;
import com.ghq.edgegateway.model.dto.ChatMessagePayload;
import com.ghq.edgegateway.model.dto.ChatReplyPayload;
import com.ghq.edgegateway.model.dto.MockDownlinkRequest;
import com.ghq.edgegateway.model.dto.RemoteCmdPayload;
import com.ghq.edgegateway.model.dto.RemoteCmdResultPayload;
import com.ghq.edgegateway.model.dto.SysConfigAckPayload;
import com.ghq.edgegateway.model.dto.SysConfigPayload;
import com.ghq.edgegateway.model.dto.WsEnvelope;
import com.ghq.edgegateway.model.enums.GatewayMessageType;
import com.ghq.edgegateway.util.JsonUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 网关消息校验器。
 */
@Service
public class GatewayMessageValidator {

    private final JsonUtil jsonUtil;

    public GatewayMessageValidator(JsonUtil jsonUtil) {
        this.jsonUtil = jsonUtil;
    }

    /**
     * 校验 WebSocket 入站消息。
     *
     * @param envelope 消息信封
     */
    public void validateInboundEnvelope(WsEnvelope<Object> envelope) {
        if (envelope == null || !StringUtils.hasText(envelope.getType())) {
            throw new BusinessException(400, "消息type不能为空");
        }
        GatewayMessageType messageType = GatewayMessageType.fromCode(envelope.getType());
        if (messageType == null) {
            throw new BusinessException(400, "未知消息类型");
        }
        if (GatewayMessageType.PING == messageType || GatewayMessageType.PONG == messageType) {
            return;
        }
        if (envelope.getPayload() == null) {
            throw new BusinessException(400, "消息payload不能为空");
        }
        switch (messageType) {
            case AUTH_REQ:
                validateAuthRequest(envelope);
                break;
            case SYS_CONFIG:
                validateSysConfig(envelope);
                break;
            case SYS_CONFIG_ACK:
                validateSysConfigAck(envelope);
                break;
            case CHAT_MSG:
                validateChatMessage(envelope);
                break;
            case CHAT_REPLY:
                validateChatReply(envelope);
                break;
            case REMOTE_CMD:
                validateRemoteCmd(envelope);
                break;
            case REMOTE_CMD_RESULT:
                validateRemoteCmdResult(envelope);
                break;
            default:
                break;
        }
    }

    /**
     * 校验 mock 下行请求。
     *
     * @param request mock请求
     */
    public void validateMockDownlink(MockDownlinkRequest request) {
        GatewayMessageType messageType = GatewayMessageType.fromCode(request.getType());
        if (messageType == null) {
            throw new BusinessException(400, "mock下行type不支持");
        }
        WsEnvelope<Object> envelope = WsEnvelope.builder()
                .msgId(request.getMsgId())
                .type(request.getType())
                .timestamp(request.getTimestamp())
                .payload(request.getPayload())
                .build();
        validateInboundEnvelope(envelope);
    }

    private void validateAuthRequest(WsEnvelope<Object> envelope) {
        AuthRequestPayload payload = jsonUtil.convertValue(envelope.getPayload(), AuthRequestPayload.class);
        if (!StringUtils.hasText(payload.getDeviceId())) {
            throw new BusinessException(400, "auth_req.device_id不能为空");
        }
        if (!StringUtils.hasText(payload.getVersion())) {
            throw new BusinessException(400, "auth_req.version不能为空");
        }
    }

    private void validateSysConfig(WsEnvelope<Object> envelope) {
        SysConfigPayload payload = jsonUtil.convertValue(envelope.getPayload(), SysConfigPayload.class);
        if (!StringUtils.hasText(payload.getAction())) {
            throw new BusinessException(400, "sys_config.action不能为空");
        }
        if (payload.getConfigVersion() == null) {
            throw new BusinessException(400, "sys_config.config_version不能为空");
        }
        if (payload.getConfig() == null || payload.getConfig().isEmpty()) {
            throw new BusinessException(400, "sys_config.config不能为空");
        }
    }

    private void validateSysConfigAck(WsEnvelope<Object> envelope) {
        SysConfigAckPayload payload = jsonUtil.convertValue(envelope.getPayload(), SysConfigAckPayload.class);
        if (payload.getConfigVersion() == null) {
            throw new BusinessException(400, "sys_config_ack.config_version不能为空");
        }
        if (!StringUtils.hasText(payload.getStatus())) {
            throw new BusinessException(400, "sys_config_ack.status不能为空");
        }
        if (payload.getApplied() == null) {
            throw new BusinessException(400, "sys_config_ack.applied不能为空");
        }
    }

    private void validateChatMessage(WsEnvelope<Object> envelope) {
        ChatMessagePayload payload = jsonUtil.convertValue(envelope.getPayload(), ChatMessagePayload.class);
        if (!StringUtils.hasText(payload.getSessionId())) {
            throw new BusinessException(400, "chat_msg.session_id不能为空");
        }
        if (!StringUtils.hasText(payload.getRole())) {
            throw new BusinessException(400, "chat_msg.role不能为空");
        }
        if (!StringUtils.hasText(payload.getText())) {
            throw new BusinessException(400, "chat_msg.text不能为空");
        }
        if (payload.getStream() == null) {
            throw new BusinessException(400, "chat_msg.stream不能为空");
        }
    }

    private void validateChatReply(WsEnvelope<Object> envelope) {
        ChatReplyPayload payload = jsonUtil.convertValue(envelope.getPayload(), ChatReplyPayload.class);
        if (!StringUtils.hasText(payload.getSessionId())) {
            throw new BusinessException(400, "chat_reply.session_id不能为空");
        }
        if (!StringUtils.hasText(payload.getRole())) {
            throw new BusinessException(400, "chat_reply.role不能为空");
        }
        if (payload.getChunkSeq() == null) {
            throw new BusinessException(400, "chat_reply.chunk_seq不能为空");
        }
        if (payload.getIsFinal() == null) {
            throw new BusinessException(400, "chat_reply.is_final不能为空");
        }
        if (payload.getIsEnd() == null) {
            throw new BusinessException(400, "chat_reply.is_end不能为空");
        }
    }

    private void validateRemoteCmd(WsEnvelope<Object> envelope) {
        RemoteCmdPayload payload = jsonUtil.convertValue(envelope.getPayload(), RemoteCmdPayload.class);
        if (!StringUtils.hasText(payload.getCommandId())) {
            throw new BusinessException(400, "remote_cmd.command_id不能为空");
        }
        if (!StringUtils.hasText(payload.getCommand())) {
            throw new BusinessException(400, "remote_cmd.command不能为空");
        }
        if (payload.getTimeoutSec() == null || payload.getTimeoutSec() <= 0) {
            throw new BusinessException(400, "remote_cmd.timeout_sec必须大于0");
        }
    }

    private void validateRemoteCmdResult(WsEnvelope<Object> envelope) {
        RemoteCmdResultPayload payload = jsonUtil.convertValue(envelope.getPayload(), RemoteCmdResultPayload.class);
        if (!StringUtils.hasText(payload.getCommandId())) {
            throw new BusinessException(400, "remote_cmd_result.command_id不能为空");
        }
        if (!StringUtils.hasText(payload.getStatus())) {
            throw new BusinessException(400, "remote_cmd_result.status不能为空");
        }
        if (payload.getStdoutTruncated() == null) {
            throw new BusinessException(400, "remote_cmd_result.stdout_truncated不能为空");
        }
        if (payload.getStderrTruncated() == null) {
            throw new BusinessException(400, "remote_cmd_result.stderr_truncated不能为空");
        }
    }
}
