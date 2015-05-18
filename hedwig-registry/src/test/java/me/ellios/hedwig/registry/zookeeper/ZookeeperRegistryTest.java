package me.ellios.hedwig.registry.zookeeper;

import me.ellios.hedwig.rpc.core.ServiceConfig;
import me.ellios.hedwig.rpc.core.ServiceNode;
import me.ellios.hedwig.rpc.core.ServiceType;
import me.ellios.hedwig.registry.RegistryHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Say something?
 *
 * @author George Cao
 * @since 3/20/13 11:18 AM
 */
public class ZookeeperRegistryTest {
    private static final Logger LOG = LoggerFactory.getLogger(ZookeeperRegistryTest.class);

    @Test
    public void testDoLookup() throws Exception {
        ServiceConfig config = ServiceConfig.newBuilder()
                .serviceFace(getClass())
                .serviceImpl(getClass())
                .serviceGroup("group")
                .type(ServiceType.THRIFT)
                .build();
        ServiceNode node = ServiceNode.createServiceNode(config);
        RegistryHelper.register(node);
        RegistryHelper.unregister(node);
    }
}
