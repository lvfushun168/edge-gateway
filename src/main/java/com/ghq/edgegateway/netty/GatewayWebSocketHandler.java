package com.ghq.edgegateway.netty;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ghq.edgegateway.config.GatewayNettyProperties;
import com.ghq.edgegateway.model.dto.AuthRequestPayload;
import com.ghq.edgegateway.model.dto.AuthResponsePayload;
import com.ghq.edgegateway.model.dto.DeviceStatusEvent;
import com.ghq.edgegateway.model.dto.RedisUplinkMessage;
import com.ghq.edgegateway.model.dto.WsEnvelope;
import com.ghq.edgegateway.model.enums.GatewayMessageType;
import com.ghq.edgegateway.redis.GatewayRedisPublisher;
import com.ghq.edgegateway.service.DeviceAuthService;
import com.ghq.edgegateway.service.GatewayMessageValidator;
import com.ghq.edgegateway.util.JsonUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * WebSocket 文本帧处理器。
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class GatewayWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final GatewayNettyProperties gatewayNettyProperties;

    private final DeviceAuthService deviceAuthService;

    private final DeviceSessionRegistry deviceSessionRegistry;

    private final GatewayRedisPublisher gatewayRedisPublisher;

    private final GatewayMessageValidator gatewayMessageValidator;

    private final JsonUtil jsonUtil;

    public GatewayWebSocketHandler(GatewayNettyProperties gatewayNettyProperties,
                                   DeviceAuthService deviceAuthService,
                                   DeviceSessionRegistry deviceSessionRegistry,
                                   GatewayRedisPublisher gatewayRedisPublisher,
                                   GatewayMessageValidator gatewayMessageValidator,
                                   JsonUtil jsonUtil) {
        this.gatewayNettyProperties = gatewayNettyProperties;
        this.deviceAuthService = deviceAuthService;
        this.deviceSessionRegistry = deviceSessionRegistry;
        this.gatewayRedisPublisher = gatewayRedisPublisher;
        this.gatewayMessageValidator = gatewayMessageValidator;
        this.jsonUtil = jsonUtil;
    }

    /**
     * 处理连接激活事件。
     *
     * @param ctx 连接上下文
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ScheduledFuture<?> authFuture = ctx.executor().schedule(() -> {
            Boolean authenticated = ctx.channel().attr(ChannelAttributeConstants.AUTHENTICATED).get();
            if (!Boolean.TRUE.equals(authenticated) && ctx.channel().isActive()) {
                log.warn("连接鉴权超时，关闭连接, channelId={}", ctx.channel().id().asShortText());
                ctx.close();
            }
        }, gatewayNettyProperties.getAuthTimeoutSeconds(), java.util.concurrent.TimeUnit.SECONDS);
        ctx.channel().attr(ChannelAttributeConstants.AUTH_TIMEOUT_FUTURE).set(authFuture);
        ctx.fireChannelActive();
    }

    /**
     * 处理文本帧消息。
     *
     * @param ctx 连接上下文
     * @param frame 文本帧
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        String text = frame.text();
        WsEnvelope<Object> envelope;
        try {
            envelope = jsonUtil.fromJson(text, new TypeReference<WsEnvelope<Object>>() {
            });
            gatewayMessageValidator.validateInboundEnvelope(envelope);
        } catch (Exception exception) {
            log.warn("收到非法WebSocket消息, channelId={}, payload={}, reason={}",
                    ctx.channel().id().asShortText(), text, exception.getMessage());
            ctx.close();
            return;
        }
        GatewayMessageType messageType = GatewayMessageType.fromCode(envelope.getType());
        if (messageType == null) {
            if (!Boolean.TRUE.equals(ctx.channel().attr(ChannelAttributeConstants.AUTHENTICATED).get())) {
                log.warn("未鉴权连接发送未知消息类型，关闭连接, channelId={}, payload={}",
                        ctx.channel().id().asShortText(), text);
                ctx.close();
                return;
            }
            log.info("收到未注册消息类型，按透传处理, type={}, channelId={}",
                    envelope.getType(), ctx.channel().id().asShortText());
            handleBusinessMessage(ctx.channel(), envelope);
            return;
        }
        if (GatewayMessageType.PING == messageType) {
            writeMessage(ctx.channel(), Collections.singletonMap("type", GatewayMessageType.PONG.getCode()));
            return;
        }
        if (GatewayMessageType.AUTH_REQ == messageType) {
            handleAuth(ctx.channel(), envelope);
            return;
        }
        if (!Boolean.TRUE.equals(ctx.channel().attr(ChannelAttributeConstants.AUTHENTICATED).get())) {
            log.warn("未鉴权连接发送业务消息，关闭连接, channelId={}", ctx.channel().id().asShortText());
            ctx.close();
            return;
        }
        handleBusinessMessage(ctx.channel(), envelope);
    }

    /**
     * 处理心跳空闲事件。
     *
     * @param ctx 连接上下文
     * @param evt 事件
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            log.warn("设备心跳超时，关闭连接, channelId={}", ctx.channel().id().asShortText());
            ctx.close();
            return;
        }
        super.userEventTriggered(ctx, evt);
    }

    /**
     * 处理连接关闭事件。
     *
     * @param ctx 连接上下文
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String deviceId = deviceSessionRegistry.unbind(ctx.channel());
        if (StringUtils.hasText(deviceId)) {
            gatewayRedisPublisher.publishStatus(DeviceStatusEvent.builder()
                    .deviceId(deviceId)
                    .event("offline")
                    .timestamp(System.currentTimeMillis())
                    .clientIp(deviceSessionRegistry.resolveClientIp(ctx.channel()))
                    .build());
            log.info("设备离线, deviceId={}, channelId={}", deviceId, ctx.channel().id().asShortText());
        }
        ctx.fireChannelInactive();
    }

    /**
     * 处理异常。
     *
     * @param ctx 连接上下文
     * @param cause 异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("WebSocket链路异常, channelId={}", ctx.channel().id().asShortText(), cause);
        ctx.close();
    }

    /**
     * 处理设备鉴权。
     *
     * @param channel 连接
     * @param envelope 消息
     */
    private void handleAuth(Channel channel, WsEnvelope<Object> envelope) {
        AuthRequestPayload payload = jsonUtil.convertValue(envelope.getPayload(), AuthRequestPayload.class);
        boolean allowed = deviceAuthService.allowAccess(payload.getDeviceId(), payload.getVersion());
        WsEnvelope<AuthResponsePayload> response = WsEnvelope.<AuthResponsePayload>builder()
                .msgId(envelope.getMsgId())
                .type(GatewayMessageType.AUTH_RESP.getCode())
                .timestamp(System.currentTimeMillis())
                .payload(new AuthResponsePayload(allowed ? 200 : 401, allowed ? "success" : "illegal device"))
                .build();
        writeMessage(channel, response);
        if (!allowed) {
            channel.close();
            return;
        }
        ScheduledFuture<?> authFuture = channel.attr(ChannelAttributeConstants.AUTH_TIMEOUT_FUTURE).get();
        if (authFuture != null) {
            authFuture.cancel(false);
        }
        Channel previousChannel = deviceSessionRegistry.bind(payload.getDeviceId(), channel);
        if (previousChannel != null && previousChannel != channel && previousChannel.isActive()) {
            log.info("检测到设备重复登录，关闭旧连接, deviceId={}, oldChannelId={}, newChannelId={}",
                    payload.getDeviceId(), previousChannel.id().asShortText(), channel.id().asShortText());
            previousChannel.close();
        }
        gatewayRedisPublisher.publishStatus(DeviceStatusEvent.builder()
                .deviceId(payload.getDeviceId())
                .event("online")
                .timestamp(System.currentTimeMillis())
                .clientIp(deviceSessionRegistry.resolveClientIp(channel))
                .build());
        log.info("设备鉴权成功, deviceId={}, version={}, channelId={}",
                payload.getDeviceId(), payload.getVersion(), channel.id().asShortText());
    }

    /**
     * 处理业务消息并上报 Redis。
     *
     * @param channel 连接
     * @param envelope 消息
     */
    private void handleBusinessMessage(Channel channel, WsEnvelope<Object> envelope) {
        String deviceId = channel.attr(ChannelAttributeConstants.DEVICE_ID).get();
        if (!StringUtils.hasText(deviceId)) {
            log.warn("连接未绑定deviceId，忽略业务消息, channelId={}", channel.id().asShortText());
            return;
        }
        if (!StringUtils.hasText(envelope.getMsgId())) {
            envelope.setMsgId(UUID.randomUUID().toString());
        }
        if (envelope.getTimestamp() == null) {
            envelope.setTimestamp(System.currentTimeMillis());
        }
        gatewayRedisPublisher.publishUplink(RedisUplinkMessage.builder()
                .deviceId(deviceId)
                .msg(envelope)
                .build());
        log.info("上报设备消息到Redis, deviceId={}, type={}", deviceId, envelope.getType());
    }

    /**
     * 发送消息到设备端。
     *
     * @param channel 连接
     * @param message 消息
     */
    private void writeMessage(Channel channel, Object message) {
        channel.writeAndFlush(new TextWebSocketFrame(jsonUtil.toJson(message)));
    }
}
