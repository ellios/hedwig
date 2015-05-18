package me.ellios.hedwig.zookeeper;

import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.*;

/**
 * Author: ellios
 * Date: 12-10-30 Time: 上午11:01
 */
public class ZookeeperClientTest {
    private static final Logger LOG = LoggerFactory.getLogger(ZookeeperClientTest.class);
    private ZookeeperClient zookeeperClient = ZookeeperClientFactory.getZookeeperClient();
    String parent;
    String child;

    @BeforeMethod
    public void setUp() throws Exception {
        parent = "/root/parent/dir-" + ThreadLocalRandom.current().nextInt(65535);
        child = parent.concat("/").concat("node");
    }

    @Test
    public void testCreateIfExist2() throws Exception {
        while (!zookeeperClient.exist(child)) {
            zookeeperClient.create(child, true);
        }
        DataListener dataListener = new DataListener() {
            @Override
            public void dataChanged(String path, byte[] data) {
                LOG.info("Data: {}", new String(data, 0, data.length));
            }
        };
        zookeeperClient.getData(child, dataListener);
        zookeeperClient.create(child, true);
        zookeeperClient.create(child, false);
        cleanup();
    }

    @Test
    public void testCreateIfExist() throws Exception {
        while (!zookeeperClient.exist(child)) {
            zookeeperClient.create(child, false);
        }
        zookeeperClient.create(child, false);
        zookeeperClient.create(child, true);
        zookeeperClient.delete(child);
        cleanup();
    }

    @Test
    public void testCreate() throws Exception {
        zookeeperClient.create(child, false);
        List<String> children
                = zookeeperClient.getChildren(parent);
        assertNotNull(children);
        assertEquals(children.size(), 1);
        LOG.info("{}", zookeeperClient.getChildren(parent));
        cleanup();
    }

