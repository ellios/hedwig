package me.ellios.jedis.loadbalancer.impl;

import me.ellios.jedis.loadbalancer.DistributionRatio;
import me.ellios.jedis.loadbalancer.Function;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Weighted Round Robin load balancer.
 * http://kb.linuxvirtualserver.org/wiki/Weighted_Round-Robin_Scheduling.
 *
 * @author George Cao
 * @since 3/19/13 11:55 AM
 */
public class WeightedRoundRobinLoadBalancer<R> extends me.ellios.jedis.loadbalancer.ParameterizedLoadBalancer<R> {
    private volatile int index = 0;
    private ReentrantLock lock = new ReentrantLock();

    public WeightedRoundRobinLoadBalancer(List<R> resources) {
        super(resources);
    }

    @Override
    protected Function<R, DistributionRatio> function() {
        return new me.ellios.jedis.loadbalancer.Function<R, me.ellios.jedis.loadbalancer.DistributionRatio>() {
            @Override
            public me.ellios.jedis.loadbalancer.DistributionRatio apply(int index, R resource) {
                if (resource instanceof me.ellios.jedis.loadbalancer.WeightedResource) {
                    me.ellios.jedis.loadbalancer.WeightedResource wr = (me.ellios.jedis.loadbalancer.WeightedResource) resource;
                    return new me.ellios.jedis.loadbalancer.DistributionRatio(index, wr.weight());
                } else {
                    throw new IllegalArgumentException("Not supported resource " + resource);
                }
            }
        };
    }

    @Override
    public int selectIndex() {
        //加锁避免同步问题
        if (isRuntimeRatiosZeroed()) {
            resetRuntimeRatios();
            index = 0;
        }
        boolean found = false;
        while (!found) {
            if (index >= getRuntimeRatios().size()) {
                index = 0;
            }
            me.ellios.jedis.loadbalancer.DistributionRatio dr = getRuntimeRatios().get(index);
            if (dr.getRuntimeWeight() > 0) {
                dr.setRuntimeWeight((dr.getRuntimeWeight()) - 1);
                found = true;
            } else {
                index++;
            }
        }
        return index;
    }
}
