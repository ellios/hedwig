package me.ellios.hedwig.rpc.tracer;

import com.yammer.metrics.Metrics;
import me.ellios.hedwig.registry.CallbackWatcher;
import me.ellios.hedwig.registry.RegistryHelper;
import me.ellios.hedwig.rpc.core.ServiceConfig;
import me.ellios.hedwig.rpc.core.ServiceNode;
import me.ellios.hedwig.rpc.core.ServiceType;
import me.ellios.hedwig.zookeeper.DataListener;
import me.ellios.hedwig.zookeeper.ZooType;
import me.ellios.hedwig.zookeeper.ZookeeperClient;
import me.ellios.hedwig.zookeeper.ZookeeperClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/1/13 10:27 AM
 */
public class ZookeeperReporterTest {
    private static final Logger LOG = LoggerFactory.getLogger(ZookeeperReporterTest.class);
    ZookeeperClient client = ZookeeperClientFactory.getZookeeperClient(ZooType.SERVICE);

    private ExecutorService pool = Executors.newCachedThreadPool();

    @Test
    public void testEnable() throws Exception {
        ServiceConfig config = ServiceConfig.newBuilder()
                .serviceFace(ZookeeperReporterTest.class)
                .serviceImpl(ZookeeperReporterTest.class)
                .type(ServiceType.THRIFT)
                .build();
        final int count = 200;
        final ServiceNode node = ServiceNode.createServiceNode(config);
        client.create(node.getZnodePath(), true, node.toJson());
        ZookeeperReporter.enable(node, 1, TimeUnit.SECONDS);
        pool.execute(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < count; i++) {
                    Metrics.newCounter(ZookeeperReporter.METRIC_NAME).inc();
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        LOG.warn("Something wrong", e);
                    }
                }
            }
        });
        //Child changed listener.
        RegistryHelper.subscribe(node.getName(), new CallbackWatcher() {
            @Override
            public void doCallback(List<ServiceNode> urls) {
                LOG.info("#Children {}", urls);
            }
        });

        pool.execute(new Runnable() {
            @Override
            public void run() {
                client.getData(node.getZnodePath(), new DataListener() {
                    @Override
                    public void dataChanged(String path, byte[] data) {
                        ServiceNode n = ServiceNode.fromJson(data);
                        LOG.info("#Data {}", n);
                    }
                });
            }
        });
        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.DAYS);
    }

    @Test
    public void testEnableDefault() throws Exception {

    }
}
