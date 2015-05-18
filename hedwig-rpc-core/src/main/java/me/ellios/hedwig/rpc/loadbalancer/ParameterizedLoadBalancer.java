package me.ellios.hedwig.rpc.loadbalancer;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for parameterized load balancer like Weighted Round Robin, Weighted Random or Least Connection
 * load balancer. Resource with type {@link R} must implements {@link me.ellios.hedwig.rpc.loadbalancer.WeightedResource} or {@link me.ellios.hedwig.rpc.loadbalancer.ConnectionResource}
 * to let the load balancer get enough info about the resource.
 *
 * @author George Cao
 * @since 3/19/13 11:57 AM
 */
public abstract class ParameterizedLoadBalancer<R> extends BasicLoadBalancer<R> {
    private static final Logger LOG = LoggerFactory.getLogger(ParameterizedLoadBalancer.class);
    private volatile List<DistributionRatio> runtimeRatios;

    protected void reset(List<R> resources, Function<R, DistributionRatio> function) {
        Preconditions.checkArgument(null != resources, "Resources for the load balancer cannot be null.");
        Preconditions.checkArgument(null != function, "Callback function cannot be null.");
        // Set the valid resources.
        super.reset(resources);
        // Set the related parameters.
        resetRuntimeRatios(function);
    }

    @Override
    public void reset(List<R> resources) {
        reset(resources, function());
    }

    public ParameterizedLoadBalancer(List<R> resources) {
        super(resources);
    }

    protected abstract Function<R, DistributionRatio> function();

    /**
     * Refresh the {@link me.ellios.hedwig.rpc.loadbalancer.DistributionRatio} values.
     *
     * @see #resetRuntimeRatios(me.ellios.hedwig.rpc.loadbalancer.Function)
     * @see #function()
     */
    public void refresh() {
        resetRuntimeRatios(function());
    }

    /**
     * Refresh the specific resource.
     *
     * @param index    the index of the resource in the resource pool
     * @param resource the resource
     */
    public void refresh(int index, R resource) {
        DistributionRatio dr = function().apply(index, resource);
        getRuntimeRatios().set(index, dr);
    }


    /**
     * This can only used when original distribution and runtime ratios are same thing.
     *
     * @see #resetRuntimeRatios(me.ellios.hedwig.rpc.loadbalancer.Function)
     */
    protected void resetRuntimeRatios() {
        for (DistributionRatio runtimeRatio : runtimeRatios) {
            runtimeRatio.setRuntimeWeight(runtimeRatio.getDistributionWeight());
        }
    }

    /**
     * Refresh runtime ratios.
     *
     * @param function calculate {@link me.ellios.hedwig.rpc.loadbalancer.DistributionRatio} from the {@code R} resource.
     */
    protected void resetRuntimeRatios(Function<R, DistributionRatio> function) {
        ArrayList<DistributionRatio> runtimeRatios = new ArrayList<>(size());
        int i = 0;
        for (R resource : all()) {
            DistributionRatio dr = function.apply(i++, resource);
            runtimeRatios.add(dr);
        }
        setRuntimeRatios(runtimeRatios);
    }

    public List<DistributionRatio> getRuntimeRatios() {
        return runtimeRatios;
    }

    public void setRuntimeRatios(ArrayList<DistributionRatio> runtimeRatios) {
        this.runtimeRatios = runtimeRatios;
    }


    protected boolean isRuntimeRatiosZeroed() {
        boolean cleared = true;
        for (DistributionRatio runtimeRatio : runtimeRatios) {
            if (runtimeRatio.getRuntimeWeight() > 0) {
                cleared = false;
            }
        }
        return cleared;
    }
}