    @Test
    public void testChildListener() throws Exception {
        final AtomicInteger counter = new AtomicInteger(0);
        zookeeperClient.create(parent, false);
        // Child changed events can be merged.
        final AtomicInteger pre = new AtomicInteger(0);
        ChildListener listener = new ChildListener() {
            @Override
            public void childChanged(String path, List<String> children) {
                int delta = children.size() - pre.get();
                counter.addAndGet(delta);
                LOG.info("Path: {}, Children added: {}, total: {}", path, delta, children.size());
                pre.set(children.size());

            }
        };
        zookeeperClient.addChildListener(parent, listener);

        int round = 20;
        final CountDownLatch latch = new CountDownLatch(round);
        ExecutorService pool = Executors.newFixedThreadPool(10);
        for (int i = 0; i < round; i++) {
            final int index = i;
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        zookeeperClient.create(parent.concat("/node-").concat(String.valueOf(index)), false);
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        latch.await();
        Thread.sleep(2000);
        assertEquals(counter.get(), round);
        final CountDownLatch l2 = new CountDownLatch(round);
        for (int i = 0; i < round; i++) {
            final int index = i;
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        zookeeperClient.delete(parent.concat("/node-").concat(String.valueOf(index)));
                    } finally {
                        l2.countDown();
                    }
                }
            });
        }
        l2.await();
        Thread.sleep(1000);
        assertEquals(counter.get(), 0);
        cleanup();
    }


    @Test
    public void testSetData() throws Exception {
        StringBuilder builder = new StringBuilder();
        for (long i = 0; i < 1 * 512 * 1024L; i++) {
            builder.append(1);
        }
        String path = parent.concat("/barrier");
        zookeeperClient.create(path, true);
        LOG.info("data length: {}", builder.length());
        zookeeperClient.setData(path, builder.toString().getBytes());
        cleanup();
    }

    @Test
    public void testRemoveWatch() throws Exception {
        zookeeperClient.create(parent, false);
        ChildListener listener = new ChildListener() {
            @Override
            public void childChanged(String path, List<String> children) {
                LOG.info("{},{}", path, children);
            }
        };
        // We need to remove listeners before deleting the node.
        zookeeperClient.addChildListener(parent, listener);
        zookeeperClient.removeChildListener(parent, listener);

        zookeeperClient.delete(parent);
    }

    private void cleanup() throws Exception {
        cleanup(child);
        cleanup(parent);
    }

    /**
     * clean up specific path
     *
     * @param path znode path
     * @throws Exception something bad happened.
     */
    private void cleanup(String path) throws Exception {
        Thread.sleep(1000);
        // We cannot delete a non-empty directory.
        if (zookeeperClient.exist(path)) {
            List<String> children = zookeeperClient.getChildren(path);
            for (String child : children) {
                zookeeperClient.delete(path.concat("/").concat(child));
            }
            Set<ChildListener> childListeners = zookeeperClient.getChildListeners(path);
            if (null != childListeners) {
                for (ChildListener listener : childListeners) {
                    zookeeperClient.removeChildListener(path, listener);
                }
            }
            Set<DataListener> dataListeners = zookeeperClient.getDataListeners(path);
            if (null != dataListeners) {
                for (DataListener dataListener : dataListeners) {
                    zookeeperClient.removeDataListener(path, dataListener);
                }
            }
            Set<StateListener> stateListeners = zookeeperClient.getStateListeners();
            if (null != stateListeners) {
                for (StateListener listener : stateListeners) {
                    zookeeperClient.removeStateListener(listener);
                }
            }
            zookeeperClient.delete(path);
        }
    }

    @Test
    public void testGetData() throws Exception {
        byte[] testData = new byte[]{0x23, 0x70};
        String node = parent + "/node";
        Thread.sleep(1000);
        zookeeperClient.create(parent, false);
        zookeeperClient.create(node, true);
        final AtomicBoolean childChanged = new AtomicBoolean(false);
        final AtomicBoolean dataChanged = new AtomicBoolean(false);
        byte[] data = zookeeperClient.getData(node, new DataListener() {
            @Override
            public void dataChanged(String path, byte[] data) {
                dataChanged.compareAndSet(false, true);
                LOG.info("data changed. path : {} data : {}", path, new String(data));
            }
        });
        //The default value of the znode is the client ip address.
        assertNotNull(data);
        LOG.info("Client Ip: {}", new String(data));
        ChildListener listener = new ChildListener() {
            @Override
            public void childChanged(String path, List<String> children) {
                childChanged.compareAndSet(false, true);
                LOG.info("child changed. path : {} children : {}", path, children);
            }
        };
        zookeeperClient.addChildListener(parent, listener);
        zookeeperClient.setData(node, testData);
        Thread.sleep(1000);
        assertTrue(dataChanged.getAndSet(false));

        zookeeperClient.create(parent + "/node-2", true);
        Thread.sleep(1000);
        assertTrue(childChanged.getAndSet(false));
        zookeeperClient.delete(parent + "/node-2");
        Thread.sleep(1000);
        assertTrue(childChanged.get());
        cleanup();
    }

    private String getAclDataFromZookeeper() {
        // root=/hedwig/dev/service/netscape
        //System.setProperty("hedwig.zk.ips", "10.15.177.69:2181");
        ZookeeperClientFactory.clear();
        ZookeeperClient client = ZookeeperClientFactory.getZookeeperClient();
        assertTrue(client.exist("acltest"));
        String data = null;
        try {
            data = new String(client.getData("acltest"));
        } catch (Exception e) {
        }
        return data;
    }

    @Test(enabled = false)
    public void testZookeeperClientACLWithWold() throws Exception {
        clearAuthInfo();
        String data = getAclDataFromZookeeper();
        assertNull(data);
    }

    @Test(enabled = false)
    public void testZookeeperClientACLWithDigest() throws Exception {
        System.setProperty("hedwig.zk.acl.user", "test");
        System.setProperty("hedwig.zk.acl.password", "123456");
        String data = getAclDataFromZookeeper();
        assertNotNull(data);
    }

    @Test(enabled = false)
    public void testZookeeperClientACLWithIP() throws Exception {
        // first you must make sure that zookeeper's acl to use.
        clearAuthInfo();
        String data = getAclDataFromZookeeper();
        assertNull(data);
    }

    @Test
    public void testZookeeperACLDigestGenerate() {
        try {
            assertEquals("test:PbXQT4DQMDcaYC1X0EY0B2RZCwM=", DigestAuthenticationProvider.generateDigest("test:123456"));
        } catch (NoSuchAlgorithmException e) {
        }
    }

    @Test
    public void aclSetTest() {
        try {
            System.setProperty("hedwig.zk.acl.user", "ugc_ugc");
            System.setProperty("hedwig.zk.acl.password", "0lsI5C45DWYFy258");
            ZookeeperClientFactory.clear();
            ZookeeperClient client = ZookeeperClientFactory.getZookeeperClient();
            List<String> digests = Arrays.asList(
                    "vrs_mixer:4PCPXeCXbsl3fOB8",
                    "lego_ugc:zardp3CmyRDPk5vM",
                    "openapi_ugc:MEr48nSyiu1MTr8o",
                    "up_down_ugc:A5qLwFqz3v9h49",
                    "sns_ugc:5FP6HgEgt6fh2CDR",
                    "vis-apis:fV8YabNQeV1xZgNF",
                    "vis_intranet:2UA2XG0fSzfZsEiz",
                    "vis_external_cooperation:XIFb1e9rY7MDmz4k",
                    "top_recommend:X6pjZGKlzSgygV1c",
                    "vrs_h5:n1mK59X9IBujBs9A"
            );
            List<ACL> acls = new ArrayList<ACL>();
            for (String digest : digests) {
                Id id = new Id("digest", DigestAuthenticationProvider.generateDigest(digest));
                ACL acl = new ACL(ZooDefs.Perms.READ, id);
                acls.add(acl);
            }

            // root acl
            Id rootId = new Id("digest", DigestAuthenticationProvider.generateDigest("ugc_ugc:0lsI5C45DWYFy258"));
            ACL rootAcl = new ACL(ZooDefs.Perms.ALL, rootId);
            acls.add(rootAcl);

            //client.withACL("/hedwig/prod/service/intra/T_TUgcVideoService", acls);
            //client.withACL("/hedwig/test/service/continuous/T_TUgcVideoService", acls);
            client.withACL("/T_TUgcVideoService", acls);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test(enabled = false)
    public void aclCleanTest() {
        try {
            System.setProperty("hedwig.zk.acl.user", "ugc");
            System.setProperty("hedwig.zk.acl.password", "uTRYk8Nv0Y9Zcq94");
            ZookeeperClientFactory.clear();
            ZookeeperClient client = ZookeeperClientFactory.getZookeeperClient();

            List<ACL> acls = new ArrayList<ACL>();
            // root acl
            Id rootId = new Id("world", "anyone");
            ACL rootAcl = new ACL(ZooDefs.Perms.ALL, rootId);
            acls.add(rootAcl);

            //client.withACL("/hedwig/prod/service/intra/T_TUgcVideoService", acls);
            client.withACL("/hedwig/test/service/continuous/T_TUgcVideoService", acls);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearAuthInfo() {
        System.setProperty("hedwig.zk.acl.user", "");
        System.setProperty("hedwig.zk.acl.password", "");
    }
}
