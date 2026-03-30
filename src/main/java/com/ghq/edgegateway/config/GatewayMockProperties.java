package com.ghq.edgegateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Mock 功能配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "gateway.mock")
public class GatewayMockProperties {

    private Boolean enabled = Boolean.TRUE;
}
