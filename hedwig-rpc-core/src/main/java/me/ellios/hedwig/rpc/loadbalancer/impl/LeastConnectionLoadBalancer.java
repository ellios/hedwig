package me.ellios.hedwig.rpc.loadbalancer.impl;

import me.ellios.hedwig.rpc.loadbalancer.ConnectionResource;
import me.ellios.hedwig.rpc.loadbalancer.DistributionRatio;
import me.ellios.hedwig.rpc.loadbalancer.Function;
import me.ellios.hedwig.rpc.loadbalancer.ParameterizedLoadBalancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * The least connections load balancer.
 * http://kb.linuxvirtualserver.org/wiki/Least-Connection_Scheduling.
 *
 * @author George Cao
 * @since 3/19/13 1:41 PM
 */
public class LeastConnectionLoadBalancer<R> extends ParameterizedLoadBalancer<R> {
    private static final Logger LOG = LoggerFactory.getLogger(LeastConnectionLoadBalancer.class);

    public LeastConnectionLoadBalancer(List<R> resources) {
        super(resources);
    }

    protected Function<R, DistributionRatio> function() {
        return new Function<R, DistributionRatio>() {
            @Override
            public DistributionRatio apply(int index, R resource) {
                if (resource instanceof ConnectionResource) {
                    ConnectionResource cr = (ConnectionResource) resource;
                    return new DistributionRatio(index, cr.weight(), cr.connections());
                } else {
                    throw new IllegalArgumentException("Not supported resource " + resource);
                }
            }
        };
    }


    /**
     * Get the resource index. The Least-Connection impl.
     *
     * @return resource index
     */
    @Override
    public int selectIndex() {
        for (int m = 0; m < size(); m++) {
            DistributionRatio dm = getRuntimeRatios().get(m);
            if (dm.getDistributionWeight() > 0) {
                for (int i = m + 1; i < size(); i++) {
                    DistributionRatio di = getRuntimeRatios().get(i);
                    if (di.getDistributionWeight() <= 0) {
                        continue;
                    }
                    if (di.getRuntimeWeight() < dm.getRuntimeWeight()) {
                        m = i;
                    }
                }
                return m;
            }
        }
        return 0;
    }
}
