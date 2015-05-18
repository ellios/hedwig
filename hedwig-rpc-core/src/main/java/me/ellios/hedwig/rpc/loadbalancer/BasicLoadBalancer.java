package me.ellios.hedwig.rpc.loadbalancer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Say something?
 *
 * @author George Cao
 * @since 3/19/13 11:34 AM
 */
public abstract class BasicLoadBalancer<R> implements LoadBalancer<R> {

    private static final Logger LOG = LoggerFactory.getLogger(BasicLoadBalancer.class);
    private volatile List<R> resources = Collections.emptyList();

    public BasicLoadBalancer(List<R> resources) {
        reset(resources);
    }

    @Override
    public void reset(List<R> resources) {
        if (null == resources || resources.isEmpty()) {
            throw new IllegalArgumentException("Resources for load balancer cannot be null or empty.");
        }
        this.resources = new ArrayList<>(resources);
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

    @Override
    public boolean isEmpty() {
        return 0 == size();
    }
}
