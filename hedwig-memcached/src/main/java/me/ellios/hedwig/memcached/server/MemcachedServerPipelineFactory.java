package me.ellios.hedwig.memcached.server;

import me.ellios.hedwig.memcached.container.NettyMemcachedContainer;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.LineBasedFrameDecoder;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;

/**
 * MemcachedServerPipelineFactory
 *
 * @author gaofeng
 * @since: 14-3-14
 */
public class MemcachedServerPipelineFactory implements ChannelPipelineFactory {
    private static final IdleStateHandler idleChannelTester = new IdleStateHandler(new HashedWheelTimer(), 0, 0, 5);
    private static final IdleTimeoutHandler IdleTimeoutHandler = new IdleTimeoutHandler();

    private final MemcachedServerHandler memcachedServerHandler;

    public MemcachedServerPipelineFactory(final NettyMemcachedContainer container) {
        memcachedServerHandler = new MemcachedServerHandler(container);
    }

    /**
     * Returns a newly created {@link org.jboss.netty.channel.ChannelPipeline}.
     */
    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("idleChannelTester", idleChannelTester);
        pipeline.addLast("IdleTimeoutHandler", IdleTimeoutHandler);
        pipeline.addLast("decoder", new LineBasedFrameDecoder(4096));
        pipeline.addLast("mcHandler", memcachedServerHandler);
        return pipeline;
    }
}
