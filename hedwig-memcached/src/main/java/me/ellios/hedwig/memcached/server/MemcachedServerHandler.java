package me.ellios.hedwig.memcached.server;

import me.ellios.hedwig.memcached.container.NettyMemcachedContainer;
import me.ellios.hedwig.memcached.protocol.text.DefaultTextProtocol;
import me.ellios.hedwig.memcached.protocol.text.Protocol;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MemcachedServerHandler
 *
 * @author gaofeng@qiyi.com
 * @since: 14-3-14
 */
public class MemcachedServerHandler extends SimpleChannelUpstreamHandler {
    private static final Logger LOG = LoggerFactory.getLogger(MemcachedServerHandler.class);
    private final Protocol protocol;

    public MemcachedServerHandler(NettyMemcachedContainer container) {
        protocol = new DefaultTextProtocol(container);
    }

    /**
     * Invoked when a message object (e.g: {@link org.jboss.netty.buffer.ChannelBuffer}) was received
     * from a remote peer.
     */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        ChannelBuffer channelBuffer = (ChannelBuffer) e.getMessage();
        String command = channelBuffer.toString(CharsetUtil.UTF_8);
        protocol.handleCommand(ctx, command);
    }

    /**
     * Invoked when an exception was raised by an I/O thread or a
     * {@link org.jboss.netty.channel.ChannelHandler}.
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        LOG.error("memcachedHandler is error.", e);
    }
}
