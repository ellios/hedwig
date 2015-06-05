package me.ellios.hedwig.memcached.protocol;

import org.jboss.netty.buffer.ChannelBuffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Say something?
 *
 * @author George Cao
 * @since 2014-03-26 13:50
 */
public interface MemcachedResponse {
    void writeTo(ChannelBuffer buffer);

    void writeTo(OutputStream output) throws IOException;

    ChannelBuffer buffer();
}
