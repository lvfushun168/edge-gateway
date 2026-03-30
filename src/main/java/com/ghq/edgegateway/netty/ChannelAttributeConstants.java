package com.ghq.edgegateway.netty;

import io.netty.util.AttributeKey;
import java.util.concurrent.ScheduledFuture;

/**
 * Channel 属性常量。
 */
public final class ChannelAttributeConstants {

    public static final AttributeKey<String> DEVICE_ID = AttributeKey.valueOf("deviceId");

    public static final AttributeKey<Boolean> AUTHENTICATED = AttributeKey.valueOf("authenticated");

    public static final AttributeKey<ScheduledFuture<?>> AUTH_TIMEOUT_FUTURE = AttributeKey.valueOf("authTimeoutFuture");

    private ChannelAttributeConstants() {
    }
}
