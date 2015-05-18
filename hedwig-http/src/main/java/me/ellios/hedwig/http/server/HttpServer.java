package me.ellios.hedwig.http.server;

import com.google.common.base.Preconditions;
import me.ellios.hedwig.http.container.ContainerBuilder;
import me.ellios.hedwig.http.container.NettyHttpContainer;
import me.ellios.hedwig.rpc.server.support.AbstractRpcServer;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;

/**
 * The default HTTP server implementation.
 * In order to reuse the Thrift/Protobuf service interface, we use Jersey(http://jersey.java.net)
 * as RESTful service engine which provide Annotation based service development just suited for our expectation.
 * This can take great advantages of the service implementations that we already have for thrift or protobuf RPC.
 * <p/>
 * We do not support HTTP cookies, if the request carried cookies with them, the service just ignore them silently.
 * So you'd better not send requests with cookies. They are useless here.
 *
 * @author George Cao
 * @since 4/15/13 10:50 AM
 */
public class HttpServer extends AbstractRpcServer {
    private static final Logger LOG = LoggerFactory.getLogger(HttpServer.class);
    public static final String PROPERTY_BASE_URI = "com.sun.jersey.server.impl.container.netty.baseUri";
    public static final String CORE_PKG = "me.ellios.hedwig.http";


    @Override
    public void start() {
        Preconditions.checkArgument(isServiceRegistered(), "You should at least register one service.");
        try {
            for (Integer port : getPortGroups()) {
                Class<?>[] resources = getServiceResources(port);
                ExecutorService bossThreadPool = newCachedThreadPool("http-server-{0}-boss-#%d", port);
                ExecutorService workerThreadPool = newCachedThreadPool("http-server-{0}-worker-#%d", port);
                NettyHttpContainer container = ContainerBuilder.newBuilder()
                        .packages(CORE_PKG)
                        .resources(resources)
                        .build(NettyHttpContainer.class);
                ServerBootstrap server
                        = new ServerBootstrap(new NioServerSocketChannelFactory(bossThreadPool, workerThreadPool));
                server.setOptions(getNettyOptions());
                server.setPipelineFactory(new HttpServerPipelineFactory(container));
                server.bind(new InetSocketAddress(port));
                LOG.info("Start server on port {} with resources {}", port, resources);
                track(server);
            }
        } catch (Exception e) {
            // If we cannot start all the services, we need stop the services already started first.
            stop();
            LOG.error("Cannot start services successfully.", e);
        }
    }
}
