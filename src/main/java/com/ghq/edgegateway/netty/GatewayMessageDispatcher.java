package com.ghq.edgegateway.netty;

import com.ghq.edgegateway.model.dto.RedisDownlinkMessage;
import com.ghq.edgegateway.util.JsonUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 网关消息分发器。
 */
@Slf4j
@Component
public class GatewayMessageDispatcher {

    private final DeviceSessionRegistry deviceSessionRegistry;

    private final JsonUtil jsonUtil;

    public GatewayMessageDispatcher(DeviceSessionRegistry deviceSessionRegistry, JsonUtil jsonUtil) {
        this.deviceSessionRegistry = deviceSessionRegistry;
        this.jsonUtil = jsonUtil;
    }

    /**
     * 将 Redis 下行消息转发到设备连接。
     *
     * @param message 下行消息
     */
    public void dispatchDownlink(RedisDownlinkMessage message) {
        Channel channel = deviceSessionRegistry.getChannel(message.getDeviceId());
        if (channel == null || !channel.isActive()) {
            log.warn("设备不在线，忽略下行消息, deviceId={}", message.getDeviceId());
            return;
        }
        channel.writeAndFlush(new TextWebSocketFrame(jsonUtil.toJson(message.getMsg())));
    }
}
