package com.ghq.edgegateway.service;

import com.ghq.edgegateway.model.dto.MockDownlinkRequest;
import com.ghq.edgegateway.model.dto.RedisDownlinkMessage;
import com.ghq.edgegateway.model.dto.WsEnvelope;
import com.ghq.edgegateway.redis.GatewayRedisPublisher;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Mock 下行服务。
 */
@Slf4j
@Service
public class MockDownlinkService {

    private final GatewayRedisPublisher gatewayRedisPublisher;

    private final GatewayMessageValidator gatewayMessageValidator;

    public MockDownlinkService(GatewayRedisPublisher gatewayRedisPublisher,
                               GatewayMessageValidator gatewayMessageValidator) {
        this.gatewayRedisPublisher = gatewayRedisPublisher;
        this.gatewayMessageValidator = gatewayMessageValidator;
    }

    /**
     * 构造模拟下行消息并投递到 Redis。
     *
     * @param request 请求参数
     */
    public void publish(MockDownlinkRequest request) {
        gatewayMessageValidator.validateMockDownlink(request);
        WsEnvelope<Object> msg = WsEnvelope.builder()
                .msgId(request.getMsgId() == null ? UUID.randomUUID().toString() : request.getMsgId())
                .type(request.getType())
                .timestamp(request.getTimestamp() == null ? System.currentTimeMillis() : request.getTimestamp())
                .payload(request.getPayload())
                .build();
        RedisDownlinkMessage message = new RedisDownlinkMessage();
        message.setDeviceId(request.getDeviceId());
        message.setMsg(msg);
        log.info("收到mock下行请求, deviceId={}, type={}", request.getDeviceId(), request.getType());
        gatewayRedisPublisher.publishDownlink(message);
    }
}
