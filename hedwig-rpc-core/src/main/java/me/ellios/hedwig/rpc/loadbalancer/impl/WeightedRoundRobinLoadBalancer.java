package me.ellios.hedwig.rpc.loadbalancer.impl;

import me.ellios.hedwig.rpc.loadbalancer.DistributionRatio;
import me.ellios.hedwig.rpc.loadbalancer.Function;
import me.ellios.hedwig.rpc.loadbalancer.ParameterizedLoadBalancer;
import me.ellios.hedwig.rpc.loadbalancer.WeightedResource;

import java.util.List;

/**
 * Weighted Round Robin load balancer.
 * http://kb.linuxvirtualserver.org/wiki/Weighted_Round-Robin_Scheduling.
 *
 * @author George Cao
 * @since 3/19/13 11:55 AM
 */
public class WeightedRoundRobinLoadBalancer<R> extends ParameterizedLoadBalancer<R> {
    private int index = 0;

    public WeightedRoundRobinLoadBalancer(List<R> resources) {
        super(resources);
    }

    @Override
    protected Function<R, DistributionRatio> function() {
        return new Function<R, DistributionRatio>() {
            @Override
            public DistributionRatio apply(int index, R resource) {
                if (resource instanceof WeightedResource) {
                    WeightedResource wr = (WeightedResource) resource;
                    return new DistributionRatio(index, wr.weight());
                } else {
                    throw new IllegalArgumentException("Not supported resource " + resource);
                }
            }
        };
    }

    @Override
    public int selectIndex() {
        if (isRuntimeRatiosZeroed()) {
            resetRuntimeRatios();
            index = 0;
        }
        boolean found = false;
        while (!found) {
            if (index >= getRuntimeRatios().size()) {
                index = 0;
            }
            DistributionRatio dr = getRuntimeRatios().get(index);
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
