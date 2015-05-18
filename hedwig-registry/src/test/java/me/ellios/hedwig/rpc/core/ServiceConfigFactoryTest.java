package me.ellios.hedwig.rpc.core;


import me.ellios.hedwig.rpc.core.ServiceConfig;
import me.ellios.hedwig.rpc.core.ServiceSchema;
import me.ellios.hedwig.rpc.core.ServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Say something?
 * User: George
 * Date: 13-2-28
 * Time: 下午5:37
 */

public class ServiceConfigFactoryTest {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceConfigFactoryTest.class);

    @Test
    public void testCreateThriftServiceConfig() throws Exception {
        ServiceConfig config = ServiceConfig.newBuilder().type(ServiceType.THRIFT).serviceImpl(getClass()).serviceFace(getClass()).build();
        assertNotNull(config);
        assertEquals(ServiceType.THRIFT, config.getType());
        assertEquals(ServiceSchema.TCP, config.getSchema());
        assertEquals("T_ServiceConfigFactoryTest", config.getName());
        assertEquals(ServiceConfig.DEFAULT_SERVICE_GROUP, config.getServiceGroup());

    }

    @Test
    public void testRun() throws Exception {
        int port = 3333;
        String groupName = ServiceConfig.DEFAULT_SERVICE_GROUP;
        ServiceConfig config = ServiceConfig.newBuilder().name("name").port(port).schema(ServiceSchema.SPDY).type(ServiceType.THRIFT)
                .serviceFace(getClass()).serviceImpl(getClass()).build();
        assertNotNull(config);
        assertEquals(port, config.getPort());
        assertEquals("name", config.getName());
        assertEquals(groupName, config.getServiceGroup());
        assertEquals(ServiceSchema.SPDY, config.getSchema());
    }

    @Test
    public void testCreateServiceConfig() throws Exception {
        int port = 2;
        String groupName = "i";
        ServiceConfig config = ServiceConfig.newBuilder().name("name").port(port).schema(ServiceSchema.SPDY).type(ServiceType.THRIFT)
                .serviceGroup(groupName).serviceImpl(getClass()).serviceFace(getClass()).build();
        assertNotNull(config);
        assertEquals(port, config.getPort());
        assertEquals("name", config.getName());
        assertEquals(groupName, config.getServiceGroup());
        assertEquals(ServiceSchema.SPDY, config.getSchema());
        assertEquals(ServiceType.THRIFT, config.getType());
    }
}
