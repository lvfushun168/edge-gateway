package com.ghq.edgegateway.config;

import com.ghq.edgegateway.redis.GatewayRedisSubscriber;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Redis 订阅配置。
 */
@Configuration
public class RedisSubscriberConfig {

    /**
     * 注册 Redis 订阅容器。
     *
     * @param connectionFactory 连接工厂
     * @param gatewayRedisSubscriber 订阅器
     * @param gatewayRedisProperties 配置
     * @return 订阅容器
     */
    @Bean
    @ConditionalOnProperty(prefix = "gateway.redis", name = "enabled", havingValue = "true")
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory,
                                                                       GatewayRedisSubscriber gatewayRedisSubscriber,
                                                                       GatewayRedisProperties gatewayRedisProperties) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(gatewayRedisSubscriber, new PatternTopic(gatewayRedisProperties.getDownlinkTopic()));
        return container;
    }
}
