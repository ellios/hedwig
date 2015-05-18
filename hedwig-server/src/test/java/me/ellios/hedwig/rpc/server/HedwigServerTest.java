package me.ellios.hedwig.rpc.server;

import me.ellios.hedwig.rpc.core.ServiceConfig;
import me.ellios.hedwig.rpc.core.ServiceSchema;
import me.ellios.hedwig.rpc.core.ServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.testng.Assert.fail;

/**
 * Say something?
 *
 * @author George
 * @since 13-3-6 下午6:47
 */

public class HedwigServerTest {
    private static final Logger LOG = LoggerFactory.getLogger(HedwigServerTest.class);

    @Test
    public void testStart() throws Exception {
        try {
            server.start();
            fail("Server started without service registered.");
        } catch (IllegalArgumentException ignored) {
            // no-op
        }
    }

    @Test
    public void testThriftServer() throws Exception {

    }


    private int getPort() {
        return ThreadLocalRandom.current().nextInt(4096) + 1024;
    }

    @Test
    public void testHttpServer() throws Exception {
        int port = getPort();
        ServiceConfig config = ServiceConfig.newBuilder()
                .port(port)
                .schema(ServiceSchema.HTTP)
                .type(ServiceType.THRIFT)
                .serviceImpl(getClass())
                .serviceFace(getClass())
                .build();
        server.registerService(config);
        server.start();
    }

    @Test
    public void testProtobufServer() throws Exception {

    }

    private HedwigServer server;

    @Test
    public void testName() throws Exception {


    }

    @BeforeMethod
    public void setUp() throws Exception {
        server = HedwigServer.getServer();
    }
}
