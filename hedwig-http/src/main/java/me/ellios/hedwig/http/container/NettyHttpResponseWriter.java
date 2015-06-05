package me.ellios.hedwig.http.container;

import org.glassfish.jersey.server.ContainerException;
import org.glassfish.jersey.server.ContainerResponse;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Netty HTTP Response Writer which bridges Jersey Response and Netty HTTP response.
 *
 * @author George Cao
 * @since 2014-03-25 17:58
 */
public class NettyHttpResponseWriter extends JerseyResponseWriter<HttpResponse> {
    private static final Logger LOG = LoggerFactory.getLogger(NettyHttpResponseWriter.class);
    private static final String CONTENT_TYPE = "application/json; charset=UTF-8";

    public NettyHttpResponseWriter(HttpResponse response) {
        super(response);
    }


    @Override
    public OutputStream writeResponseStatusAndHeaders(long contentLength, ContainerResponse context) throws ContainerException {
        // Copy the HTTP status.
        response.setStatus(HttpResponseStatus.valueOf(context.getStatus()));
        // Create a buffer to receive the result.
        // If we know the specific content length, we create a buffer with the exact size,
        // else, we use an empty buffer.
        ChannelBuffer content;
        if (contentLength > 0) {
            content = ChannelBuffers.buffer((int) contentLength);
        } else {
            content = ChannelBuffers.EMPTY_BUFFER;
        }
        response.setContent(content);
        // Set the content length HTTP header.
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, contentLength);

        // Copy HTTP headers
        for (final Map.Entry<String, List<String>> e : context.getStringHeaders().entrySet()) {
            for (final String value : e.getValue()) {
                response.headers().add(e.getKey(), value);
            }
        }
        // The response will be written to this output stream, which also means to channel buffer.
        return new ChannelBufferOutputStream(content);
    }

    @Override
    public void commit() {
        // In order to serialize thrift entity to simple json, we use application/x-thrift;protocol=simple.
        // Now, we need to override the original content type to application/json.
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, CONTENT_TYPE);
    }

    @Override
    public void failure(Throwable error) {
        LOG.error("Cannot get content for {}", response, error);
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, -1);
        response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
        response.headers().remove(HttpHeaders.Names.TRANSFER_ENCODING);
        response.setContent(ChannelBuffers.EMPTY_BUFFER);

    }
}
