package me.ellios.hedwig.memcached.protocol.text;

import me.ellios.hedwig.memcached.container.NettyMemcachedContainer;
import me.ellios.hedwig.memcached.protocol.MemcachedResponse;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default text protocol impl.
 *
 * @author gaofeng@qiyi.com
 * @since 14-3-20
 */
public class DefaultTextProtocol implements Protocol {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultTextProtocol.class);
    private final NettyMemcachedContainer container;

    public DefaultTextProtocol(NettyMemcachedContainer container) {
        this.container = container;
    }

    @Override
    public void handleCommand(ChannelHandlerContext ctx, String command) {
        if (!command.startsWith(Commend.GET.name().toLowerCase())) {
            error(ctx, ErrorType.CLIENT_ERROR, "Not supported commend " + command);
            return;
        }
        try {
            String url = command.substring(Commend.GET.name().length() + 1);
            ChannelBuffer data = container.service(url);
            response(data, url, ctx);
        } catch (Throwable ex) {
            LOG.error("handle get command error, command: " + command, ex);
            error(ctx, ErrorType.SERVER_ERROR, "Cannot handle command " + command);
        }
    }

    public void error(ChannelHandlerContext ctx, ErrorType errorType, String error) {
        error(ctx, new ErrorResponse(errorType, error));
    }

    @Override
    public void error(ChannelHandlerContext ctx, ErrorResponse error) {
        channelWrite(ctx, error);
    }

    private static void response(ChannelBuffer data, String key, ChannelHandlerContext ctx) {
        GetResponse response;
        if (data == null) {
            response = GetResponse.EMPTY_RESPONSE;
        } else {
            response = new GetResponse(key, data);
        }
        channelWrite(ctx, response);
    }

    private static void channelWrite(ChannelHandlerContext ctx, MemcachedResponse response) {
        ctx.getChannel().write(response.buffer());
    }
}
