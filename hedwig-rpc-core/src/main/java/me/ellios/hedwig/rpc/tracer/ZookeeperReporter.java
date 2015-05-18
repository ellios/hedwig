package me.ellios.hedwig.rpc.tracer;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import me.ellios.hedwig.rpc.core.ServiceNode;
import me.ellios.hedwig.zookeeper.ZooType;
import me.ellios.hedwig.zookeeper.ZookeeperClient;
import me.ellios.hedwig.zookeeper.ZookeeperClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Report connection count to Zookeeper.
 *
 * @author George Cao
 * @since 4/1/13 10:09 AM
 */
public class ZookeeperReporter extends AbstractPollingReporter implements MetricProcessor<ZookeeperClient> {
    private static final Logger LOG = LoggerFactory.getLogger(ZookeeperReporter.class);
    private final ServiceNode node;
    // In order to close the reporter later.
    private static ZookeeperReporter reporter;
    private final ZookeeperClient client = ZookeeperClientFactory.getZookeeperClient(ZooType.SERVICE);
    public static final MetricName METRIC_NAME = new MetricName("connections", "global", "total");
    // Last value reported.
    private static volatile long previous;

    /**
     * Creates a new {@link com.yammer.metrics.reporting.AbstractPollingReporter} instance.
     *
     * @param registry   the {@link com.yammer.metrics.core.MetricsRegistry} containing the metrics this reporter will
     *                   report
     * @param serverNode the ServiceNode to locate Zookeeper path.
     * @see com.yammer.metrics.reporting.AbstractReporter#AbstractReporter(com.yammer.metrics.core.MetricsRegistry)
     */
    protected ZookeeperReporter(ServiceNode serverNode, MetricsRegistry registry) {
        super(registry, "zookeeper-reporter-" + serverNode.getName());
        this.node = serverNode;
    }

    /**
     * Enables the zookeeper reporter for the default metrics registry, and causes it to set the connection
     * property with the specified period.
     *
     * @param period the period between successive outputs
     * @param unit   the time unit of {@code period}
     */
    public static void enable(ServiceNode node, long period, TimeUnit unit) {
        enable(node, Metrics.defaultRegistry(), period, unit);
    }

    /**
     * Enables the zookeeper reporter for the given metrics registry, and causes it to set the connection
     * property with the specified period and unrestricted output.
     *
     * @param metricsRegistry the metrics registry
     * @param period          the period between successive outputs
     * @param unit            the time unit of {@code period}
     */
    public static void enable(ServiceNode node, MetricsRegistry metricsRegistry, long period, TimeUnit unit) {
        shutdownDefault();
        reporter = new ZookeeperReporter(node, metricsRegistry);
        reporter.start(period, unit);
    }

    /**
     * Stops the default instance of {@link me.ellios.hedwig.rpc.tracer.ZookeeperReporter}.
     */
    public static void shutdownDefault() {
        if (reporter != null) {
            reporter.shutdown();
        }
    }

    @Override
    public void run() {
        Metric metric = getMetricsRegistry().allMetrics().get(METRIC_NAME);
        if (null != metric) {
            try {
                metric.processWith(this, METRIC_NAME, client);
            } catch (Exception e) {
                LOG.warn("Process metric {}", metric, e);
            }
        }
    }

    @Override
    public void processMeter(MetricName name, Metered meter, ZookeeperClient context) throws Exception {
        LOG.warn("Not implemented yet.");
    }

    @Override
    public void processCounter(MetricName name, Counter counter, ZookeeperClient context) throws Exception {
        // Reduce duplicated value reported.
        if (counter.count() == previous) {
            LOG.info("Counter value of {} is same as the value {} last reported.", METRIC_NAME, previous);
            return;
        }
        String path = node.getZnodePath();
        // Make sure the service is alive.
        if (context.exist(path)) {
            byte[] data = context.getData(path);
            if (null == data || data.length == 0) {
                LOG.error("Service {} is alive but data missed.", path);
                return;
            }
            // TODO we should create ephemeral data nodes instead, but not put all data together.
            ServiceNode d = ServiceNode.fromJson(data);
            LOG.info("Update ServiceNode {} by counter {} with new value {}", d, name, counter.count());
            d.setConnections((int) counter.count());
            // Rewrite the data of the znode.
            context.setData(path, d.toJson());
            previous = counter.count();
        }
    }

    @Override
    public void processHistogram(MetricName name, Histogram histogram, ZookeeperClient context) throws Exception {
        LOG.warn("Not implemented yet.");
    }

    @Override
    public void processTimer(MetricName name, Timer timer, ZookeeperClient context) throws Exception {
        LOG.warn("Not implemented yet.");
    }

    @Override
    public void processGauge(MetricName name, Gauge<?> gauge, ZookeeperClient context) throws Exception {
        LOG.warn("Not implemented yet.");
    }
}
