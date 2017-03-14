package me.ellios.hedwig.rpc.thrift.server;

import me.ellios.hedwig.common.config.HedwigConfig;
import me.ellios.hedwig.rpc.thrift.ThriftServiceDef;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A thrift server channel pipeline factory.
 *
 * @author George Cao
 * @since 13-10-11 下午3:46
 */
public class ThriftServerPipelineFactory implements ChannelPipelineFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ThriftServerPipelineFactory.class);
    private static final HashedWheelTimer timer = new HashedWheelTimer();
    private static final HedwigConfig hc = HedwigConfig.getInstance();

    private final ThriftServiceDef thriftServiceDef;
    private final ChannelGroup allChannels;

    public ThriftServerPipelineFactory(ThriftServiceDef thriftServiceDef, ChannelGroup allChannels) {
        this.thriftServiceDef = thriftServiceDef;
        this.allChannels = allChannels;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        LOG.info("get Pipeline channels : {}", allChannels);
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
}
