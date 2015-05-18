package me.ellios.hedwig.rpc.loadbalancer;

/**
 * Define least connection load balancer resource.
 * Must be a {@link me.ellios.hedwig.rpc.loadbalancer.WeightedResource}.
 *
 * @author George Cao
 * @since 3/19/13 2:58 PM
 */
public interface ConnectionResource extends WeightedResource {
    /**
     * Get the current connection number.
     *
     * @return the connection count.
     */
    int connections();

    /**
     * Add the connection number by {@code delta} then return the value.
     *
     * @param delta the delta value
     * @return the connection count.
     */
    int addAndGetConnections(int delta);
}
