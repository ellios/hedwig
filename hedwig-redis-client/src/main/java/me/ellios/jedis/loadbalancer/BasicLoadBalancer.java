package me.ellios.jedis.loadbalancer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * Say something?
 *
 * @author George Cao
 * @since 3/19/13 11:34 AM
 */
public abstract class BasicLoadBalancer<R> implements me.ellios.jedis.loadbalancer.LoadBalancer<R> {
    private static final Logger LOG = LoggerFactory.getLogger(BasicLoadBalancer.class);
    private volatile List<R> resources = Collections.emptyList();

    public BasicLoadBalancer(List<R> resources) {
        reset(resources);
    }

    @Override
    public void reset(List<R> resources) {
        Preconditions.checkArgument(null != resources, "Resources for load balancer cannot be null.");
        Preconditions.checkArgument(!resources.isEmpty(), "Resources for load balancer cannot be empty.");
        this.resources = Lists.newArrayList(resources);
    }

    @Override
    public List<R> all() {
        return resources;
    }

    @Override
    public R select() {
        int index = selectIndex();
        return selectByIndex(index);
    }

    public R selectByIndex(int index) {
//        LOG.info("Select # {} out of {} resources.", index, size());
        return resources.get(index);
    }

    @Override
    public int size() {
        return resources.size();
    }
}
