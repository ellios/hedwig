package me.ellios.jedis.loadbalancer;

/**
 * Parameterized resource. e.g. weighted or least connections resource.
 *
 * @author George Cao
 * @since 3/19/13 11:54 AM
 */
public interface WeightedResource extends me.ellios.jedis.loadbalancer.Resource {

    /**
     * Get the weight.
     *
     * @return the current weight.
     */
    int weight();

}
