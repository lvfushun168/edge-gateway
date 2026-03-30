package com.ghq.edgegateway.model.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 在线会话视图。
 */
@Data
@Builder
public class DeviceSessionVO {

    private String deviceId;

    private String channelId;

    private String clientIp;

    private Boolean active;
}
