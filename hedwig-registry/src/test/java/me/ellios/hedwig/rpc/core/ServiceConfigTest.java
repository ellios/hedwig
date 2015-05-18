package me.ellios.hedwig.rpc.core;

import me.ellios.hedwig.rpc.core.ServiceConfig;
import me.ellios.hedwig.rpc.core.ServiceSchema;
import me.ellios.hedwig.rpc.core.ServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Say something?
 * User: George
 * Date: 13-2-28
 * Time: 下午5:08
 */

public class ServiceConfigTest {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceConfigTest.class);

    ServiceConfig.Builder builder;

    @BeforeMethod
    public void setUp() throws Exception {
        builder = ServiceConfig.newBuilder()
                .serviceFace(getClass())
                .serviceImpl(getClass())
                .type(ServiceType.THRIFT);
    }

    @Test
    public void testBuilderFace() throws Exception {
        boolean yes = false;
        try {
            ServiceConfig config = ServiceConfig.newBuilder()
                    .serviceFace(getClass())
                    .build();
            LOG.info("{}", config);
        } catch (NullPointerException ignored) {
            yes = true;
        }
        assertTrue(yes);
        yes = false;
        try {
            ServiceConfig.newBuilder()
                    .serviceFace(getClass())
                    .serviceImpl(ServiceConfig.class)
                    .type(ServiceType.THRIFT)
                    .build();
        } catch (IllegalArgumentException ignored) {
            yes = true;
        }
        assertTrue(yes);
    }

    @Test
    public void testBuilderType() throws Exception {
        boolean yes = false;
        try {
            ServiceConfig config = ServiceConfig.newBuilder()
                    .type(ServiceType.THRIFT)
                    .build();
            LOG.info("{}", config);
        } catch (NullPointerException ignored) {
            yes = true;
        }
        assertTrue(yes);
    }

    @Test
    public void testBuilderImpl() throws Exception {
        boolean yes = false;
        try {
            ServiceConfig config = ServiceConfig.newBuilder()
                    .serviceImpl(getClass())
                    .build();
            LOG.info("{}", config);
        } catch (NullPointerException ignored) {
            yes = true;
        }
        assertTrue(yes);
    }

    @Test
    public void testBuilderAllNull() throws Exception {
        boolean yes = false;
        try {
            ServiceConfig config = ServiceConfig.newBuilder().build();
            LOG.info("{}", config);
        } catch (NullPointerException ignored) {
            yes = true;
        }
        assertTrue(yes);
    }

    @Test
    public void testGetMaxFrameSize() throws Exception {
        int maxFrameSize = ServiceConfig.DEFAULT_MAX_FRAME_SIZE;
        ServiceConfig config = builder.build();
        assertNotNull(config);
        assertEquals(maxFrameSize, config.getMaxFrameSize());
        int round = 2000;
        // Positive
        for (int i = 0; i < round; i++) {
            maxFrameSize = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE) + 1;
            config = builder.maxFrameSize(maxFrameSize).build();
            assertNotNull(config);
            assertEquals(maxFrameSize, config.getMaxFrameSize());
        }
        // 0
        maxFrameSize = ServiceConfig.DEFAULT_MAX_FRAME_SIZE;
        config = builder.maxFrameSize(0).build();
        assertNotNull(config);
        assertEquals(maxFrameSize, config.getMaxFrameSize());

        // Negative
        maxFrameSize = ServiceConfig.DEFAULT_MAX_FRAME_SIZE;
        config = builder.maxFrameSize(-1).build();
        assertNotNull(config);
        assertEquals(maxFrameSize, config.getMaxFrameSize());

    }

    @Test
    public void testGetName() throws Exception {
        String defaultName = "T_ServiceConfigTest";
        ServiceConfig config = builder.build();
        assertNotNull(config);
        assertNotNull(config.getName());
        LOG.info("{}", config.getName());
        assertEquals(defaultName, config.getName());
    }

    @Test
    public void testGetHost() throws Exception {

    }

    @Test
    public void testGetPort() throws Exception {

    }

    @Test
    public void testGetSchema() throws Exception {
        ServiceSchema s = ServiceSchema.TCP;
        assertEquals(s, builder.build().getSchema());
        ServiceSchema schema = builder.schema(ServiceSchema.HTTP).build().getSchema();
        assertEquals(ServiceSchema.HTTP, schema);
    }

    @Test
    public void testGetServiceGroup() throws Exception {

    }

    @Test
    public void testToString() throws Exception {

    }

    @Test
    public void testEquals() throws Exception {

    }

    @Test
    public void testHashCode() throws Exception {

    }

    @Test
    public void testNewBuilder() throws Exception {

    }
}
