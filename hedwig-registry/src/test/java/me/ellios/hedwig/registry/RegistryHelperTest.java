package me.ellios.hedwig.registry;

import me.ellios.hedwig.rpc.core.*;
import me.ellios.hedwig.zookeeper.ZooType;
import me.ellios.hedwig.zookeeper.ZookeeperClient;
import me.ellios.hedwig.zookeeper.ZookeeperClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

/**
 * Say something?
 *
 * @author George Cao
 * @since 5/21/13 10:18 AM
 */
public class RegistryHelperTest {
    private static final Logger LOG = LoggerFactory.getLogger(RegistryHelperTest.class);
    private ZookeeperClient client = ZookeeperClientFactory.getZookeeperClient(ZooType.SERVICE);

    ServiceNode node;
    ServiceConfig.Builder builder;

    @BeforeMethod
    public void setUp() throws Exception {
        builder = ServiceConfig.newBuilder()
                .schema(ServiceSchema.TCP)
                .type(ServiceType.THRIFT)
                .serviceFace(ServiceFace.class)
                .serviceImpl(ServiceImpl.class)
                .port(9009);
        ServiceConfig config = builder.build();
        node = ServiceNode.createServiceNode(config);

    }

    @Test(groups = "normal")
    public void testRegister() throws Exception {
        // Normal registration.
        RegistryHelper.register(node);
        assertTrue(client.exist(node.getZnodePath()));
        // Duplicated registration, should delete previous one first.
        RegistryHelper.register(node);
        assertTrue(client.exist(node.getZnodePath()));
        // Normal un-registration
        RegistryHelper.unregister(node);
        assertTrue(!client.exist(node.getZnodePath()));
        // Un-registration a non-exists one
        RegistryHelper.unregister(node);
        assertTrue(!client.exist(node.getZnodePath()));
    }

    private volatile boolean notified = false;
    private volatile int size = 0;

    @Test(groups = "normal")
    public void testSubscribe() throws Exception {
        RegistryHelper.register(node);
        assertTrue(client.exist(node.getZnodePath()));
        String serviceName = ServiceConfigHelper.buildDefaultThriftServiceName(ServiceFace.class);
        CallbackWatcher watcher = new CallbackWatcher() {
            @Override
            public void doCallback(List<ServiceNode> urls) {
                LOG.info("Callback got: {}->{}", urls.size(), urls);
                size = urls.size();
                notified = true;
            }
        };
        RegistryHelper.subscribe(serviceName, watcher);
        // First attempt to subscribe the service will call the CallbackWatcher directly.
        notified = false;
        // Remove one child.
        RegistryHelper.unregister(node);
        checkThenUnset();
        assertEquals(size, 0);

        // Add one child
        RegistryHelper.register(node);
        checkThenUnset();
        assertEquals(size, 1);
        ServiceNode newNode = ServiceNode.createServiceNode(builder.port(9008).build());
        RegistryHelper.register(newNode);
        checkThenUnset();
        assertEquals(size, 2);

        // Un-subscribe
        RegistryHelper.unsubscribe(serviceName, watcher);
        RegistryHelper.unregister(newNode);
        assertTrue(!notified);
        RegistryHelper.unregister(node);
        assertTrue(!notified);
    }

    private void checkThenUnset() throws InterruptedException {
        int c = 0;
        // Wait for 5 seconds.
        while (!notified && c++ < 10) {
            TimeUnit.MILLISECONDS.sleep(500);
        }
        assertTrue(notified);
        notified = false;
    }

    @Test(groups = "normal")
    public void testLookup() throws Exception {
        String serviceName = ServiceConfigHelper.buildDefaultThriftServiceName(ServiceFace.class);
        List<ServiceNode> nodes = RegistryHelper.lookup(serviceName);
        assertNotNull(nodes);
        assertEquals(nodes.size(), 0);

        RegistryHelper.register(node);
        nodes = RegistryHelper.lookup(serviceName);
        assertNotNull(nodes);
        assertEquals(nodes.size(), 1);
        RegistryHelper.unregister(node);
    }

    @Test(dependsOnGroups = "normal")
    public void testDestroy() throws Exception {
        RegistryHelper.destroy();
    }
}
