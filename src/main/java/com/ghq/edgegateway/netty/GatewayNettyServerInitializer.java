package com.ghq.edgegateway.netty;

import com.ghq.edgegateway.config.GatewayNettyProperties;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.stereotype.Component;

/**
 * Netty Channel 初始化器。
 */
@Component
public class GatewayNettyServerInitializer extends ChannelInitializer<SocketChannel> {

    private final GatewayNettyProperties gatewayNettyProperties;

    private final GatewayWebSocketHandler gatewayWebSocketHandler;

    public GatewayNettyServerInitializer(GatewayNettyProperties gatewayNettyProperties,
                                         GatewayWebSocketHandler gatewayWebSocketHandler) {
        this.gatewayNettyProperties = gatewayNettyProperties;
        this.gatewayWebSocketHandler = gatewayWebSocketHandler;
    }

    /**
     * 初始化 ChannelPipeline。
     *
     * @param channel SocketChannel
     */
    @Override
    protected void initChannel(SocketChannel channel) {
        channel.pipeline()
                .addLast(new HttpServerCodec())
                .addLast(new HttpObjectAggregator(65536))
                .addLast(new IdleStateHandler(gatewayNettyProperties.getHeartbeatReadIdleSeconds(), 0, 0))
                .addLast(new WebSocketServerProtocolHandler(gatewayNettyProperties.getWebsocketPath(), null, true, 65536))
                .addLast(gatewayWebSocketHandler);
    }
}
