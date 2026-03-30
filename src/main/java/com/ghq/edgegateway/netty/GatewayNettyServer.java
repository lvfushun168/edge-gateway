package com.ghq.edgegateway.netty;

import com.ghq.edgegateway.config.GatewayNettyProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

/**
 * Netty 网关生命周期管理。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "gateway.netty", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GatewayNettyServer implements SmartLifecycle {

    private final GatewayNettyProperties gatewayNettyProperties;

    private final GatewayNettyServerInitializer gatewayNettyServerInitializer;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private Channel serverChannel;

    private volatile boolean running;

    public GatewayNettyServer(GatewayNettyProperties gatewayNettyProperties,
                              GatewayNettyServerInitializer gatewayNettyServerInitializer) {
        this.gatewayNettyProperties = gatewayNettyProperties;
        this.gatewayNettyServerInitializer = gatewayNettyServerInitializer;
    }

    /**
     * 启动 Netty 服务。
     */
    @Override
    public void start() {
        if (running) {
            return;
        }
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(gatewayNettyServerInitializer);
            serverChannel = serverBootstrap.bind(gatewayNettyProperties.getHost(), gatewayNettyProperties.getPort())
                    .sync()
                    .channel();
            running = true;
            log.info("Netty网关启动成功, host={}, port={}, path={}",
                    gatewayNettyProperties.getHost(),
                    gatewayNettyProperties.getPort(),
                    gatewayNettyProperties.getWebsocketPath());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Netty网关启动失败", exception);
        }
    }

    /**
     * 停止 Netty 服务。
     */
    @Override
    public void stop() {
        shutdown();
    }

    /**
     * 停止 Netty 服务并执行回调。
     *
     * @param callback 回调
     */
    @Override
    public void stop(Runnable callback) {
        shutdown();
        callback.run();
    }

    /**
     * 当前服务是否运行中。
     *
     * @return 是否运行
     */
    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * 设置为自动启动。
     *
     * @return true
     */
    @Override
    public boolean isAutoStartup() {
        return true;
    }

    /**
     * 设置启动顺序。
     *
     * @return 顺序
     */
    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    /**
     * 进程退出前释放资源。
     */
    @PreDestroy
    public void preDestroy() {
        shutdown();
    }

    /**
     * 关闭 Netty 资源。
     */
    private void shutdown() {
        if (!running) {
            return;
        }
        try {
            if (serverChannel != null) {
                serverChannel.close().syncUninterruptibly();
            }
        } finally {
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
            running = false;
            log.info("Netty网关已停止");
        }
    }
}
