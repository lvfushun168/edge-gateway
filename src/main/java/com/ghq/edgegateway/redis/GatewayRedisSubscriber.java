package com.ghq.edgegateway.redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ghq.edgegateway.model.dto.RedisDownlinkMessage;
import com.ghq.edgegateway.netty.GatewayMessageDispatcher;
import com.ghq.edgegateway.util.JsonUtil;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/**
 * Redis 下行订阅器。
 */
@Slf4j
@Component
public class GatewayRedisSubscriber implements MessageListener {

    private final JsonUtil jsonUtil;

    private final GatewayMessageDispatcher gatewayMessageDispatcher;

    public GatewayRedisSubscriber(JsonUtil jsonUtil, GatewayMessageDispatcher gatewayMessageDispatcher) {
        this.jsonUtil = jsonUtil;
        this.gatewayMessageDispatcher = gatewayMessageDispatcher;
    }

    /**
     * 消费下行消息并路由到设备连接。
     *
     * @param message Redis消息
     * @param pattern 订阅模式
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        RedisDownlinkMessage downlinkMessage = jsonUtil.fromJson(body, new TypeReference<RedisDownlinkMessage>() {
        });
        log.info("收到Redis下行消息, deviceId={}, type={}",
                downlinkMessage.getDeviceId(),
                downlinkMessage.getMsg() == null ? null : downlinkMessage.getMsg().getType());
        gatewayMessageDispatcher.dispatchDownlink(downlinkMessage);
    }
}
