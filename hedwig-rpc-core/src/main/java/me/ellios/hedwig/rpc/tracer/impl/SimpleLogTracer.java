package me.ellios.hedwig.rpc.tracer.impl;

import me.ellios.hedwig.rpc.tracer.TracerDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple log tracer. Output every log every time.
 *
 * @author George Cao
 * @since 13-3-4 下午3:43
 */

public class SimpleLogTracer implements TracerDriver {

    private static final ConcurrentMap<String, AtomicLong> COUNTER_MAP = new ConcurrentHashMap<>();
    private static final Logger LOG = LoggerFactory.getLogger(SimpleLogTracer.class);

    public SimpleLogTracer() {
    }

    @Override
    public void addTrace(String name, long time, TimeUnit unit) {
        LOG.info("Trace: {}, time: {}, unit: {}", name, time, unit);
    }

    @Override
    public void addGauge(String name, Object value) {
        LOG.debug("Stat: {}, value: {}", name, value);
    }

    @Override
    public void addCount(String name, int increment) {
        if (!COUNTER_MAP.containsKey(name)) {
            COUNTER_MAP.putIfAbsent(name, new AtomicLong(0));
        }
        long value = COUNTER_MAP.get(name).addAndGet(increment);
        LOG.debug("Counter: {}, delta: {}, total: {}", name, increment, value);
    }
}
