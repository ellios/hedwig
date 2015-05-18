package me.ellios.hedwig.rpc.loadbalancer.impl;

import me.ellios.hedwig.rpc.loadbalancer.*;
import me.ellios.hedwig.rpc.loadbalancer.factory.LoadBalancerFactory;
import me.ellios.hedwig.rpc.loadbalancer.factory.LoadBalancerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Say something?
 *
 * @author George Cao
 * @since 3/19/13 5:50 PM
 */
public class WeightedRoundRobinLoadBalancerTest extends LoadBalancerTest {
    private static final Logger LOG = LoggerFactory.getLogger(WeightedRoundRobinLoadBalancerTest.class);

    LoadBalancerFactory factory;

    @BeforeMethod
    public void setUp() throws Exception {
        factory = LoadBalancerSupport.newFactory(Strategy.WEIGHTED_ROUND_ROBIN);
    }

    public static class WR implements WeightedResource {
        @Override
        public int weight() {
            return 1;
        }

        @Override
        public int addAndGetWeight(int delta) {
            return 1;
        }
    }

    @Test
    public void testCreate() throws Exception {
        List<WeightedRoundRobinLoadBalancerTest> resource = make(100, WeightedRoundRobinLoadBalancerTest.class);
        boolean yes = false;
        try {
            LoadBalancer<WeightedRoundRobinLoadBalancerTest> balancer = factory.newLoadBalancer(resource);
        } catch (Exception e) {
            yes = true;
        }
        assertTrue(yes);
        Map<Integer, AtomicLong> stat = new HashMap<>();
        LoadBalancer<WR> balancer = factory.newLoadBalancer(make(10, WR.class));
        assertNotNull(balancer);
        for (int i = 0; i < 10000; i++) {
            int index = balancer.selectIndex();
            if (!stat.containsKey(index)) {
                stat.put(index, new AtomicLong(0));
            }
            stat.get(index).incrementAndGet();
        }
        LOG.info("{}", stat);
    }

    public static class WR2 implements WeightedResource {
        @Override
        public int weight() {
            return ThreadLocalRandom.current().nextInt(20) + 1;
        }

        @Override
        public int addAndGetWeight(int delta) {
            return weight();
        }
    }

    @Test
    public void testSelectIndex() throws Exception {
        Map<Integer, AtomicLong> stat = new HashMap<>();
        LoadBalancer<WR2> balancer = factory.newLoadBalancer(make(10, WR2.class));
        assertNotNull(balancer);
        for (int i = 0; i < 10000; i++) {
            int index = balancer.selectIndex();
            if (!stat.containsKey(index)) {
                stat.put(index, new AtomicLong(0));
            }
            stat.get(index).incrementAndGet();
        }
        LOG.info("{}", stat);
        ParameterizedLoadBalancer<WR2> b = (ParameterizedLoadBalancer<WR2>) balancer;
        for (Map.Entry<Integer, AtomicLong> entry : stat.entrySet()) {
            int index = entry.getKey();
            long count = entry.getValue().get();
            DistributionRatio ratio = b.getRuntimeRatios().get(index);
            int weight = ratio.getDistributionWeight();
            LOG.info("{}->{}", weight, count);
        }
    }
}
