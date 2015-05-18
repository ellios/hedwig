package me.ellios.hedwig.rpc.tracer.impl;

import me.ellios.hedwig.rpc.tracer.TracerDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Sample log tracer. Output the stat info through log at some frequency.
 *
 * @author George Cao
 * @since 13-3-4 下午3:43
 */

public class SampleLogTracer implements TracerDriver {

    private static final ConcurrentMap<String, AtomicLong> COUNTER_MAP
            = new ConcurrentHashMap<>();
    private static final Logger LOG = LoggerFactory.getLogger(SampleLogTracer.class);
    private Logger logger;
    private long index = 0;
    // Just print one line every 3000 log requests.
    private long frequency = 3000;
    private TimeUnit timeUnit;

    public SampleLogTracer() {
        this(LOG);
    }

    public SampleLogTracer(Logger logger, long f) {
        this.frequency = f;
        this.logger = logger;
    }

    public SampleLogTracer(Logger logger) {
        this(logger, 1);
    }

    private boolean should() {
        // overflow
        return 0 == (index++ % frequency);
    }

    @Override
    public void addTrace(String name, long time, TimeUnit unit) {
        addCount(name, 1);
        if (should()) {
            logger.info("Trace: {}, time: {}, unit: {}", name, time, unit);
        }
    }

    @Override
    public void addGauge(String name, Object value) {
        if (should()) {
            logger.info("Stat: {}, value: {}", name, value);
        }
    }

    @Override
    public void addCount(String name, int increment) {
        if (!COUNTER_MAP.containsKey(name)) {
            COUNTER_MAP.putIfAbsent(name, new AtomicLong(0));
        }
        long value = COUNTER_MAP.get(name).addAndGet(increment);
        if (should()) {
            logger.info("Counter: {}, delta: {}, total: {}", name, increment, value);
        }
    }
}
