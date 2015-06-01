package me.ellios.jedis.loadbalancer.impl;

import me.ellios.jedis.loadbalancer.LoadBalancer;

import java.util.List;

/**
 * User: ellios
 * Time: 13-8-6 : 下午10:57
 */
public class LoadBalancerFactory {

    public <R> LoadBalancer<R> newWeightLoadBalancer(List<R> resources) {
        return new WeightedRoundRobinLoadBalancer<R>(resources);
    }
}
