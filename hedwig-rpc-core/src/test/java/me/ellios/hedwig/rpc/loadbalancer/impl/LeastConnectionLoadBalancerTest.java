package me.ellios.hedwig.rpc.loadbalancer.impl;


import me.ellios.hedwig.rpc.loadbalancer.ConnectionResource;
import me.ellios.hedwig.rpc.loadbalancer.LoadBalancer;
import me.ellios.hedwig.rpc.loadbalancer.ParameterizedLoadBalancer;
import me.ellios.hedwig.rpc.loadbalancer.Strategy;
import me.ellios.hedwig.rpc.loadbalancer.factory.LoadBalancerFactory;
import me.ellios.hedwig.rpc.loadbalancer.factory.LoadBalancerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Say something?
 *
 * @author George Cao
 * @since 3/19/13 5:49 PM
 */
public class LeastConnectionLoadBalancerTest {
    private static final Logger LOG = LoggerFactory.getLogger(LeastConnectionLoadBalancerTest.class);

    public static class LeastConnectionResource implements ConnectionResource {
        int index = 0;
        AtomicInteger connec = new AtomicInteger(0);
        AtomicInteger weight = new AtomicInteger(1);

        public int getIndex() {
            return index;
        }

        public LeastConnectionResource(int index) {
            this.index = index;
        }

        @Override
        public int connections() {
            return connec.get();
        }

        @Override
        public int addAndGetConnections(int delta) {
            return connec.addAndGet(delta);
        }

        @Override
        public int weight() {
            return weight.get();
        }

        @Override
        public int addAndGetWeight(int delta) {
            return weight.addAndGet(delta);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            LeastConnectionResource resource = (LeastConnectionResource) o;

            if (index != resource.index) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return index;
        }

        @Override
        public String toString() {
            return "{i:" + index +
                    ", c:" + connec +
                    ", w:" + weight +
                    '}';
        }
    }

    LoadBalancer<LeastConnectionResource> balancer;
    List<LeastConnectionResource> resources;
    int count = 10;

    @BeforeMethod
    public void setUp() throws Exception {
        LoadBalancerFactory factory = LoadBalancerSupport.newFactory(Strategy.LEAST_CONNECTION);
        resources = createResource(count);
        balancer = factory.newLoadBalancer(resources);
    }

    public List<LeastConnectionResource> createResource(int count) {
        List<LeastConnectionResource> resources = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            LeastConnectionResource r = new LeastConnectionResource(i);
            resources.add(r);
            r.addAndGetConnections(ThreadLocalRandom.current().nextInt(count));
            r.addAndGetWeight(2);
        }
        return resources;
    }

    @Test
    public void testReset() throws Exception {
        boolean yes = true;
        try {
            balancer.reset(createResource(0));
            yes = false;
        } catch (IllegalArgumentException ignored) {

        }
        assertTrue(yes);
        for (int i = 1; i < count; i++) {
            balancer.reset(createResource(i));
            assertEquals(i, balancer.all().size());
        }

    }

    @Test
    public void testSelectIndex() throws Exception {
        ParameterizedLoadBalancer<LeastConnectionResource> b = (ParameterizedLoadBalancer) balancer;
        final List<LeastConnectionResource> resources = createResource(count);
        b.reset(resources);
        LOG.info("{}", b.all());
        for (int i = 0; i < 2 * count; i++) {
            int index = b.selectIndex();
            LOG.info("{}", index);
            LeastConnectionResource r = b.selectByIndex(index);
            r.addAndGetConnections(ThreadLocalRandom.current().nextInt(count) - r.connections());
            b.refresh(index, b.selectByIndex(index));
            Thread.sleep(200);
        }
    }
}
