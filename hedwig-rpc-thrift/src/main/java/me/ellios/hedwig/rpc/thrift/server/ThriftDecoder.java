package me.ellios.hedwig.rpc.thrift.server;

import org.apache.thrift.transport.TTransport;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

public class ThriftDecoder extends OneToOneDecoder {
    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg)
            throws Exception {
        if (!(msg instanceof ChannelBuffer)) {
            return msg;
        } else {
            ChannelBuffer cb = (ChannelBuffer) msg;
            if (cb.readableBytes() > 0) {
                return getTransport(channel, cb);
            }
        }
        return msg;
    }

    protected TTransport getTransport(Channel channel, ChannelBuffer cb) {
        return new TRpcTransport(channel, cb);
    }
}
