package me.ellios.hedwig.rpc.thrift.server;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.timeout.IdleState;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;


/**
 * Close Idle connections.
 */
public class IdleTimeoutHandler extends IdleStateAwareChannelHandler {
    private static final Logger LOG = LoggerFactory.getLogger(IdleTimeoutHandler.class);

    public IdleTimeoutHandler() {
    }

    @Override
    public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) throws Exception {
        String remoteAddress = ((InetSocketAddress) e.getChannel().getRemoteAddress()).getAddress().getHostAddress();
        if (e.getState() == IdleState.ALL_IDLE) {
            LOG.warn("channel {} over idle Timeout, close it", remoteAddress);
            e.getChannel().close();
        }
    }
}
