package me.ellios.hedwig.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Author: ellios
 * Date: 12-11-1 Time: 上午10:26
 */
public class NetworkUtilsTest {
    private static final Logger LOG = LoggerFactory.getLogger(NetworkUtilsTest.class);

    @Test
    public void testGetEth0Address() {
        String address = NetworkUtils.getEth0Address();
        assertNotNull(address);
        LOG.info("{}", address);
        assertTrue(address.startsWith("192.") || address.startsWith("10."));
    }

    @Test
    public void testLocalHost() throws Exception {
        String host = NetworkUtils.getLocalHost();
        assertNotNull(host);
        LOG.info("{}", host);
    }

    @Test
    public void testGetLocalAddress() throws Exception {
        String address = NetworkUtils.getLocalAddress();
        assertNotNull(address);
        LOG.info("{}", address);
    }

    @Test
    public void test() {
        LOG.info("{}", NetworkUtils.class.getSimpleName());
    }
}

