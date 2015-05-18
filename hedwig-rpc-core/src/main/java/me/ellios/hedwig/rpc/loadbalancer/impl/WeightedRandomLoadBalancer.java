package me.ellios.hedwig.rpc.loadbalancer.impl;


import me.ellios.hedwig.rpc.loadbalancer.DistributionRatio;
import me.ellios.hedwig.rpc.loadbalancer.Function;
import me.ellios.hedwig.rpc.loadbalancer.ParameterizedLoadBalancer;
import me.ellios.hedwig.rpc.loadbalancer.WeightedResource;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Say something?
 *
 * @author George Cao
 * @since 3/19/13 3:27 PM
 */
public class WeightedRandomLoadBalancer<R> extends ParameterizedLoadBalancer<R> {
    private int distributionRatioSum;
    private int runtimeRatioSum;

    public WeightedRandomLoadBalancer(List<R> resources) {
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
    public void reset(List<R> resources) {
        reset(resources, function());
        int sum = 0;
        for (DistributionRatio ratio : getRuntimeRatios()) {
            sum += ratio.getDistributionWeight();
        }
        distributionRatioSum = sum;
        runtimeRatioSum = distributionRatioSum;
    }

    @Override
    public int selectIndex() {
        if (runtimeRatioSum == 0) { // every resource is exhausted, reload for a new distribution round
            resetRuntimeRatios();
            runtimeRatioSum = distributionRatioSum;
        }
        DistributionRatio selected = null;
        int randomWeight = ThreadLocalRandom.current().nextInt(runtimeRatioSum);
        int choiceWeight = 0;
        for (DistributionRatio distributionRatio : getRuntimeRatios()) {
            choiceWeight += distributionRatio.getRuntimeWeight();
            if (randomWeight < choiceWeight) {
                selected = distributionRatio;
                break;
            }
        }
        selected.setRuntimeWeight(selected.getRuntimeWeight() - 1);
        runtimeRatioSum--;
        return selected.getResourcePosition();
    }
}
