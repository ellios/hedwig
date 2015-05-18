package me.ellios.hedwig.rpc.thrift.server;

import me.ellios.hedwig.rpc.thrift.ThriftServiceDef;
import me.ellios.hedwig.rpc.tracer.TracerDriver;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Counters for number of channels open, generic traffic stats and maybe cleanup logic here.
 */
public class ChannelStatistics extends SimpleChannelHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ChannelStatistics.class);
    // TODO : expose these stats somewhere
    private static final AtomicInteger channelCount = new AtomicInteger(0);
    private final AtomicLong bytesRead = new AtomicLong(0);
    private final AtomicLong bytesWritten = new AtomicLong(0);
    private final ChannelGroup allChannels;
    private TracerDriver tracer;
    private ThriftServiceDef def;
    public static final String NAME = ChannelStatistics.class.getSimpleName();

    public ChannelStatistics(ThriftServiceDef def, ChannelGroup allChannels) {
        this(def.getTracer(), allChannels);
        this.def = def;
    }

    public ChannelStatistics(TracerDriver tracer, ChannelGroup allChannels) {
        this.tracer = tracer;
        this.allChannels = allChannels;
    }

    public ChannelStatistics(ChannelGroup allChannels) {
        this(TracerDriver.DISCARD_TRACER, allChannels);
    }

    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
            throws Exception {
        if (e instanceof ChannelStateEvent) {
            ChannelStateEvent cse = (ChannelStateEvent) e;
            switch (cse.getState()) {
                case OPEN:
                    if (Boolean.TRUE.equals(cse.getValue())) {
                        // connect
                        channelCount.incrementAndGet();
                        allChannels.add(e.getChannel());
                        tracer.addCount("connections-counter", 1);
                    } else {
                        // disconnect
                        channelCount.decrementAndGet();
                        allChannels.remove(e.getChannel());
                        tracer.addCount("connections-counter", -1);
                    }
                    break;
                case BOUND:
                    break;
            }
        }

        if (e instanceof UpstreamMessageEvent) {
            UpstreamMessageEvent ume = (UpstreamMessageEvent) e;
            if (ume.getMessage() instanceof ChannelBuffer) {
                ChannelBuffer cb = (ChannelBuffer) ume.getMessage();
                int readableBytes = cb.readableBytes();
                //  compute stats here, bytes read from remote
                bytesRead.getAndAdd(readableBytes);
                tracer.addCount("bytes-received-counter", readableBytes);
            }
        }
        ctx.sendUpstream(e);
    }

    public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e)
            throws Exception {
        if (e instanceof DownstreamMessageEvent) {
            DownstreamMessageEvent dme = (DownstreamMessageEvent) e;
            if (dme.getMessage() instanceof ChannelBuffer) {
                ChannelBuffer cb = (ChannelBuffer) dme.getMessage();
                int readableBytes = cb.readableBytes();
                // compute stats here, bytes written to remote
                bytesWritten.getAndAdd(readableBytes);
                tracer.addCount("bytes-sent-counter", readableBytes);
            }
        }
        ctx.sendDownstream(e);
    }

    public static int getChannelCount() {
        return channelCount.get();
    }

    public long getBytesRead() {
        return bytesRead.get();
    }

    public long getBytesWritten() {
        return bytesWritten.get();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
    }
}



