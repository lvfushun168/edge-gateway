package com.ghq.edgegateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Netty 网关配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "gateway.netty")
public class GatewayNettyProperties {

    private Boolean enabled = Boolean.TRUE;

    private String host = "0.0.0.0";

    private Integer port = 18080;

    private String websocketPath = "/ws/device";

    private Integer authTimeoutSeconds = 5;

    private Integer heartbeatReadIdleSeconds = 90;
}
