package me.ellios.hedwig.rpc.proxy.support;

import me.ellios.hedwig.common.concurrent.FutureRunner;
import me.ellios.hedwig.common.concurrent.ResourceCreator;
import me.ellios.hedwig.common.exceptions.HedwigException;
import me.ellios.hedwig.common.exceptions.HedwigTransportException;
import me.ellios.hedwig.rpc.core.ServiceNode;
import me.ellios.hedwig.registry.CallbackWatcher;
import me.ellios.hedwig.registry.Registry;
import me.ellios.hedwig.registry.zookeeper.ZookeeperRegistryFactory;
import me.ellios.hedwig.rpc.loadbalancer.*;
import me.ellios.hedwig.rpc.loadbalancer.factory.LoadBalancerFactory;
import me.ellios.hedwig.rpc.proxy.ServiceInvoker;
import me.ellios.hedwig.zookeeper.DataListener;
import me.ellios.hedwig.zookeeper.ZookeeperClient;
import me.ellios.hedwig.zookeeper.ZookeeperClientFactory;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.ReentrantLock;


public abstract class AbstractServiceInvoker<R> implements ServiceInvoker {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractServiceInvoker.class);
    protected final String serviceName;
    protected final String serviceGroup;
    protected final LoadBalancerFactory loadBalancerFactory;
    protected volatile LoadBalancer<R> loadBalancer;
    private final ZookeeperClient zookeeperClient = ZookeeperClientFactory.getZookeeperClient();
    protected final Registry registry;
    private final ReentrantLock subscribeLock = new ReentrantLock();

    private final transient ConcurrentMap<ServiceNode, DataListener> track = new ConcurrentHashMap<>();
    private final transient ConcurrentMap<ServiceNode, FutureTask<R>> nodePoolMap;
    private final ResourceCreator<ServiceNode, R> resourceCreator;

    protected AbstractServiceInvoker(String serviceGroup, String serviceName,
                                     LoadBalancerFactory loadBalancerFactory) {
        this.serviceName = serviceName;
        this.serviceGroup = serviceGroup;
        this.loadBalancerFactory = loadBalancerFactory;
        registry = ZookeeperRegistryFactory.getInstance().getRegistry(serviceGroup);
        this.nodePoolMap = new ConcurrentHashMap<>();
        this.resourceCreator = new ResourceCreator<>(this.nodePoolMap);
    }

    protected abstract void destroy(R resource);

    @Override
    public void destroy() {
        //取消订阅
        registry.unsubscribe(serviceName, getCallbackWatcher());
        registry.destroy();

        //销毁连接池
        LOG.info("Ready to shutdown all connection pools.");
        destroyPools();

        // Close Zookeeper client connection.
        LOG.info("Close Zookeeper client connection. ");
        zookeeperClient.close();
    }

    @Override
    public Object invoke(Method method, Object[] args) {
        Object target = getTarget();
        try {
            return doInvoke(target, method, args);
        } catch (HedwigTransportException e) {
            // If it's a connection issue, we try one more time.
            LOG.warn("Due to transport fail, we try this one more time, method: {}, args:{}", method, args);
            try {
                return doInvoke(target, method, args);
            } catch (HedwigTransportException ex) {
                LOG.error("Transport failed again, method: {}, args: {}, return null.", method, args, ex);
                return null;
            } catch (Exception exc) {
                throw new HedwigException(e);
            }
        } catch (Exception e) {
            throw new HedwigException(e);
        }
    }

    abstract protected Object doInvoke(Object target, Method method, Object[] args);

    abstract protected CallbackWatcher getCallbackWatcher();

    protected Object getTarget() {
        return null;
    }

    /**
     * Create one Client connection pool for the Service Node.
     *
     * @param node the service node.
     * @return The client connection pool, a.k.a {@code R} resource.
     */
    abstract protected R createPool(ServiceNode node);


    /**
     * 构建负载均衡选择器
     *
     * @param serviceNodes all the available service nodes of the service.
     * @return the load balancer
     */
    protected LoadBalancer<R> buildLoadBalancer(List<ServiceNode> serviceNodes) {
        if (CollectionUtils.isEmpty(serviceNodes)) {
            LOG.error("ServiceNodeList is empty. LoadBalancer cannot exist without any resources.");
            // This will clear the previous load balancer.
            loadBalancer = null;
            destroyPools();
        }
        try {
            // Remove duplicated service node if any.
            Set<ServiceNode> nodes = new HashSet<>(serviceNodes);
            // Create fresh new resources.
            List<R> resources = resourceCreator.create(nodes, new Function<ServiceNode, R>() {
                @Override
                public R apply(ServiceNode node) {
                    return createPool(node);
                }
            });
            // We should first reset the load balancer then clean up the dead pools.
            loadBalancer = loadBalancerFactory.newLoadBalancer(resources);
            // Clean up useless pools to prevent resource leak.
            destroyOfflinePool(nodes);
        } catch (Exception e) {
            throw new HedwigException("Create client pool fail. serviceNodeList : " + serviceNodes, e);
        }
        return loadBalancer;
    }

    /**
     * Destroy old offline pool.
     */

    protected void destroyOfflinePool(Set<ServiceNode> validServiceNodes) {
        for (final ServiceNode node : nodePoolMap.keySet()) {
            if (!validServiceNodes.contains(node)) {
                destroyPool(node, nodePoolMap.remove(node));
            }
        }
    }

    private void destroyPool(ServiceNode node, FutureTask<R> future) {
        if (null != future) {
            R pool = FutureRunner.run(future);
            if (null != pool) {
                LOG.info("Destroy pool for node {}, close it now.", node);
                destroy(pool);
            }
        }
    }

    protected void destroyPools() {
        for (Map.Entry<ServiceNode, FutureTask<R>> entry : nodePoolMap.entrySet()) {
            destroyPool(entry.getKey(), entry.getValue());
        }
        // Should clear out the resource the same time.
        nodePoolMap.clear();
    }

    protected void registerDataListener(List<ServiceNode> nodes, LoadBalancer<R> balancer) {
        for (int i = 0; i < nodes.size(); i++) {
            ServiceNode node = nodes.get(i);
            final R pool = balancer.all().get(i);
            // We should do this to prevent memory leaking.
            DataListener old = track.remove(node);
            if (null != old) {
                zookeeperClient.removeDataListener(node.getZnodePath(), old);
            }
            // Create the data listener.
            DataListener dl = createDataListener(pool, balancer);
            // register data change listener.
            zookeeperClient.getData(node.getZnodePath(), dl);
            // keep track of the data listener.
            track.put(node, dl);
        }
    }

    private DataListener createDataListener(final R resource, final LoadBalancer<R> balancer) {
        // This will be executed in another thread.
        return new DataListener() {
            @Override
            public void dataChanged(String path, byte[] data) {
                if (null == data || data.length == 0) {
                    LOG.warn("Service Node {}, data was cleared out.", path);
                    return;
                }
                ServiceNode node = ServiceNode.fromJson(data);
                if (resource instanceof ConnectionResource) {
                    ConnectionResource r = (ConnectionResource) resource;
                    r.addAndGetConnections(node.getConnections() - r.connections());
                }
                if (resource instanceof WeightedResource) {
                    WeightedResource r = (WeightedResource) resource;
                    r.addAndGetWeight(node.getWeight() - r.weight());
                }
                if (balancer instanceof ParameterizedLoadBalancer) {
                    ParameterizedLoadBalancer<R> load = (ParameterizedLoadBalancer<R>) balancer;
                    load.refresh();
                }
            }
        };
    }

    /**
     * Pull service nodes from zookeeper, and for each node, build a client pool connected to the node
     * then use {@link me.ellios.hedwig.rpc.loadbalancer.LoadBalancer} to choose one of the client pools to fulfill the client's functionality.
     *
     * @return The client connection pool for some service node.
     */
    public R getPool() {
        if (loadBalancer == null) {
            subscribeLock.lock();
            try {
                if (loadBalancer == null) {
                    // We ONLY get notified when there are child nodes got created or deleted.
                    // We does NOT get notified because the children nodes' data changed.
                    registry.subscribe(serviceName, getCallbackWatcher());
                }
            } finally {
                subscribeLock.unlock();
            }
        }
        if (loadBalancer == null || loadBalancer.isEmpty()) {
            throw new HedwigException("No service node found for service: " + serviceName);
        }
        return loadBalancer.select();
    }

}
