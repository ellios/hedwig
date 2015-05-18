package me.ellios.hedwig.rpc.client.proxy;

import me.ellios.hedwig.rpc.loadbalancer.Strategy;
import me.ellios.hedwig.rpc.loadbalancer.factory.LoadBalancerFactory;
import me.ellios.hedwig.rpc.loadbalancer.factory.LoadBalancerSupport;
import me.ellios.hedwig.rpc.proxy.ServiceProxyFactory;
import me.ellios.hedwig.zookeeper.config.ZookeeperConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Author: ellios
 * Date: 12-11-4 Time: 下午3:17
 */
abstract public class AbstractServiceProxyFactory implements ServiceProxyFactory {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractServiceProxyFactory.class);
    private final ConcurrentMap<String, Object> serviceProxyMap = new ConcurrentHashMap<>();

    @Override
    public <T> T getService(String serviceName, Class<T> clazz) {
        return getService(serviceName, Strategy.RANDOM, clazz);
    }

    @Override
    public <T> T getService(String serviceName, Strategy strategy, Class<T> clazz) {
        return getService(serviceName, LoadBalancerSupport.newFactory(strategy), clazz);
    }

    @Override
    public <T> T getService(String serviceName, LoadBalancerFactory factory, Class<T> clazz) {
        return getService(ZookeeperConfig.getServiceGroup(serviceName), serviceName, factory, clazz);
    }

    @Override
    public <T> T getService(String serviceGroup, String serviceName, LoadBalancerFactory factory, Class<T> clazz) {
        String key = serviceGroup + serviceName;
        Object serviceProxy = serviceProxyMap.get(key);
        if (serviceProxy == null) {
            LOG.info("Try to create proxy for service {} in group {}", serviceName, serviceGroup);
            serviceProxy = createServiceProxy(serviceGroup, serviceName, factory, clazz);
            serviceProxyMap.putIfAbsent(key, serviceProxy);
        }
        serviceProxy = serviceProxyMap.get(key);
        return clazz.cast(serviceProxy);
    }

    abstract protected <T> T createServiceProxy(String serviceGroup, String serviceName, LoadBalancerFactory factory, Class<T> clazz);

    protected <T> T createServiceProxy(String serviceName, LoadBalancerFactory factory, Class<T> clazz) {
        return createServiceProxy(ZookeeperConfig.getServiceGroup(serviceName), serviceName, factory, clazz);
    }
}