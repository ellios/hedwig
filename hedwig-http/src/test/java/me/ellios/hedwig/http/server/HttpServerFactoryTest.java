package me.ellios.hedwig.http.server;

import me.ellios.hedwig.rpc.core.ServiceSchema;
import me.ellios.hedwig.rpc.core.ServiceType;
import me.ellios.hedwig.rpc.server.RpcServer;
import me.ellios.hedwig.rpc.server.RpcServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertEquals;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/28/13 3:48 PM
 */
public class HttpServerFactoryTest {
    private static final Logger LOG = LoggerFactory.getLogger(HttpServerFactoryTest.class);

    @Test
    public void testCanCreate() throws Exception {
        RpcServerFactory factory = new HttpServerFactory();
        for (ServiceSchema schema : ServiceSchema.values()) {
            boolean expected = (schema == ServiceSchema.HTTP);
            for (ServiceType type : ServiceType.values()) {
                assertEquals(factory.accept(schema, type), expected);
            }
        }
    }


    @Test
    public void testCreate() throws Exception {
        RpcServerFactory factory = new HttpServerFactory();
        RpcServer s = factory.create();
        assertNotNull(s);
    }
}
