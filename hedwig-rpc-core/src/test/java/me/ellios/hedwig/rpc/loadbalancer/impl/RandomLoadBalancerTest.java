package me.ellios.hedwig.rpc.loadbalancer.impl;

import me.ellios.hedwig.rpc.loadbalancer.LoadBalancer;
import me.ellios.hedwig.rpc.loadbalancer.Strategy;
import me.ellios.hedwig.rpc.loadbalancer.factory.LoadBalancerFactory;
import me.ellios.hedwig.rpc.loadbalancer.factory.LoadBalancerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Say something?
 *
 * @author George Cao
 * @since 3/19/13 4:22 PM
 */
public class RandomLoadBalancerTest {
    private static final Logger LOG = LoggerFactory.getLogger(RandomLoadBalancerTest.class);

    LoadBalancerFactory factory;

    @BeforeMethod
    public void setUp() throws Exception {
        factory = LoadBalancerSupport.newFactory(Strategy.RANDOM);
    }

    private List<Object> makeResources(int size) {
        List<Object> resources = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            resources.add(new Object());
        }
        return resources;
    }

    @Test
    public void testSelect() throws Exception {
        int size = 10;
        List<Object> resources = makeResources(size);
        LoadBalancer<Object> balancer = factory.newLoadBalancer(resources);
        Map<Integer, AtomicLong> stat = new HashMap<>(size);
        for (int i = 0; i < 10000 * size; i++) {
            int index = balancer.selectIndex();
            if (!stat.containsKey(index)) {
                stat.put(index, new AtomicLong(0));
            }
            stat.get(index).incrementAndGet();
        }

        for (int i = 0; i < size; i++) {
            System.out.printf("%2d->%10d%n", i, stat.get(i).get());
        }
    }
}
