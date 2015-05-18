package me.ellios.hedwig.rpc.client.proxy.thrift;

import me.ellios.hedwig.rpc.client.proxy.AbstractServiceProxyFactory;
import me.ellios.hedwig.rpc.loadbalancer.factory.LoadBalancerFactory;
import me.ellios.hedwig.rpc.proxy.ServiceInvoker;
import me.ellios.hedwig.zookeeper.config.ZookeeperConfig;

import java.lang.reflect.Proxy;

/**
 * Author: ellios
 * Date: 12-10-30 Time: 下午8:33
 */
public class ThriftServiceProxyFactory extends AbstractServiceProxyFactory {

    private static final ThriftServiceProxyFactory instance = new ThriftServiceProxyFactory();

    private ThriftServiceProxyFactory() {
    }

    @Override
    protected <T> T createServiceProxy(String serviceName, LoadBalancerFactory factory, Class<T> clazz) {
        return createServiceProxy(ZookeeperConfig.getServiceGroup(serviceName), serviceName, factory, clazz);
    }

    @Override
    protected <T> T createServiceProxy(String serviceGroup, String serviceName, LoadBalancerFactory factory, Class<T> clazz) {
        final ServiceInvoker invoker = new ThriftServiceInvoker(serviceGroup, serviceName, factory);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                invoker.destroy();
            }
        }));
        Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[]{clazz}, new ThriftServiceInvocationHandler(invoker));
        return clazz.cast(proxy);
    }


    public static ThriftServiceProxyFactory getInstance() {
        return instance;
    }
}
