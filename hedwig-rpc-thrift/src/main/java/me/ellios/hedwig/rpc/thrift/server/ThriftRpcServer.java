package me.ellios.hedwig.rpc.thrift.server;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.reporting.JmxReporter;
import me.ellios.hedwig.rpc.core.ServiceConfig;
import me.ellios.hedwig.rpc.server.support.AbstractRpcServer;
import me.ellios.hedwig.rpc.thrift.ThriftServiceDef;
import me.ellios.hedwig.rpc.tracer.ZookeeperReporter;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static me.ellios.hedwig.rpc.thrift.ThriftServiceDef.aggregate;

/**
 * @author ellios
 *         Date: 12-11-1 Time: 下午3:03
 */
public class ThriftRpcServer extends AbstractRpcServer {

    private static final Logger LOG = LoggerFactory.getLogger(ThriftRpcServer.class);
    private final ChannelGroup allChannels = new DefaultChannelGroup();
    private ExecutorService bossExecutor;
    private ExecutorService workerExecutor;

    public void start() {
        // Start the Jmx Statistics Reporter.
        JmxReporter.startDefault(Metrics.defaultRegistry());
        Set<Integer> ports = getPortGroups();
        for (int port : ports) {
            Collection<ServiceConfig> services = getServiceConfigs(port);
            bossExecutor = newCachedThreadPool("hedwig-thrift-rpc-boss-#%d");
            workerExecutor = newCachedThreadPool("hedwig-thrift-rpc-worker-#%d");
            ServerBootstrap bootstrap
                    = new ServerBootstrap(new NioServerSocketChannelFactory(bossExecutor, workerExecutor));
            bootstrap.setOptions(getNettyOptions());
            ThriftServiceDef aggregate = aggregate(services);
            SocketAddress address = new InetSocketAddress(aggregate.getServerHost(), aggregate.getServerPort());
            bootstrap.setPipelineFactory(new ThriftServerPipelineFactory(aggregate, allChannels));
            Channel channel = bootstrap.bind(address);
            allChannels.add(channel);
            publish(services);
        }
        // Start the Zookeeper connections count reporter.
        // ZookeeperReporter.enable(ServiceNode.createServiceNode(config), 3, TimeUnit.SECONDS);
    }

    public void stop() {
        hideAll();
        // stop bosses
        if (bossExecutor != null) {
            shutdownExecutor(bossExecutor, "bossExecutor");
            bossExecutor = null;
        }

        try {
            allChannels.close();
        } catch (Exception e) {
            LOG.warn("ignored exception while shutting down channels", e);
        }

        // finally the reader writer
        if (workerExecutor != null) {
            shutdownExecutor(workerExecutor, "workerExecutor");
            workerExecutor = null;
        }

        // stop the Jmx reporter
        JmxReporter.shutdownDefault();
        // Stop the zookeeper reporter
        ZookeeperReporter.shutdownDefault();
    }
}
