package com.ghq.edgegateway.netty;

import com.ghq.edgegateway.model.vo.DeviceSessionVO;
import io.netty.channel.Channel;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * 在线设备会话注册表。
 */
@Component
public class DeviceSessionRegistry {

    private final Map<String, Channel> channelByDeviceId = new ConcurrentHashMap<>();

    /**
     * 绑定设备与连接。
     *
     * @param deviceId 设备ID
     * @param channel 连接
     */
    public void bind(String deviceId, Channel channel) {
        channelByDeviceId.put(deviceId, channel);
        channel.attr(ChannelAttributeConstants.DEVICE_ID).set(deviceId);
        channel.attr(ChannelAttributeConstants.AUTHENTICATED).set(Boolean.TRUE);
    }

    /**
     * 根据设备ID查找连接。
     *
     * @param deviceId 设备ID
     * @return 连接
     */
    public Channel getChannel(String deviceId) {
        return channelByDeviceId.get(deviceId);
    }

    /**
     * 解除绑定。
     *
     * @param channel 连接
     * @return 被移除的设备ID
     */
    public String unbind(Channel channel) {
        String deviceId = channel.attr(ChannelAttributeConstants.DEVICE_ID).get();
        if (deviceId != null) {
            channelByDeviceId.remove(deviceId, channel);
        }
        return deviceId;
    }

    /**
     * 列出当前在线设备。
     *
     * @return 在线设备
     */
    public List<DeviceSessionVO> listSessions() {
        return channelByDeviceId.entrySet().stream()
                .map(entry -> DeviceSessionVO.builder()
                        .deviceId(entry.getKey())
                        .channelId(entry.getValue().id().asShortText())
                        .clientIp(resolveClientIp(entry.getValue()))
                        .active(entry.getValue().isActive())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 解析客户端 IP。
     *
     * @param channel 连接
     * @return 客户端IP
     */
    public String resolveClientIp(Channel channel) {
        if (channel.remoteAddress() instanceof InetSocketAddress) {
            return ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();
        }
        return "unknown";
    }
}
