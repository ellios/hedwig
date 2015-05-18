package me.ellios.hedwig.rpc.client;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import me.ellios.hedwig.rpc.client.proxy.thrift.ThriftServiceProxyFactory;
import me.ellios.hedwig.rpc.core.ServiceConfigHelper;
import me.ellios.hedwig.rpc.core.ServiceType;
import me.ellios.hedwig.rpc.loadbalancer.Strategy;
import me.ellios.hedwig.rpc.loadbalancer.factory.LoadBalancerFactory;
import me.ellios.hedwig.rpc.loadbalancer.factory.LoadBalancerSupport;
import me.ellios.hedwig.rpc.proxy.ServiceProxyFactory;
import me.ellios.hedwig.zookeeper.config.ZookeeperConfig;
import org.apache.commons.lang3.StringUtils;

/**
 * Create service client.
 *
 * @author George Cao
 * @since 3/21/13 6:11 PM
 */
public class ClientBuilder<T> {

    private String serviceName;
    private LoadBalancerFactory factory;
    private Class<T> clazz;
    private Strategy strategy;
    private ServiceType serviceType;
    private String serviceGroup;
    private String user;
    private String password;

    public static <T> ClientBuilder<T> newClientBuilder() {
        return new ClientBuilder<>();
    }

    public static <T> ClientBuilder<T> newProtobufClientBuilder() {
        return new ClientBuilder<T>().serviceType(ServiceType.PROTOBUF);
    }

    public static <T> ClientBuilder<T> newThriftClientBuilder() {
        return new ClientBuilder<T>().serviceType(ServiceType.THRIFT);
    }

    public ClientBuilder<T> serviceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public ClientBuilder<T> loadBalancerFactory(LoadBalancerFactory factory) {
        this.factory = factory;
        return this;
    }

    public ClientBuilder<T> serviceFace(Class<T> clazz) {
        this.clazz = clazz;
        return this;
    }

    public ClientBuilder<T> loadBalancerStrategy(Strategy strategy) {
        this.strategy = strategy;
        return this;
    }

    public ClientBuilder<T> serviceGroup(String group) {
        this.serviceGroup = group;
        return this;
    }

    public ClientBuilder<T> user(String user) {
        this.user = user;
        return this;
    }

    public ClientBuilder<T> password(String password) {
        this.password = password;
        return this;
    }

    public ClientBuilder<T> serviceType(ServiceType serviceType) {
        this.serviceType = serviceType;
        return this;
    }

    public T build() {
        Preconditions.checkArgument(null != serviceType, "ServiceType cannot be null.");
        Preconditions.checkArgument(null != clazz, "Service interface cannot be null.");
        Preconditions.checkArgument(clazz.isInterface(), "Service interface must be an interface.");
        if (null == factory) {
            if (null == strategy) {
                strategy = Strategy.RANDOM;
            }
            factory = LoadBalancerSupport.newFactory(strategy);
        }
        ServiceProxyFactory proxyFactory;
        switch (serviceType) {
            case THRIFT:
                proxyFactory = ThriftServiceProxyFactory.getInstance();
                break;
            default:
                throw new IllegalArgumentException("Not supported ServiceType: " + serviceType);
        }
        if (StringUtils.isEmpty(serviceName)) {
            serviceName = ServiceConfigHelper.buildDefaultServiceName(serviceType, clazz);
        }
        if (Strings.isNullOrEmpty(serviceGroup)) {
            serviceGroup = ZookeeperConfig.getServiceGroup(serviceName);
        }
        if(!Strings.isNullOrEmpty(user)){
            
        }
        return proxyFactory.getService(serviceGroup, serviceName, factory, clazz);
    }
}
