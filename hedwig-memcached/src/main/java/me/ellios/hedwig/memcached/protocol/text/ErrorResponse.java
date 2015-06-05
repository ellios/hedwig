package me.ellios.hedwig.memcached.protocol.text;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static me.ellios.hedwig.memcached.protocol.text.Protocol.*;

/**
 * Common error response.
 *
 * @author George Cao
 * @since 2014-03-26 14:12
 */
public class ErrorResponse extends GenericResponse {
    private static final Logger LOG = LoggerFactory.getLogger(ErrorResponse.class);

    ErrorType type;
    String error;

    public ErrorResponse(ErrorType type) {
        this(type, null);
    }

    public ErrorResponse(ErrorType type, String error) {
        this.type = type;
        this.error = error;
    }

    @Override
    public void writeTo(ChannelBuffer buffer) {
        buffer.writeBytes(buffer());
    }

    @Override
    public ChannelBuffer buffer() {
        switch (type) {
            case CLIENT_ERROR:
                byte[] clientBytes = error.getBytes(UTF_8);
                ChannelBuffer client = ChannelBuffers.buffer(CLIENT_ERROR.length + 3 + clientBytes.length);
                client.writeBytes(CLIENT_ERROR);
                client.writeByte(SPACE);
                client.writeBytes(clientBytes);
                client.writeBytes(NEW_LINE);
                return client;
            case SERVER_ERROR:
                byte[] serverBytes = error.getBytes(UTF_8);
                ChannelBuffer server = ChannelBuffers.buffer(SERVER_ERROR.length + 3 + serverBytes.length);
                server.writeBytes(SERVER_ERROR);
                server.writeByte(SPACE);
                server.writeBytes(serverBytes);
                server.writeBytes(NEW_LINE);
            case ERROR:
            default:
                ChannelBuffer error = ChannelBuffers.buffer(ERROR.length + 2);
                error.writeBytes(ERROR);
                error.writeBytes(NEW_LINE);
                return error;
        }
    }
}
