package me.ellios.hedwig.rpc.thrift.server;

import me.ellios.hedwig.common.config.HedwigConfig;
import me.ellios.hedwig.common.exceptions.HedwigException;
import me.ellios.hedwig.registry.RegistryHelper;
import me.ellios.hedwig.rpc.core.ServiceConfig;
import me.ellios.hedwig.rpc.core.ServiceNode;
import me.ellios.hedwig.rpc.thrift.ThriftServiceDef;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 * A rpc channel the decode framed Thrift message, dispatches to the TProcessor given
 * and then encode message back to Thrift frame.
 */
public class ThriftServerTransport {
    private static final Logger logger = LoggerFactory.getLogger(ThriftServerTransport.class);
    private static final HedwigConfig hc = HedwigConfig.getInstance();
    private static final HashedWheelTimer timer = new HashedWheelTimer();
    private final int port;
    private final ChannelPipelineFactory pipelineFactory;
    private ServerBootstrap bootstrap;
    private Channel serverChannel;
    private final ThriftServiceDef thriftServiceDef;
    private final ServiceConfig serviceConfig;
    private ServiceNode serviceNode;

    /**
     * Returns the service config used by this transport.
     *
     * @return {@link ServiceConfig} info
     */
    public ServiceConfig serviceConfig() {
        return serviceConfig;
    }

    private void addOptionalHandlers(final ChannelPipeline cp, final ChannelGroup allChannels) {
        // Idle Checks.
        if (HedwigConfig.getInstance().getBoolean("hedwig.rpc.idle.check", true)) {
            cp.addLast("idleState", new IdleStateHandler(timer, 0, 0, hc.getInt("hedwig.rpc.maxIdle", 60000)));
            cp.addLast("idleTimeout", new IdleTimeoutHandler());
        }
        // Bytes Stats.
        if (HedwigConfig.getInstance().getBoolean("hedwig.rpc.stat.enable", false)) {
            cp.addLast(ChannelStatistics.NAME, new ChannelStatistics(thriftServiceDef, allChannels));
        }
    }

    public ThriftServerTransport(final ServiceConfig serviceConfig, final ChannelGroup allChannels) {
        try {
            this.thriftServiceDef = ThriftServiceDef.create(serviceConfig);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new HedwigException(e);
        }
        this.serviceConfig = serviceConfig;
        this.port = thriftServiceDef.getServerPort();
        if (thriftServiceDef.isHeaderTransport()) {
            throw new UnsupportedOperationException("ASF version does not support THeaderTransport !");
        } else {
            this.pipelineFactory = new ChannelPipelineFactory() {
                @Override
                public ChannelPipeline getPipeline() throws Exception {
                    ChannelPipeline cp = Channels.pipeline();
                    // Get runtime connection numbers.
                    cp.addLast("connectionLimiter", new ConnectionLimitFilterHandler(thriftServiceDef));
                    // Add optional handlers first.
                    addOptionalHandlers(cp, allChannels);
                    // Here comes the business logic.
                    cp.addLast("frameDecoder",
                            // TODO Maybe we can calculate a reasonable max frame size later.
                            new LengthFieldBasedFrameDecoder(thriftServiceDef.getMaxFrameSize(), 0, 4, 0, 4));
                    cp.addLast("thriftDecoder", new ThriftDecoder());
                    cp.addLast("frameEncoder", new LengthFieldPrepender(4));
                    cp.addLast("dispatcher", new TRpcDispatcher(thriftServiceDef));
                    return cp;
                }
            };
        }
    }

    public void start(ServerBootstrap serverBootstrap) {
        bootstrap = serverBootstrap;
        bootstrap.setPipelineFactory(pipelineFactory);
        if (logger.isInfoEnabled()) {
            logger.info("starting transport {}:{}", serviceConfig.getName(), port);
        }
        ServiceNode serviceNode = ServiceNode.createServiceNode(serviceConfig);
        this.serviceNode = serviceNode;
        serverChannel = bootstrap.bind(serviceNode.getSocketAddress());
        RegistryHelper.register(serviceNode);
    }

    public void stop() throws InterruptedException {
        if (serviceNode != null) {
            RegistryHelper.unregister(serviceNode);
        }
        if (serverChannel != null) {
            logger.info("stopping transport {}:{}", serviceConfig.getName(), port);
            // first stop accepting
            final CountDownLatch latch = new CountDownLatch(1);
            serverChannel.close().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future)
                        throws Exception {
                    try {
                        // stop and process remaining in-flight invocations
                        if (thriftServiceDef.getExecutor() instanceof ExecutorService) {
                            ExecutorService exe = (ExecutorService) thriftServiceDef.getExecutor();
                            ThriftRpcServer.shutdownExecutor(exe, "dispatcher");
                        }
                    } finally {
                        latch.countDown();
                    }
                }
            });
            latch.await();
            serverChannel = null;
        }
    }
}
