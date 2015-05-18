package me.ellios.hedwig.rpc.loadbalancer.impl;

import com.google.common.base.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Say something?
 *
 * @author George Cao
 * @since 3/20/13 3:56 PM
 */
public abstract class LoadBalancerTest {
    private static final Logger LOG = LoggerFactory.getLogger(LoadBalancerTest.class);

    public <R> List<R> make(int size, Class<R> cl) throws IllegalAccessException, InstantiationException {
        List<R> r = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            r.add(cl.newInstance());
        }
        return r;
    }

    public void round(int total, Function function) {
        for (int i = 0; i < total; i++) {

        }
    }
}
