package me.ellios.hedwig.rpc.client;


import me.ellios.hedwig.rpc.loadbalancer.Strategy;
import me.ellios.hedwig.rpc.loadbalancer.factory.LoadBalancerFactory;

/**
 * Author: ellios
 * Date: 12-11-4 Time: 下午2:57
 *
 * @see ClientBuilder
 */
public class ServiceHelper {
    // Protobuf Service client utils.

    public static <T> T getPbService(String serviceName, LoadBalancerFactory factory, Class<T> clazz) {
        return ClientBuilder.<T>newProtobufClientBuilder()
                .serviceName(serviceName)
                .loadBalancerFactory(factory)
                .serviceFace(clazz)
                .build();
    }

    public static <T> T getPbService(String serviceName, Strategy strategy, Class<T> clazz) {
        return ClientBuilder.<T>newProtobufClientBuilder()
                .serviceName(serviceName)
                .loadBalancerStrategy(strategy)
                .serviceFace(clazz)
                .build();
    }

    /**
     * 根据服务名获取pb服务，
     *
     * @param serviceName service logic name
     * @param clazz       service interface
     * @param <T>         Type
     * @return Service instance.
     */
    public static <T> T getPbService(String serviceName, Class<T> clazz) {
        return ClientBuilder.<T>newProtobufClientBuilder()
                .serviceName(serviceName)
                .serviceFace(clazz)
                .build();
    }

    /**
     * 根据接口类型获取pb服务
     */
    public static <T> T getPbService(Class<T> clazz) {
        return ClientBuilder.<T>newProtobufClientBuilder()
                .serviceFace(clazz)
                .build();
    }

    // Thrift Service client utils.

    public static <T> T getThriftService(LoadBalancerFactory factory, Class<T> clazz) {
        return ClientBuilder.<T>newThriftClientBuilder()
                .serviceFace(clazz)
                .loadBalancerFactory(factory)
                .build();
    }

    public static <T> T getThriftService(Strategy strategy, Class<T> clazz) {
        return ClientBuilder.<T>newThriftClientBuilder()
                .serviceFace(clazz)
                .loadBalancerStrategy(strategy)
                .build();
    }


    public static <T> T getThriftService(String serviceName, LoadBalancerFactory factory, Class<T> clazz) {
        return ClientBuilder.<T>newThriftClientBuilder()
                .serviceFace(clazz)
                .serviceName(serviceName)
                .loadBalancerFactory(factory)
                .build();
    }

    public static <T> T getThriftService(String serviceName, Strategy strategy, Class<T> clazz) {
        return ClientBuilder.<T>newThriftClientBuilder()
                .serviceFace(clazz)
                .serviceName(serviceName)
                .loadBalancerStrategy(strategy)
                .build();
    }

    public static <T> T getThriftService(String serviceName, Class<T> clazz) {
        return ClientBuilder.<T>newThriftClientBuilder()
                .serviceFace(clazz)
                .serviceName(serviceName)
                .build();
    }

    public static <T> T getThriftService(Class<T> clazz) {
        return ClientBuilder.<T>newThriftClientBuilder()
                .serviceFace(clazz)
                .build();
    }
}
