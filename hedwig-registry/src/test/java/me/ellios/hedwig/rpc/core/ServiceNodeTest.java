package me.ellios.hedwig.rpc.core;

import com.alibaba.fastjson.JSON;
import me.ellios.hedwig.rpc.core.ServiceConfig;
import me.ellios.hedwig.rpc.core.ServiceNode;
import me.ellios.hedwig.rpc.core.ServiceSchema;
import me.ellios.hedwig.rpc.core.ServiceType;
import me.ellios.hedwig.zookeeper.config.ZookeeperConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Say something?
 *
 * @author George Cao
 * @since 13-3-12 下午3:08
 */

public class ServiceNodeTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceNodeTest.class);

    @Test
    public void test() {
        ServiceConfig serviceConfig = ServiceConfig.newBuilder()
                .serviceFace(getClass())
                .serviceImpl(getClass())
                .type(ServiceType.PROTOBUF)
                .build();
        ServiceNode serviceNode = ServiceNode.createServiceNode(serviceConfig);
        serviceNode.toJson();
        String jsonStr = JSON.toJSONString(serviceNode);
        LOGGER.info("{}", serviceNode);
        LOGGER.info("{}", jsonStr);
        ServiceNode node2 = ServiceNode.fromJson(serviceNode.toJson());
        assertEquals(serviceNode, node2);
        assertEquals(0, node2.getConnections());
        assertEquals(1, node2.getWeight());
    }

    @Test
    public void testGroupName() throws Exception {
        String group = ZookeeperConfig.getNamespaceString();
        ServiceConfig config = ServiceConfig.newBuilder()
                .serviceFace(this.getClass())
                .serviceImpl(this.getClass())
                .type(ServiceType.THRIFT)
                .serviceGroup(group)
                .build();
        ServiceNode node = ServiceNode.createServiceNode(config);
        assertEquals(ServiceType.THRIFT, node.getType());
        assertEquals(ServiceSchema.TCP, node.getSchema());
    }
}