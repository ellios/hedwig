package me.ellios.hedwig.rpc.server.support;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import me.ellios.hedwig.common.config.HedwigConfig;
import me.ellios.hedwig.registry.RegistryHelper;
import me.ellios.hedwig.rpc.core.ServiceConfig;
import me.ellios.hedwig.rpc.core.ServiceNode;
import me.ellios.hedwig.rpc.server.RpcServer;
import org.jboss.netty.util.ExternalResourceReleasable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Author: ellios
 * Date: 12-11-1 Time: 下午3:05
 */
abstract public class AbstractRpcServer implements RpcServer {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractRpcServer.class);
    protected static HedwigConfig hc = HedwigConfig.getInstance();

    protected static final int BACKLOG = hc.getInt("hedwig.server.backlog", 1024000);
    protected static final boolean TCP_NODELAY = hc.getBoolean("hedwig.server.tcp.nodeley", true);
    protected static final boolean REUSE_ADDRESS = hc.getBoolean("hedwig.server.reuse.address", true);
    protected static final boolean KEEPALIVE = hc.getBoolean("hedwig.server.keep.alive", true);

    protected static final int SEND_BUFFER_SIZE = hc.getInt("hedwig.server.send.buffer.size", 8192);
    protected static final int RECEIVE_BUFFER_SIZE = hc.getInt("hedwig.server.receive.buffer.size", 8192);

    public static final String PROPERTY_MAX_SERVICE_THRESHOLD = "hedwig.server.max.service.threshold";

    protected static final int MAX_SERVER_THRESHOLD = hc.getInt(PROPERTY_MAX_SERVICE_THRESHOLD, 3);
    private Multimap<Integer, ServiceConfig> portToConfigMap = LinkedListMultimap.create();
    private List<ExternalResourceReleasable> trackedResources = new LinkedList<>();

    public void track(ExternalResourceReleasable resource) {
        trackedResources.add(resource);
    }

    public void destroy() {
        for (ExternalResourceReleasable resource : trackedResources) {
            resource.releaseExternalResources();
        }
        trackedResources.clear();
    }

    public void groupServiceConfigByPort(ServiceConfig config) {
        Preconditions.checkNotNull(config, "You just cannot register a NULL service.");
        if (portToConfigMap.size() >= MAX_SERVER_THRESHOLD) {
            throw new IllegalStateException("Exceeds the max service threshold " + MAX_SERVER_THRESHOLD
                    + " per server. Please use " + PROPERTY_MAX_SERVICE_THRESHOLD + " to overwrite this config.");
        }
        portToConfigMap.put(config.getPort(), config);
    }

    public Set<Integer> getPortGroups() {
        return portToConfigMap.keySet();
    }

    public Collection<ServiceConfig> getServiceConfigs(int port) {
        return portToConfigMap.get(port);
    }

    public Class<?>[] getServiceResources(int port) {
        Collection<ServiceConfig> collection = getServiceConfigs(port);
        Class<?>[] resources = new Class<?>[0];
        if (null != collection && collection.size() > 0) {
            resources = new Class<?>[collection.size()];
            int idx = 0;
            for (ServiceConfig config : collection) {
                resources[idx++] = config.getServiceImpl();
            }
        }
        return resources;
    }

    public boolean isServiceRegistered() {
        return portToConfigMap.size() > 0;
    }

    public Map<String, Object> getNettyOptions() {
        Map<String, Object> map = new HashMap<>();
        map.put("backlog", BACKLOG);
        map.put("reuseAddress", REUSE_ADDRESS);
        map.put("child.reuseAddress", REUSE_ADDRESS);
        map.put("child.tcpNoDelay", TCP_NODELAY);
        map.put("child.keepAlive", KEEPALIVE);
        if (SEND_BUFFER_SIZE > 0) {
            map.put("child.sendBufferSize", SEND_BUFFER_SIZE);
            map.put("sendBufferSize", SEND_BUFFER_SIZE);
        }
        if (RECEIVE_BUFFER_SIZE > 0) {
            map.put("child.receiveBufferSize", RECEIVE_BUFFER_SIZE);
            map.put("receiveBufferSize", RECEIVE_BUFFER_SIZE);
        }
        return map;
    }

    /**
     * Try to terminate the specified thread pool.
     * Wait for 5 seconds for the termination.
     *
     * @param executor the thread pool.
     * @param name     an identifier, used in output log.
     */
    public static void shutdownExecutor(ExecutorService executor, final String name) {
        if (null != executor) {
            try {
                executor.shutdown();
                LOG.info("waiting for {} to shutdown", name);
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
                LOG.info("Executor {} 's status is ", executor.isTerminated());
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Create a cached thread pool with specified thread name pattern.
     *
     * @param threadNameFormat thread name pattern used in {@link String#format(String, Object...)} )}.
     *                         The second parameter is a integer index of the thread.
     * @return thread pool
     */
    public static ExecutorService newCachedThreadPool(String threadNameFormat) {
        ThreadFactory bossThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat(threadNameFormat)
                .build();
        return Executors.newCachedThreadPool(bossThreadFactory);
    }

    public static ExecutorService newCachedThreadPool(String threadNameFormat, Object... args) {
        return newCachedThreadPool(MessageFormat.format(threadNameFormat, args));
    }

    @Override
    public void stop() {
        destroy();
    }

    /**
     * In order to be compatible with the other servers, we also reuse this interface to register HTTP service.
     * User can invoke this method multiple times to add more resource to the HTTP server. So there are chances that
     * the port number specified by multiple {@link ServiceConfig} are different from each other. We solve problem by
     * just group the services by port number, and start each server per port.
     * <p/>
     * Also We recommend the number of port number groups should not exceed {@link #MAX_SERVER_THRESHOLD} per server.
     * <p/>
     * Be advised.
     *
     * @param serviceConfig the service config.
     * @see #groupServiceConfigByPort(me.ellios.hedwig.rpc.core.ServiceConfig)
     */
    @Override
    public void registerService(ServiceConfig serviceConfig) {
        groupServiceConfigByPort(serviceConfig);
    }

    /**
     * Let the clients see these services.
     *
     * @param services brand-new services.
     */
    public void publish(Collection<ServiceConfig> services) {
        for (ServiceConfig service : services) {
            RegistryHelper.register(ServiceNode.createServiceNode(service));
        }
    }

    /**
     * Hide the services.
     *
     * @param services the services should be stopped.
     */
    public void hide(Collection<ServiceConfig> services) {
        for (ServiceConfig service : services) {
            RegistryHelper.unregister(ServiceNode.createServiceNode(service));
        }
    }

    /**
     * Hide all services from the clients.
     *
     * @see #hide(java.util.Collection)
     */
    public void hideAll() {
        Set<Integer> ports = getPortGroups();
        for (Integer port : ports) {
            hide(getServiceConfigs(port));
        }
    }
}