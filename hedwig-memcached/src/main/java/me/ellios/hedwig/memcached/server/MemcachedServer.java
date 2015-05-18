package me.ellios.hedwig.memcached.server;

import com.google.common.base.Preconditions;
import me.ellios.hedwig.http.container.ContainerBuilder;
import me.ellios.hedwig.memcached.container.NettyMemcachedContainer;
import me.ellios.hedwig.rpc.server.support.AbstractRpcServer;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;

/**
 * The server that speaks memcached binary protocol.
 *
 * @author George Cao(caozhangzhi@qiyi.com)
 * @since 2014-01-26 14
 */
public class MemcachedServer extends AbstractRpcServer {
    private static final Logger LOG = LoggerFactory.getLogger(MemcachedServer.class);

    @Override
    public void start() {
        Preconditions.checkArgument(isServiceRegistered(), "You should at least register one service.");
        try {
            for (Integer port : getPortGroups()) {
                Class<?>[] resources = getServiceResources(port);
                ExecutorService bossThreadPool = newCachedThreadPool("memcached-server-{0}-boss-#%d", port);
                ExecutorService workerThreadPool = newCachedThreadPool("memcached-server-{0}-worker-#%d", port);
                ServerBootstrap memcachedServer
                        = new ServerBootstrap(new NioServerSocketChannelFactory(bossThreadPool, workerThreadPool));
                memcachedServer.setOptions(getNettyOptions());
                NettyMemcachedContainer container = ContainerBuilder.newBuilder()
                        .resources(resources)
                        .build(NettyMemcachedContainer.class);
                memcachedServer.setPipelineFactory(new MemcachedServerPipelineFactory(container));
                memcachedServer.bind(new InetSocketAddress(port));
                track(memcachedServer);
            }
        } catch (Exception e) {
            // If we cannot start all the services, we need stop the services already started first.
            stop();
            LOG.error("Cannot start services successfully.", e);
        }
    }
}
