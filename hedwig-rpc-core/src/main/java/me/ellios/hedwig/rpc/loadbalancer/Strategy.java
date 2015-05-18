package me.ellios.hedwig.rpc.loadbalancer;

/**
 * Load balancer strategies supported right now.
 *
 * @author George Cao
 * @since 3/19/13 3:58 PM
 */
public enum Strategy {
    WEIGHTED_ROUND_ROBIN,
    ROUND_ROBIN,
    RANDOM,
    WEIGHTED_RANDOM,
    LEAST_CONNECTION
}
