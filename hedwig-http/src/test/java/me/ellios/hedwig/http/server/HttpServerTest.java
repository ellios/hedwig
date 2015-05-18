package me.ellios.hedwig.http.server;

import me.ellios.hedwig.http.service.TRestService;
import me.ellios.hedwig.http.service.impl.TRestServiceImpl;
import me.ellios.hedwig.rpc.core.ServiceConfig;
import me.ellios.hedwig.rpc.core.ServiceSchema;
import me.ellios.hedwig.rpc.core.ServiceType;
import me.ellios.hedwig.rpc.server.RpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/15/13 7:51 PM
 */
public class HttpServerTest {
    private static final Logger LOG = LoggerFactory.getLogger(HttpServerTest.class);

    @Test
    public void testStop() throws Exception {
        RpcServer server = new HttpServer();
        try {
            server.start();
            fail("This should not happen.");
        } catch (Exception ignored) {
        }
        server.stop();
    }

    @Test
    public void testRegister() throws Exception {
        HttpServer server = HttpServerStarter.createHttpServer();
        ServiceConfig config = ServiceConfig.newBuilder()
                .type(ServiceType.THRIFT)
                .schema(ServiceSchema.HTTP)
                .serviceFace(TRestService.Iface.class)
                .serviceImpl(TRestServiceImpl.class)
                .port(9090)
                .build();
        server.registerService(config);
        server.start();
        TimeUnit.DAYS.sleep(1);
    }
}
