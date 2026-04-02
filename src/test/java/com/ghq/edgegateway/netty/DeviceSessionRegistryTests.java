package com.ghq.edgegateway.netty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

class DeviceSessionRegistryTests {

    @Test
    void bindShouldReturnPreviousChannel() {
        DeviceSessionRegistry registry = new DeviceSessionRegistry();
        EmbeddedChannel oldChannel = new EmbeddedChannel();
        EmbeddedChannel newChannel = new EmbeddedChannel();

        ChannelAttributeConstants.AUTHENTICATED.toString();
        assertNull(registry.bind("device-1", oldChannel));
        assertSame(oldChannel, registry.bind("device-1", newChannel));
        assertSame(newChannel, registry.getChannel("device-1"));
    }

    @Test
    void unbindShouldIgnoreReplacedChannel() {
        DeviceSessionRegistry registry = new DeviceSessionRegistry();
        EmbeddedChannel oldChannel = new EmbeddedChannel();
        EmbeddedChannel newChannel = new EmbeddedChannel();

        registry.bind("device-1", oldChannel);
        registry.bind("device-1", newChannel);

        assertNull(registry.unbind(oldChannel));
        assertEquals("device-1", registry.unbind(newChannel));
    }
}
