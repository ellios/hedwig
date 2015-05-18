package me.ellios.hedwig.memcached.example;

import me.ellios.hedwig.memcached.protocol.text.Protocol;
import me.ellios.hedwig.memcached.server.MemcachedServer;
import me.ellios.hedwig.rpc.core.ServiceConfig;
import me.ellios.hedwig.rpc.core.ServiceSchema;
import me.ellios.hedwig.rpc.core.ServiceType;
import net.spy.memcached.DefaultConnectionFactory;
import net.spy.memcached.MemcachedClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Say something?
 *
 * @author George Cao(caozhangzhi@qiyi.com)
 * @since 2014-03-26 18:53
 */
public class HelloServiceTest {
    private static final Logger LOG = LoggerFactory.getLogger(HelloServiceTest.class);
    private static final int port = 8902;
    private MemcachedServer server = new MemcachedServer();
    private MemcachedClient client;

    class Factory extends DefaultConnectionFactory {
        @Override
        public long getOperationTimeout() {
            return TimeUnit.DAYS.toMillis(1);
        }
    }

    @BeforeClass
    public void setUp() throws Exception {

        ServiceConfig httpConfig = ServiceConfig.newBuilder()
                .serviceFace(HelloService.class)
                .serviceImpl(HelloService.class)
                .port(port)
                .type(ServiceType.THRIFT)
                .schema(ServiceSchema.MEMCACHED)
                .build();
        server.registerService(httpConfig);
        server.start();

        client = new MemcachedClient(new Factory(), Collections.singletonList(new InetSocketAddress("127.0.0.1", port)));
    }

    @Test
    public void testHello() throws Exception {
        Object value = client.get("/memcached/hello/p");
        System.out.println(value);
    }

    @Test
    public void testHelloParameters() throws Exception {
        byte[] bytes = "28".getBytes(Protocol.UTF_8);
        System.out.println(bytes);
    }

    @Test
    public void testRest() throws Exception {

    }

    @Test
    public void testRestWithParameters() throws Exception {

    }
}
