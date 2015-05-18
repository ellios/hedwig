package me.ellios.hedwig.zookeeper;


import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import me.ellios.hedwig.zookeeper.config.ZookeeperConfig;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.*;

import static org.testng.Assert.*;

/**
 * Author: ellios
 * Date: 12-11-13 Time: 下午3:28
 */
public class CuratorTest {
    static ExecutorService pool = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("init-#%d")
            .build());
    private CuratorFramework chrootClient;
    private static volatile CuratorFramework rawClient;
    private static final Logger LOG = LoggerFactory.getLogger(CuratorTest.class);
    String namespace = "fun";
    String chroot = "/curator/dev/test";

    static {
        try {
            rawClient = newClient("");
        } catch (Exception e) {
            LOG.warn("Something wrong", e);
        }
    }

    @BeforeMethod
    public void setUp() throws Exception {
        // Create the chroot path.
        if (null == rawClient.checkExists().forPath(chroot)) {
            rawClient.create().creatingParentsIfNeeded().forPath(chroot);
        }
        chrootClient = newClient(chroot, ZookeeperConfig.getNamespaceString()).get();
    }

    public static CuratorFramework newClient(String ns) throws Exception {
        return newClient("", ns).get();
    }

    public static Future<CuratorFramework> newClient(final String chroot, final String ns) throws Exception {
        return pool.submit(new Callable<CuratorFramework>() {
            @Override
            public CuratorFramework call() throws Exception {
                CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                        .connectString(ZookeeperConfig.getConnectString() + chroot)
                        .retryPolicy(new RetryNTimes(Integer.MAX_VALUE, 1000))
                        .connectionTimeoutMs(50000);
                if (!Strings.isNullOrEmpty(ns)) {
                    builder.namespace(ns);
                }
                CuratorFramework client = builder.build();
                final CountDownLatch latch = new CountDownLatch(1);
                client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
                    @Override
                    public void stateChanged(CuratorFramework client, ConnectionState newState) {
                        latch.countDown();
                    }
                });
                client.start();
                latch.await();
                return client;
            }
        });
    }

    private void cleanUp(String start) throws Exception {
        List<String> children = rawClient.getChildren().forPath(start);
        // Clean up all children if exists.
        if (null != children && children.size() > 0) {
            for (String path : children) {
                cleanUp(start + "/" + path);
            }
        }
        // Clean himself.
        LOG.info("Delete {}", start);
        rawClient.delete().guaranteed().forPath(start);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        // let the  EPHEMERAL die first.
        chrootClient.close();
        cleanUpAll(chroot);
        chrootClient.close();
    }

    private void cleanUpAll(String start) throws Exception {
        if (start.startsWith("/")) {
            start = start.substring(1);
        }
        String[] paths = start.split("/");
        if (null != paths && paths.length > 0) {
            int unsafe = -1;
            for (int i = 0; i < paths.length; i++) {
                if (i == 0 && "hedwig".equalsIgnoreCase(paths[0])) {
                    unsafe = i;
                }
                if (0 == unsafe && i == 1 && ("dev".equalsIgnoreCase(paths[1])
                        || "prod".equalsIgnoreCase(paths[1]) ||
                        "test".equalsIgnoreCase(paths[1]))) {
                    unsafe = i;
                }
                if (1 == unsafe && i == 2 && ("service".equalsIgnoreCase(paths[2])
                        || "redis".equalsIgnoreCase(paths[2])
                        || "jms".equalsIgnoreCase(paths[2])
                        || "leader".equalsIgnoreCase(paths[2])
                        || "recipes".equalsIgnoreCase(paths[2]))) {
                    unsafe = i;
                }
            }
            if (unsafe + 1 >= paths.length) {
                List<String> children = rawClient.getChildren().forPath(start);
                for (String child : children) {
                    cleanUp(child);
                }
            } else {
                StringBuilder buffer = new StringBuilder();
                for (int i = 0; i <= unsafe + 1; i++) {
                    buffer.append("/").append(paths[i]);
                }
                cleanUp(buffer.toString());
            }
        }
    }

    @Test
    public void testSetNameSpace() throws Exception {
        // Normal String.
        ZookeeperConfig.setNamespaceString(namespace);
        LOG.info("namespace: {}", namespace);
        assertEquals(ZookeeperConfig.getNamespaceString(), namespace);
        // Empty String.
        try {
            ZookeeperConfig.setNamespaceString("");
            fail("Empty String.");
        } catch (Exception ignored) {
        }
        // Null String.
        try {
            ZookeeperConfig.setNamespaceString(null);
            fail("Null String.");
        } catch (Exception ignored) {
        }
        // Blank String.
        try {
            ZookeeperConfig.setNamespaceString("    ");
            fail("Blank String.");
        } catch (Exception ignored) {
        }
    }

    @Test(enabled = false)
    public void testNamespace() throws Exception {
        // chroot
        Stat stat = chrootClient.checkExists().forPath("");
        assertNotNull(stat);
        long owner = stat.getEphemeralOwner();
        // chroot is  a PERSISTENT znode.
        assertEquals(owner, 0);
        String path = chrootClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("that");
        // namespace not included in the path.
        assertFalse(path.contains(ZookeeperConfig.getNamespaceString()));

        // Namespace appended.
        stat = chrootClient.checkExists().forPath(namespace);
        assertNull(stat);

        // With namespace
        String znode = "this";
        CuratorFramework newClient = newClient(chroot, namespace).get();
        path = newClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(znode);
        assertNotNull(path);
        // namespace not included in the path.
        assertFalse(path.contains(namespace));
        assertTrue(path.endsWith(znode));
        // check namespace znode
        stat = rawClient.checkExists().forPath(chroot + "/" + namespace);
        assertNotNull(stat);
        newClient.close();
        //EPHEMERAL znode disappears when the client was closed(session invalided).
        stat = rawClient.checkExists().forPath(chroot + "/" + namespace + "/" + znode);
        assertNull(stat);
    }


    @Test
    public void test() throws Exception {
        int round = 40;
        Stat stat = chrootClient.checkExists().forPath(chroot);
        assertNull(stat);
        for (int i = 0; i < round; i++) {
            String path = (chrootClient.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(chroot + "/node-" + i, new byte[]{0x23, 0x34}));
            assertNotNull(path);
            LOG.info("Node: {}", path);
        }

        List<String> children = chrootClient.getChildren().forPath(chroot);
        LOG.info("Children: {}", children);
        for (int i = 0; i < round; i++) {
            byte[] data = chrootClient.getData().forPath(chroot + "/node-" + i);
            assertEquals(2, data.length);
        }
    }

    @Test
    public void testLock() throws Exception {
        String path = chrootClient.create()
                .creatingParentsIfNeeded()
                .withProtection()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(chroot.concat("/hello"));
        assertNotNull(path);
    }

    @Test
    public void testGetChildren() throws Exception {
        String child = chroot.concat("/child");
        chrootClient.create().creatingParentsIfNeeded().forPath(child);
        List<String> list = chrootClient.getChildren().forPath(chroot);
        assertNotNull(list);
        assertEquals(list.size(), 1);
        LOG.info("children: {}", list);
    }

    @Test
    public void testCreateSequence() throws Exception {
        int round = 200;
        for (int i = 0; i < round; i++) {
            String path = chrootClient
                    .create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                    .forPath("/pp");
            assertNotNull(path);
            LOG.info("{}", path);
        }
    }
}
