package me.ellios.hedwig.rpc.thrift.server;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricName;
import me.ellios.hedwig.rpc.thrift.ThriftServiceDef;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.ipfilter.IpFilteringHandlerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Connection limits.
 * Because we cannot refuse to accept a connection in the java stack, So we just accept a connection then close it
 * immediately if the server is overloaded right now.
 * And also when we close the server side channel, the client side will throw I/O exception like
 * {@link java.net.SocketException}: Software caused connection abort
 *
 * @author George Cao
 * @since 13-3-7 下午2:58
 */

@Sharable
public class ConnectionLimitFilterHandler extends IpFilteringHandlerImpl {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionLimitFilterHandler.class);
    private final ConcurrentMap<InetAddress, AtomicInteger> hostConnectionCount =
            new ConcurrentHashMap<>();

    private final AtomicInteger totalConnections = new AtomicInteger(0);

    private final ThriftServiceDef def;
    private final boolean enable;
    private static final String GROUP_KEY = "connections";
    private static final String HOST_KEY = "host";
    private static final String TOTAL_KEY = "total";
    private static final String GLOBAL_KEY = "global";
    private static final MetricName METRIC_NAME = new MetricName(GROUP_KEY, GLOBAL_KEY, TOTAL_KEY);

    public ConnectionLimitFilterHandler(ThriftServiceDef def) {
        this.def = def;
        this.enable = def.isEnableConnectionCheck();
    }

    private void decrement(InetAddress address) {
        add(address, -1);
    }

    private void increment(InetAddress address) {
        add(address, 1);
    }

    private void add(InetAddress address, int delta) {
        // Record the total connections.
        totalConnections.addAndGet(delta);
        def.getTracer().addCount("connections-host-" + address.getHostAddress(), delta);
        Metrics.newCounter(new MetricName(GROUP_KEY, HOST_KEY, address.getHostAddress())).inc(delta);
        // Record total connections per client.
        hostConnectionCount.get(address).addAndGet(delta);
        def.getTracer().addCount("connections-global-total", delta);
        Metrics.newCounter(METRIC_NAME).inc(delta);
    }

    @Override
    protected boolean accept(ChannelHandlerContext ctx, ChannelEvent e, InetSocketAddress socketAddress)
            throws Exception {
        // Check the per-client limits first.
        final InetAddress address = socketAddress.getAddress();
        if (!hostConnectionCount.containsKey(address)) {
            hostConnectionCount.put(address, new AtomicInteger(0));
        }
        int hostCount = hostConnectionCount.get(address).get();
        if (enable && def.getHostConnectionsThresholds() <= hostCount) {
            LOG.warn("Host {} already created {} connections(max allowed {}). We will refuse this one from {}.",
                    address, hostCount, def.getHostConnectionsThresholds(), socketAddress);
            return false;
        }
        // Then check the global limits.
        int totalCount = totalConnections.get();
        if (enable && def.getOpenConnectionsThresholds() <= totalCount) {
            LOG.warn("Server has accepted {} connections(max allowed {}). We will refuse this one from {}.",
                    totalCount, def.getOpenConnectionsThresholds(), socketAddress);
            return false;
        }
        // Increment the connection counters.
        increment(address);
        // Register a listener to decrement these counters when it's closed.
        e.getChannel().getCloseFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                decrement(address);
            }
        });
        return true;
    }
}
