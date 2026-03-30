package com.ghq.edgegateway.redis;

import com.ghq.edgegateway.config.GatewayRedisProperties;
import com.ghq.edgegateway.model.dto.DeviceStatusEvent;
import com.ghq.edgegateway.model.dto.RedisDownlinkMessage;
import com.ghq.edgegateway.model.dto.RedisUplinkMessage;
import com.ghq.edgegateway.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis 消息发布器。
 */
@Slf4j
@Component
public class GatewayRedisPublisher {

    private final StringRedisTemplate stringRedisTemplate;

    private final JsonUtil jsonUtil;

    private final GatewayRedisProperties gatewayRedisProperties;

    public GatewayRedisPublisher(StringRedisTemplate stringRedisTemplate,
                                 JsonUtil jsonUtil,
                                 GatewayRedisProperties gatewayRedisProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.jsonUtil = jsonUtil;
        this.gatewayRedisProperties = gatewayRedisProperties;
    }

    /**
     * 发布下行消息。
     *
     * @param message 下行消息
     */
    public void publishDownlink(RedisDownlinkMessage message) {
        if (!Boolean.TRUE.equals(gatewayRedisProperties.getEnabled())) {
            log.warn("Redis已禁用，忽略下行消息, deviceId={}", message.getDeviceId());
            return;
        }
        stringRedisTemplate.convertAndSend(gatewayRedisProperties.getDownlinkTopic(), jsonUtil.toJson(message));
    }

    /**
     * 发布上行消息。
     *
     * @param message 上行消息
     */
    public void publishUplink(RedisUplinkMessage message) {
        if (!Boolean.TRUE.equals(gatewayRedisProperties.getEnabled())) {
            log.warn("Redis已禁用，忽略上行消息, deviceId={}", message.getDeviceId());
            return;
        }
        stringRedisTemplate.convertAndSend(gatewayRedisProperties.getUplinkTopic(), jsonUtil.toJson(message));
    }

    /**
     * 发布设备状态事件。
     *
     * @param event 状态事件
     */
    public void publishStatus(DeviceStatusEvent event) {
        if (!Boolean.TRUE.equals(gatewayRedisProperties.getEnabled())) {
            log.warn("Redis已禁用，忽略状态事件, deviceId={}, event={}", event.getDeviceId(), event.getEvent());
            return;
        }
        stringRedisTemplate.convertAndSend(gatewayRedisProperties.getStatusTopic(), jsonUtil.toJson(event));
    }
}
