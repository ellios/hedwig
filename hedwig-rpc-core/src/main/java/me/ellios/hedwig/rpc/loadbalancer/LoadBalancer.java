package me.ellios.hedwig.rpc.loadbalancer;

import java.util.List;

/**
 * The Load Balancer interface for the special Resource {@link R}.
 * Use {@link me.ellios.hedwig.rpc.loadbalancer.factory.LoadBalancerFactory#newLoadBalancer(java.util.List)} to
 * obtain a Load Balancer with the given resources.
 * Then call {@link #select()} to choose one resource to use.
 * After you created the load balancer, you can reset the resources by {@link #reset(java.util.List)}
 * <p/>
 * TODO we need to check if the resource is available after we select it. If not, we should try next resource.
 *
 * @author George Cao
 * @since 3/19/13 11:31 AM
 */
public interface LoadBalancer<R> {
    /**
     * Reset the resources with fresh new ones.
     *
     * @param resources the given resources
     */
    void reset(List<R> resources);

    /**
     * Return all the resource this load balancer holds.
     *
     * @return a list of {@link R}
     */
    List<R> all();

    /**
     * Got the next resource's index in the list in case you need it.
     * Usually you just call {@link #select()} to select the next resource.
     *
     * @return resource index.
     * @see #select()
     */
    int selectIndex();

    /**
     * Select the next resource.
     *
     * @return the resource selected.
     */
    R select();

    /**
     * If you got an index by {@link #selectIndex()}, then  you can fetch the specific resource by this method.
     *
     * @param index the resource index
     * @return the resource identified by the index.
     * @see #select()
     */
    R selectByIndex(int index);

    /**
     * The total size of the resources.
     *
     * @return number of the resources.
     */
    int size();

    boolean isEmpty();
}
