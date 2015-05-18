package me.ellios.hedwig.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.Random;

import static org.testng.Assert.*;

/**
 * For HedwigConfig test.
 * User: George
 * Date: 13-2-26
 * Time: 下午4:53
 */

public class HedwigConfigTest {
    private static final Logger LOG = LoggerFactory.getLogger(HedwigConfigTest.class);

    @Test
    public void testLoadAggressively() throws Exception {
        System.setProperty(HedwigConfig.PROPERTY_LOAD_AGGRESSIVE, "true");
        assertTrue(HedwigConfig.getInstance().getBoolean(HedwigConfig.PROPERTY_LOAD_AGGRESSIVE, false));
    }

    @Test
    public void testGetInstance() throws Exception {
        HedwigConfig config = HedwigConfig.getInstance();
        assertNotNull(config);
        LOG.info("{}", config);
    }

    @Test
    public void testGetBoolean() throws Exception {
        String key = "test.boolean";//=false
        boolean value = HedwigConfig.getInstance().getBoolean(key, true);
        assertTrue(!value);
        key = "test.boolean.other";//=other
        value = HedwigConfig.getInstance().getBoolean(key, true);
        assertTrue(!value);
        key = "test.boolean.true";//=true
        value = HedwigConfig.getInstance().getBoolean(key, false);
        assertTrue(value);
        key = "test.boolean.empty";//=
        value = HedwigConfig.getInstance().getBoolean(key, true);
        assertTrue(!value);
        key = "test.boolean.non.exist";
        value = HedwigConfig.getInstance().getBoolean(key, true);
        assertTrue(value);
    }

    @Test
    public void testGetInt() throws Exception {
        String key = "test.int";//=1
        int expected = 1;
        int value = HedwigConfig.getInstance().getInt(key, 0);
        assertEquals(expected, value);
        key = "test.int.empty";//=
        expected = 1;
        value = HedwigConfig.getInstance().getInt(key, expected);
        assertEquals(expected, value);
        key = "test.invalid.int";//=int
        expected = -1;
        value = HedwigConfig.getInstance().getInt(key, expected);
        assertEquals(expected, value);
    }

    @Test
    public void testGetString() throws Exception {
        String key = "test.string";//=true
        String value = HedwigConfig.getInstance().getString(key, "");
        assertEquals("true", value);
        key = "test.string.empty";//=
        value = HedwigConfig.getInstance().getString(key, "true");
        assertEquals("", value);
    }

    @Test
    public void testGetFromSystemProperties() throws Exception {
        Random r = new Random();
        for (int i = 0; i < 200; i++) {
            String key = r.nextLong() + "" + i;
            String value = String.valueOf(r.nextLong());
            System.setProperty(key, value);
            String expected = HedwigConfig.getInstance().getString(key, "-1");
            assertEquals(value, expected);
        }
    }

    @Test
    public void testGetReadTimeoutInMillis() throws Exception {
        int millis = HedwigConfig.getInstance().readTimeoutInMillis();
        assertEquals(3000, millis);
        millis = HedwigConfig.getInstance().readTimeoutInMillis();
        assertEquals(3000, millis);
        int expected = 1000;
        System.setProperty(RpcConfig.HEDWIG_RPC_READ_TIMEOUT_IN_MILLIS, String.valueOf(expected));
        millis = HedwigConfig.getInstance().readTimeoutInMillis();
        assertEquals(expected, millis);
        expected = 10;

        System.setProperty(RpcConfig.HEDWIG_RPC_READ_TIMEOUT_IN_SECONDS, String.valueOf(expected));
        int value = HedwigConfig.getInstance().readTimeoutInMillis();
        assertEquals(millis, value);

        System.clearProperty(RpcConfig.HEDWIG_RPC_READ_TIMEOUT_IN_MILLIS);
        value = HedwigConfig.getInstance().readTimeoutInMillis();
        assertEquals(expected * 1000, value);
    }

    @Test
    public void testWriteTimeoutInMillis() throws Exception {
        int millis = HedwigConfig.getInstance().writeTimeoutInMillis();
        assertEquals(3000, millis);
        millis = HedwigConfig.getInstance().writeTimeoutInMillis();
        assertEquals(3000, millis);
        int expected = 1000;
        System.setProperty(RpcConfig.HEDWIG_RPC_WRITE_TIMEOUT_IN_MILLIS, String.valueOf(expected));
        millis = HedwigConfig.getInstance().writeTimeoutInMillis();
        assertEquals(expected, millis);
        expected = 10;

        System.setProperty(RpcConfig.HEDWIG_RPC_WRITE_TIMEOUT_IN_SECONDS, String.valueOf(expected));
        int value = HedwigConfig.getInstance().writeTimeoutInMillis();
        assertEquals(millis, value);

        System.clearProperty(RpcConfig.HEDWIG_RPC_WRITE_TIMEOUT_IN_MILLIS);
        value = HedwigConfig.getInstance().writeTimeoutInMillis();
        assertEquals(expected * 1000, value);
    }

    @Test
    public void testConnectTimeoutInMillis() throws Exception {
        int millis = HedwigConfig.getInstance().connectTimeoutInMillis();
        assertEquals(5000, millis);
        int expected = 1000;
        System.setProperty(RpcConfig.HEDWIG_RPC_CONNECT_TIMEOUT_IN_MILLIS, String.valueOf(expected));
        millis = HedwigConfig.getInstance().connectTimeoutInMillis();
        assertEquals(expected, millis);
        expected = 10;

        System.setProperty(RpcConfig.HEDWIG_RPC_CONNECT_TIMEOUT_IN_SECONDS, String.valueOf(expected));
        int value = HedwigConfig.getInstance().connectTimeoutInMillis();
        assertEquals(millis, value);

        System.clearProperty(RpcConfig.HEDWIG_RPC_CONNECT_TIMEOUT_IN_MILLIS);
        value = HedwigConfig.getInstance().connectTimeoutInMillis();
        assertEquals(expected * 1000, value);

    }
}

