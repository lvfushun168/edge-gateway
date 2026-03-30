package com.ghq.edgegateway.service;

import com.ghq.edgegateway.config.GatewayMockProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 设备鉴权服务。
 */
@Slf4j
@Service
public class DeviceAuthService {

    private final GatewayMockProperties gatewayMockProperties;

    public DeviceAuthService(GatewayMockProperties gatewayMockProperties) {
        this.gatewayMockProperties = gatewayMockProperties;
    }

    /**
     * 校验设备是否允许接入。
     *
     * @param deviceId 设备ID
     * @param version 设备版本
     * @return 是否允许接入
     */
    public boolean allowAccess(String deviceId, String version) {
        if (!StringUtils.hasText(deviceId)) {
            return false;
        }
        if (Boolean.TRUE.equals(gatewayMockProperties.getEnabled())) {
            log.info("mock设备鉴权通过, deviceId={}, version={}", deviceId, version);
            // todo 接入业务同事提供的设备注册校验接口，入参：deviceId、version；出参：是否允许接入、失败原因
            return true;
        }
        return true;
    }
}
