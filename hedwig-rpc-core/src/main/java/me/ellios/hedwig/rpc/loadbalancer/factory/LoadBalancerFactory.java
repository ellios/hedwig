package me.ellios.hedwig.rpc.loadbalancer.factory;


import me.ellios.hedwig.rpc.loadbalancer.LoadBalancer;

import java.util.List;

/**
 * Say something?
 *
 * @author George Cao
 * @since 3/19/13 3:46 PM
 */
public interface LoadBalancerFactory {

    <R> LoadBalancer<R> newLoadBalancer(List<R> resources);

}
