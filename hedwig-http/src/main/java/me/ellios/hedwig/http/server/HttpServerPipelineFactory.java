package me.ellios.hedwig.http.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import me.ellios.hedwig.http.container.NettyHttpContainer;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.MemoryAwareThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/15/13 11:40 AM
 */
public class HttpServerPipelineFactory implements ChannelPipelineFactory {
    private static final Logger LOG = LoggerFactory.getLogger(HttpServerPipelineFactory.class);
    private ThreadPoolExecutor eventExecutor;
    private NettyHttpContainer container;

    public HttpServerPipelineFactory(ThreadPoolExecutor eventExecutor, NettyHttpContainer container) {
        this.eventExecutor = eventExecutor;
        this.container = container;
    }

    public HttpServerPipelineFactory(NettyHttpContainer container) {
        ThreadFactory factory = new ThreadFactoryBuilder()
                .setNameFormat("http-server-exec-handler-#%d")
                .build();
        this.eventExecutor = new MemoryAwareThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors(),
                1000000,
                10000000, 30, TimeUnit.SECONDS, factory);
        this.container = container;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("decoder", new HttpRequestDecoder());
        // HTTP chunk
        //pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
        pipeline.addLast("encoder", new HttpResponseEncoder());
        //pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());

        // Insert OrderedMemoryAwareThreadPoolExecutor before your blocking handler
        pipeline.addLast("pipelineExecutor", new ExecutionHandler(eventExecutor));
        // MyHandler contains code that blocks
        pipeline.addLast("handler", new HttpServerHandler(container));

        return pipeline;
    }
}
