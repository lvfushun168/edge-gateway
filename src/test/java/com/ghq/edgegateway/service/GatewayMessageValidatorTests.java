package com.ghq.edgegateway.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghq.edgegateway.exception.BusinessException;
import com.ghq.edgegateway.model.dto.MockDownlinkRequest;
import com.ghq.edgegateway.model.dto.WsEnvelope;
import com.ghq.edgegateway.util.JsonUtil;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GatewayMessageValidatorTests {

    private GatewayMessageValidator gatewayMessageValidator;

    @BeforeEach
    void setUp() {
        gatewayMessageValidator = new GatewayMessageValidator(new JsonUtil(new ObjectMapper()));
    }

    @Test
    void shouldValidateChatMessageSuccessfully() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("session_id", "session-1");
        payload.put("role", "user");
        payload.put("text", "hello");
        payload.put("stream", Boolean.TRUE);
        WsEnvelope<Object> envelope = WsEnvelope.builder()
                .msgId("msg-1")
                .type("chat_msg")
                .timestamp(System.currentTimeMillis())
                .payload(payload)
                .build();

        assertDoesNotThrow(() -> gatewayMessageValidator.validateInboundEnvelope(envelope));
    }

    @Test
    void shouldRejectRemoteCmdWithoutTimeout() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("command_id", "cmd-1");
        payload.put("command", "bash");
        WsEnvelope<Object> envelope = WsEnvelope.builder()
                .msgId("msg-1")
                .type("remote_cmd")
                .timestamp(System.currentTimeMillis())
                .payload(payload)
                .build();

        assertThrows(BusinessException.class, () -> gatewayMessageValidator.validateInboundEnvelope(envelope));
    }

    @Test
    void shouldValidateMockDownlink() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("config_version", 1);
        payload.put("action", "set_env");
        Map<String, Object> config = new HashMap<>();
        config.put("API_KEY", "sk-xxx");
        payload.put("config", config);
        MockDownlinkRequest request = new MockDownlinkRequest();
        request.setDeviceId("device-1");
        request.setType("sys_config");
        request.setTimestamp(System.currentTimeMillis());
        request.setPayload(payload);

        assertDoesNotThrow(() -> gatewayMessageValidator.validateMockDownlink(request));
    }
}
