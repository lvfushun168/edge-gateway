package com.ghq.edgegateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Redis 路由配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "gateway.redis")
public class GatewayRedisProperties {

    private Boolean enabled = Boolean.TRUE;

    private String downlinkTopic = "iot:downlink";

    private String uplinkTopic = "iot:uplink";

    private String statusTopic = "iot:status_event";
}
