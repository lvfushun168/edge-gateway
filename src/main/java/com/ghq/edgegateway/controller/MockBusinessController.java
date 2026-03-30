package com.ghq.edgegateway.controller;

import com.ghq.edgegateway.common.Result;
import com.ghq.edgegateway.model.dto.MockDownlinkRequest;
import com.ghq.edgegateway.model.vo.DeviceSessionVO;
import com.ghq.edgegateway.netty.DeviceSessionRegistry;
import com.ghq.edgegateway.service.MockDownlinkService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 本地联调用的 mock 业务接口。
 */
@RestController
@RequestMapping("/api/mock")
public class MockBusinessController {

    private final MockDownlinkService mockDownlinkService;

    private final DeviceSessionRegistry deviceSessionRegistry;

    public MockBusinessController(MockDownlinkService mockDownlinkService,
                                  DeviceSessionRegistry deviceSessionRegistry) {
        this.mockDownlinkService = mockDownlinkService;
        this.deviceSessionRegistry = deviceSessionRegistry;
    }

    /**
     * 模拟业务后端发送下行消息。
     *
     * @param request 请求参数
     * @return 响应
     */
    @PostMapping("/downlink")
    public Result<Void> mockDownlink(@Valid @RequestBody MockDownlinkRequest request) {
        mockDownlinkService.publish(request);
        return Result.success(null);
    }

    /**
     * 查询当前在线设备会话。
     *
     * @return 在线设备列表
     */
    @GetMapping("/sessions")
    public Result<List<DeviceSessionVO>> sessions() {
        return Result.success(deviceSessionRegistry.listSessions());
    }
}
