package me.ellios.hedwig.rpc.loadbalancer.impl;


import me.ellios.hedwig.rpc.loadbalancer.BasicLoadBalancer;

import java.util.List;

/**
 * Round Robin balancer.
 * http://kb.linuxvirtualserver.org/wiki/Round-Robin_Scheduling
 *
 * @author George Cao
 * @since 3/19/13 11:52 AM
 */
public class RoundRobinLoadBalancer<R> extends BasicLoadBalancer<R> {
    private int counter = -1;

    public RoundRobinLoadBalancer(List<R> resources) {
        super(resources);
    }

    @Override
    public int selectIndex() {
        counter = (counter + 1) % size();
        return counter;
    }
}
