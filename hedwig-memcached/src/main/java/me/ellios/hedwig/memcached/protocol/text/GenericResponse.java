package me.ellios.hedwig.memcached.protocol.text;

import me.ellios.hedwig.memcached.protocol.MemcachedResponse;
import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Say something?
 *
 * @author George Cao
 * @since 2014-03-26 21:30
 */
public abstract class GenericResponse implements MemcachedResponse {
    private static final Logger LOG = LoggerFactory.getLogger(GenericResponse.class);

    @Override
    public void writeTo(OutputStream output) throws IOException {
        ChannelBuffer buffer = buffer();
        output.write(buffer.array(), buffer.readerIndex(), buffer.readableBytes());
    }
}
