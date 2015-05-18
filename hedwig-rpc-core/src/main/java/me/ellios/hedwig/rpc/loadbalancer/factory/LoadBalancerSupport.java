package me.ellios.hedwig.rpc.loadbalancer.factory;

import com.google.common.base.Preconditions;
import me.ellios.hedwig.rpc.loadbalancer.LoadBalancer;
import me.ellios.hedwig.rpc.loadbalancer.Strategy;
import me.ellios.hedwig.rpc.loadbalancer.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Say something?
 *
 * @author George Cao
 * @since 3/19/13 3:57 PM
 */
public class LoadBalancerSupport {
    private static final Logger LOG = LoggerFactory.getLogger(LoadBalancerSupport.class);

    public static LoadBalancerFactory newFactory(Strategy strategy) {
        Preconditions.checkArgument(null != strategy);
        switch (strategy) {
            case WEIGHTED_RANDOM:
                return new LoadBalancerFactory() {
                    @Override
                    public <R> LoadBalancer<R> newLoadBalancer(List<R> resources) {
                        return new WeightedRandomLoadBalancer<>(resources);
                    }
                };
            case WEIGHTED_ROUND_ROBIN:
                return new LoadBalancerFactory() {
                    @Override
                    public <R> LoadBalancer<R> newLoadBalancer(List<R> resources) {
                        return new WeightedRoundRobinLoadBalancer<>(resources);
                    }
                };
            case ROUND_ROBIN:
                return new LoadBalancerFactory() {
                    @Override
                    public <R> LoadBalancer<R> newLoadBalancer(List<R> resources) {
                        return new RoundRobinLoadBalancer<>(resources);
                    }
                };
            case LEAST_CONNECTION:
                return new LoadBalancerFactory() {
                    @Override
                    public <R> LoadBalancer<R> newLoadBalancer(List<R> resources) {
                        return new LeastConnectionLoadBalancer<>(resources);
                    }
                };
            case RANDOM:
            default:
                return new LoadBalancerFactory() {
                    @Override
                    public <R> LoadBalancer<R> newLoadBalancer(List<R> resources) {
                        return new RandomLoadBalancer<>(resources);
                    }
                };
        }
    }
}
