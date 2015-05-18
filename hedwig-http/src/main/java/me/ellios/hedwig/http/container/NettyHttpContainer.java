package me.ellios.hedwig.http.container;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/15/13 11:58 AM
 */
public final class NettyHttpContainer {
    private static final Logger LOG = LoggerFactory.getLogger(NettyHttpContainer.class);
    private volatile ApplicationHandler appHandler;

    /**
     * Creates a new Netty container.
     *
     * @param appHandler The Web application the container delegates to for the handling of
     *                   HTTP requests.
     */
    NettyHttpContainer(final ApplicationHandler appHandler) {
        this.appHandler = appHandler;
    }

    public void service(final HttpRequest request, final HttpResponse response) throws URISyntaxException {
        final ApplicationHandler appHandler = this.appHandler;
        final URI baseUri = new URI(getBaseUri(request));
        String originalURI = request.getUri();
        final URI requestUri = baseUri.resolve(originalURI);
        final ContainerRequest containerRequest = new ContainerRequest(baseUri, requestUri,
                request.getMethod().getName(),
                new JerseySecurityContext(),
                new JerseyPropertiesDelegate());
        //
        containerRequest.setEntityStream(new ChannelBufferInputStream(request.getContent()));
        for (String headerName : request.getHeaderNames()) {
            containerRequest.headers(headerName, request.getHeaders(headerName));
        }
        containerRequest.setWriter(new NettyHttpResponseWriter(response));

        appHandler.handle(containerRequest);
    }

    private String getBaseUri(HttpRequest resquest) {
        return "";
    }
}
