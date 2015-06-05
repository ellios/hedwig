package me.ellios.hedwig.memcached.protocol.text;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import java.io.Serializable;

import static me.ellios.hedwig.memcached.protocol.text.Protocol.*;

/**
 * The Get Command response.
 * See https://github.com/memcached/memcached/blob/master/doc/protocol.txt for detail.
 *
 * @author George Cao
 * @since 2014-03-26 12:28
 */
public class GetResponse extends GenericResponse implements Serializable {
    byte[] key;
    byte[] flags;
    byte[] bytes;
    byte[] casUnique;
    ChannelBuffer data;

    public static final GetResponse EMPTY_RESPONSE = new GetResponse();

    public GetResponse() {
        this(EMPTY_ARRAY, ChannelBuffers.EMPTY_BUFFER);
    }

    public GetResponse(String key, ChannelBuffer data) {
        this(key.getBytes(UTF_8), data);
    }

    public GetResponse(byte[] key, ChannelBuffer data) {
        this(key, FLAGS, data.readableBytes(), 0, data);
    }

    public GetResponse(byte[] key, int flags, int bytes, long casUnique, ChannelBuffer data) {
        this.key = key;
        this.flags = asString(flags);
        this.bytes = asString(bytes);
        // We do NOT support CAS.
        this.casUnique = asString(casUnique);
        this.data = data;
    }

    public int size() {
        int size = 18;
        size += len(key);
        size += len(flags);
        size += len(bytes);
        size += len(casUnique);
        size += len(data);
        return size;
    }

    public boolean isEmpty(byte[] bytes) {
        return 0 == len(bytes);
    }

    public boolean isNotEmpty(byte[] bytes) {
        return !isEmpty(bytes);
    }

    public boolean isNotEmpty(ChannelBuffer data) {
        return 0 != len(data);
    }

    private int len(ChannelBuffer data) {
        return null == data ? 0 : data.readableBytes();
    }

    private int len(byte[] bytes) {
        return null == bytes ? 0 : bytes.length;
    }


    private byte[] toUtf8Bytes(String value) {
        return value.getBytes(UTF_8);
    }

    private byte[] asString(int value) {
        if (0 > value) {
            return EMPTY_ARRAY;
        }
        return toUtf8Bytes(String.valueOf(value));
    }

    private byte[] asString(long value) {
        if (0 >= value) {
            return EMPTY_ARRAY;
        }
        return toUtf8Bytes(String.valueOf(value));
    }

    @Override
    public void writeTo(ChannelBuffer buffer) {
        if (isNotEmpty(data)) {
            buffer.writeBytes(VALUE);
            buffer.writeByte(SPACE);
            buffer.writeBytes(key);
            buffer.writeByte(SPACE);
            buffer.writeBytes(flags);
            buffer.writeByte(SPACE);
            buffer.writeBytes(bytes);
            buffer.writeByte(SPACE);
            if (isNotEmpty(casUnique)) {
                buffer.writeBytes(casUnique);
            }
            buffer.writeBytes(NEW_LINE);
            buffer.writeBytes(data);
            buffer.writeBytes(NEW_LINE);
        }
        buffer.writeBytes(END);
        buffer.writeBytes(NEW_LINE);
    }

    @Override
    public ChannelBuffer buffer() {
        ChannelBuffer buffer = createBuffer();
        writeTo(buffer);
        return buffer;
    }

    private ChannelBuffer createBuffer() {
        return ChannelBuffers.buffer(size());
    }
}
