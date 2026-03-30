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

    public MockDownlinkService(GatewayRedisPublisher gatewayRedisPublisher) {
        this.gatewayRedisPublisher = gatewayRedisPublisher;
    }

    /**
     * 构造模拟下行消息并投递到 Redis。
     *
     * @param request 请求参数
     */
    public void publish(MockDownlinkRequest request) {
        WsEnvelope<Object> msg = WsEnvelope.builder()
                .msgId(request.getMsgId() == null ? UUID.randomUUID().toString() : request.getMsgId())
                .type(request.getType())
                .timestamp(System.currentTimeMillis())
                .payload(request.getPayload())
                .build();
        RedisDownlinkMessage message = new RedisDownlinkMessage();
        message.setDeviceId(request.getDeviceId());
        message.setMsg(msg);
        log.info("收到mock下行请求, deviceId={}, type={}", request.getDeviceId(), request.getType());
        gatewayRedisPublisher.publishDownlink(message);
    }
}
