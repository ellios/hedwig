package me.ellios.hedwig.zookeeper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

/**
 * Say something?
 *
 * @author George Cao
 * @since 3/14/13 5:21 PM
 */
public class ZookeeperClientFactoryTest {
    private static final Logger LOG = LoggerFactory.getLogger(ZookeeperClientFactoryTest.class);

    @Test
    public void testGetZookeeperClient() throws Exception {
        ZookeeperClient client = ZookeeperClientFactory.getZookeeperClient();
        assertTrue(!client.exist("/hedwig/dev"));
        ExecutorService pool = Executors.newCachedThreadPool();
        int round = 1000;
        final ZookeeperClient pre = client;
        while (round-- > 0) {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    ZookeeperClient client = ZookeeperClientFactory.getZookeeperClient();
                    assertEquals(client, pre);
                }
            });
        }
        pool.shutdown();
        pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    }

    @Test
    public void testGetZookeeperClientWithType() throws Exception {
        ZookeeperClient c1 = ZookeeperClientFactory.getZookeeperClient("JMX");
        ZookeeperClient c2 = ZookeeperClientFactory.getZookeeperClient("jmx");
        assertEquals(c1, c2);
        c2 = ZookeeperClientFactory.getZookeeperClient("Redis");
        assertNotEquals(c1, c2);
        c2 = ZookeeperClientFactory.getZookeeperClient("Flush");
        c1 = ZookeeperClientFactory.getZookeeperClient();
        assertNotEquals(c1, c2);
    }

    @Test
    public void testGetClientByZooType() throws Exception {
        for (ZooType type : ZooType.values()) {
            ZookeeperClient client = ZookeeperClientFactory.getZookeeperClient(type);
            assertNotNull(client);
        }

    }
}
