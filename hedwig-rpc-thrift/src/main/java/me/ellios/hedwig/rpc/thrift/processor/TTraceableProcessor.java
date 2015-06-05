package me.ellios.hedwig.rpc.thrift.processor;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import me.ellios.hedwig.rpc.thrift.protocol.THeaderName;
import me.ellios.hedwig.rpc.thrift.protocol.TMessageHeader;
import me.ellios.hedwig.rpc.thrift.protocol.TMessageProtocol;
import me.ellios.hedwig.rpc.thrift.protocol.TMessageWrapper;
import me.ellios.hedwig.rpc.tracer.TracerDriver;
import me.ellios.hedwig.rpc.tracer.TracerInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * A {@link org.apache.thrift.TProcessor} implementation let us know the message detail.
 *
 * @author George Cao
 * @since 13-10-11 下午3:46
 */
public class TTraceableProcessor implements TProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(TTraceableProcessor.class);
    private TProcessor concreteProcessor;
    private TracerDriver driver;
    private ChannelHandlerContext context;
    private final Map<String, Set<String>> mapping;

    public TTraceableProcessor(ChannelHandlerContext context, TProcessor concreteProcessor, TracerDriver driver, Map<String, Set<String>> mapping) {
        this.concreteProcessor = concreteProcessor;
        this.driver = driver;
        this.context = context;
        this.mapping = mapping;
    }

    @Override
    public boolean process(TProtocol in, TProtocol out) throws TException {
        Stopwatch watch = Stopwatch.createStarted();
        TMessageWrapper wrapper = multiplex(in.readMessageBegin());
        TMessage message = wrapper.getMessage();
        boolean result = concreteProcessor.process(new TMessageProtocol(in, message), out);
        watch.stop();
        driver.addTrace(collect(wrapper, context, watch).toString(),
                watch.elapsed(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
        return result;
    }

    /**
     * If the {@link org.apache.thrift.protocol.TMessage#name} does NOT contain a multiplexed identifier, we add it here.
     */
    private TMessageWrapper multiplex(TMessage message) {
        TMessageWrapper wrapper = new TMessageWrapper();
        TMessageHeader header = TMessageHeader.decode(message.name);
        String service = header.getValue(THeaderName.SERVICE);
        String api = header.getValue(THeaderName.API);
        if (Strings.isNullOrEmpty(service)) {
            header.add(THeaderName.SERVICE, findService(api));
        }
        // Set the multiplex message. Strip additional headers.
        wrapper.setMessage(new TMessage(header.signature(), message.type, message.seqid));
        wrapper.setHeader(header);
        return wrapper;
    }

    private String findService(String api) {
        for (Map.Entry<String, Set<String>> entry : mapping.entrySet()) {
            if (entry.getValue().contains(api)) {
                return entry.getKey();
            }
        }
        LOG.error("Cannot find service of api {}, this should not happen anyway.", api);
        return StringUtils.EMPTY;
    }

    private TracerInfo collect(TMessageWrapper wrapper, ChannelHandlerContext context, Stopwatch watch) {
        TMessage message = wrapper.getMessage();
        TMessageHeader header = wrapper.getHeader();
        TracerInfo info = new TracerInfo();
        info.setService(header.getValue(THeaderName.SERVICE));
        info.setApi(header.getValue(THeaderName.API));
        info.setTime(watch.elapsed(TimeUnit.NANOSECONDS));
        info.setUnit(TimeUnit.NANOSECONDS);
        info.setType(message.type);
        info.setSeqId(message.seqid);
        info.setRemoteAddress(context.getChannel().getRemoteAddress());
        info.setLocalAddress(context.getChannel().getLocalAddress());
        info.setUser(header.getValue(THeaderName.USER));
        return info;
    }
}

