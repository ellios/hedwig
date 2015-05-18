package me.ellios.hedwig.http.server;

import me.ellios.hedwig.http.container.NettyHttpContainer;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Say something?
 *
 * @author George Cao
 * @since 4/15/13 11:12 AM
 */
public class HttpServerHandler extends SimpleChannelUpstreamHandler {
    private static final Logger LOG = LoggerFactory.getLogger(HttpServerHandler.class);

    private final NettyHttpContainer container;

    public HttpServerHandler(NettyHttpContainer container) {
        this.container = container;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        HttpRequest request = (HttpRequest) e.getMessage();
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        container.service(request, response);
        ChannelFuture future = e.getChannel().write(response);
        future.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        LOG.error("On channel remote:{}, local:{}", ctx.getChannel().getRemoteAddress(),
                ctx.getChannel().getLocalAddress(),
                e.getCause());
        Channels.close(ctx.getChannel());
    }
}
