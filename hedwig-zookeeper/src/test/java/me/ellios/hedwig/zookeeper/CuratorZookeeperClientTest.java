package me.ellios.hedwig.zookeeper;

import org.apache.zookeeper.client.HostProvider;
import org.apache.zookeeper.client.StaticHostProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Say something?
 *
 * @author George Cao
 * @since 3/15/13 2:11 PM
 */
public class CuratorZookeeperClientTest {
    private static final Logger LOG = LoggerFactory.getLogger(CuratorZookeeperClientTest.class);

    @Test
    public void testLiteralIPRevreseDNSLookup() throws Exception {
        int size = 10;
        HostProvider hostProvider = getHostProviderUnresolved(size);
        for (int i = 0; i < size; i++) {
            InetSocketAddress next = hostProvider.next(0);
            assertTrue(next instanceof InetSocketAddress);
            assertTrue(!next.isUnresolved());
            assertTrue(!next.getAddress().toString().startsWith("/"));
            // Do NOT trigger the reverse DNS lookup, so it should be very fast.
            // Just a simple method call, assume this need 50 ms to finish at max.
            String hostname = next.getHostName();
            assertEquals(hostname, next.getHostString());
        }
    }


    private StaticHostProvider getHostProviderUnresolved(int size)
            throws UnknownHostException {
        ArrayList<InetSocketAddress> list = new ArrayList<>(size);
        while (size > 0) {
            list.add(InetSocketAddress.createUnresolved("10.10.10." + size, 1234));
            --size;
        }
        return new StaticHostProvider(list);
    }
}
