package me.ellios.hedwig.rpc.loadbalancer;

/**
 * Parameterized resource. e.g. weighted or least connections resource.
 *
 * @author George Cao
 * @since 3/19/13 11:54 AM
 */
public interface WeightedResource extends Resource {

    /**
     * Get the weight.
     *
     * @return the current weight.
     */
    int weight();

    /**
     * Add the weight by {@code delta} then return the weight.
     *
     * @param delta the delta value
     * @return weight after the change.
     */
    int addAndGetWeight(int delta);
}
