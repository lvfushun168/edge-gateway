package com.ghq.edgegateway.model.dto;

import java.util.Map;
import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Mock 下行请求。
 */
@Data
public class MockDownlinkRequest {

    @NotBlank(message = "deviceId不能为空")
    private String deviceId;

    @NotBlank(message = "type不能为空")
    private String type;

    private String msgId;

    private Map<String, Object> payload;
}
