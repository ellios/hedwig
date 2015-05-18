package me.ellios.hedwig.rpc.loadbalancer.impl;


import me.ellios.hedwig.rpc.loadbalancer.BasicLoadBalancer;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Random balancer.
 *
 * @author George Cao
 * @since 3/19/13 11:50 AM
 */
public class RandomLoadBalancer<R> extends BasicLoadBalancer<R> {

    public RandomLoadBalancer(List<R> resources) {
        super(resources);
    }

    @Override
    public int selectIndex() {
        return ThreadLocalRandom.current().nextInt(size());
    }
}
