package me.ellios.hedwig.memcached.container;

import me.ellios.hedwig.http.container.JerseyPropertiesDelegate;
import me.ellios.hedwig.http.container.JerseySecurityContext;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Say something?
 *
 * @author George Cao(caozhangzhi@qiyi.com)
 * @since 2014-03-26 16:04
 */
public class NettyMemcachedContainer {
    private static final Logger LOG = LoggerFactory.getLogger(NettyMemcachedContainer.class);

    private volatile ApplicationHandler appHandler;

    public NettyMemcachedContainer(ApplicationHandler appHandler) {
        this.appHandler = appHandler;
    }

    public ChannelBuffer service(String url) {
        ApplicationHandler appHandler = this.appHandler;
        try {
            URI baseUri = new URI("http://127.0.0.1/");
            URI requestUri = baseUri.resolve(url);
            ContainerRequest request = new ContainerRequest(baseUri, requestUri,
                    HttpMethod.GET.getName(),
                    new JerseySecurityContext(), new JerseyPropertiesDelegate());
            final ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
            Future<ContainerResponse> future = appHandler.apply(request, new ChannelBufferOutputStream(buffer));
            await(future);
            return buffer;
        } catch (URISyntaxException e) {
            LOG.warn("Invalid url {}", url, e);
        }
        return ChannelBuffers.EMPTY_BUFFER;
    }

    private <V> V await(Future<V> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException("Cannot get value from future " + future, e);
        }
    }
}
