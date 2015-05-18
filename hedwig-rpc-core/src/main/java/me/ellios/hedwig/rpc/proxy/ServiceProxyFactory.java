package me.ellios.hedwig.rpc.proxy;


import me.ellios.hedwig.rpc.loadbalancer.Strategy;
import me.ellios.hedwig.rpc.loadbalancer.factory.LoadBalancerFactory;

/**
 * Util class.
 * Author: ellios
 * Date: 12-10-30 Time: 下午8:17
 */
public interface ServiceProxyFactory {
    public <T> T getService(String serviceName, Strategy strategy, Class<T> clazz);

    public <T> T getService(String serviceName, LoadBalancerFactory factory, Class<T> clazz);

    public <T> T getService(String serviceGroup, String serviceName, LoadBalancerFactory factory, Class<T> clazz);

    public <T> T getService(String serviceName, Class<T> clazz);
}
