package com.ghq.edgegateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "gateway.netty.enabled=false",
        "gateway.redis.enabled=false"
})
class EdgeGatewayApplicationTests {

    @Test
    void contextLoads() {
    }
}
