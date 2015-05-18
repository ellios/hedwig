package me.ellios.hedwig.registry;

import me.ellios.hedwig.rpc.core.ServiceConfig;
import me.ellios.hedwig.rpc.core.ServiceNode;
import me.ellios.hedwig.rpc.core.ServiceType;
import me.ellios.hedwig.registry.zookeeper.ZookeeperRegistryFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Author: ellios
 * Date: 12-10-30 Time: 下午4:59
 */
public class RegistryTest {

    private static RegistryFactory factory = ZookeeperRegistryFactory.getInstance();
    private static Registry registry = factory.getRegistry();

    @Test
    public void test() throws Exception {
        ServiceConfig serviceConfig = ServiceConfig.newBuilder().type(ServiceType.PROTOBUF).serviceFace(getClass()).serviceImpl(getClass()).build();
        ServiceNode serviceNode = ServiceNode.createServiceNode(serviceConfig);
        registry.register(serviceNode);
        CallbackWatcher watcher = new CallbackWatcher() {
            @Override
            public void doCallback(List<ServiceNode> nodes) {
                Assert.assertNotNull(nodes);
                ServiceNode node = nodes.get(0);
                System.out.println(node);
                System.out.println(RegistryTest.class);
                Assert.assertEquals(RegistryTest.class.getName(), node.getServiceFace());
                Assert.assertEquals(RegistryTest.class.getName(), node.getServiceImpl());
            }
        };
        registry.subscribe(serviceNode.getName(), watcher);
        registry.unsubscribe(serviceNode.getName(), watcher);
        registry.unregister(serviceNode);
        registry.subscribe(serviceNode.getName(), new CallbackWatcher() {
            @Override
            public void doCallback(List<ServiceNode> nodes) {
                Assert.assertTrue(nodes.size() == 0);
            }
        });

    }
}
