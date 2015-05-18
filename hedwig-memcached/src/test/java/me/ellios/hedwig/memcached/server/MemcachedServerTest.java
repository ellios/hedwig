package me.ellios.hedwig.memcached.server;

import me.ellios.hedwig.memcached.example.HelloService;
import me.ellios.hedwig.rpc.core.ServiceConfig;
import me.ellios.hedwig.rpc.core.ServiceSchema;
import me.ellios.hedwig.rpc.core.ServiceType;
import net.spy.memcached.MemcachedClient;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;

/**
 * dispatcher test case.
 *
 * @author gaofeng@qiyi.com
 * @since: 14-3-18
 */
public class MemcachedServerTest {
    private static final int port = 8902;
    private final MemcachedServer server = new MemcachedServer();
    private MemcachedClient client;

    @BeforeTest
    public void beforeTest() throws IOException {



    }

    @Test
    public void simpleTest() {
        assertEquals(client.get("/memcached/hello"), "hello");
    }

    @Test
    public void simpleWithParametersTest() {
        assertEquals(client.get("/memcached/hello/p?name=katty&age=16"), "name=katty,age=16");
    }


    @Test
    public void restUrlTest() {
        assertEquals(client.get("/memcached/hello/user/888888"), "uid=888888");
    }

    @Test
    public void restUrlWithParametersTest() {
        assertEquals(client.get("/memcached/hello/user/katty/info"), "name=katty,age=16");
    }

    @Test
    public void testStartServer() throws Exception {
        ServiceConfig httpConfig = ServiceConfig.newBuilder()
                .serviceFace(HelloService.class)
                .serviceImpl(HelloService.class)
                .port(port)
                .type(ServiceType.THRIFT)
                .schema(ServiceSchema.MEMCACHED)
                .build();
        server.registerService(httpConfig);
        server.start();
        TimeUnit.DAYS.sleep(1);
    }

    @AfterTest
    public void afterTest() {
        server.stop();
    }
}
