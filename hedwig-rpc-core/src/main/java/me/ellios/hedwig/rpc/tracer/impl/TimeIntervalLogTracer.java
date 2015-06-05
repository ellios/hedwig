package me.ellios.hedwig.rpc.tracer.impl;

import me.ellios.hedwig.rpc.tracer.TracerDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Say something?
 *
 * @author George Cao
 * @since 2013-12-29 19
 */
public class TimeIntervalLogTracer implements TracerDriver {
    private static final Logger LOG = LoggerFactory.getLogger(TimeIntervalLogTracer.class);

    private long interval;
    private TimeUnit timeUnit;

    @Override
    public void addTrace(String name, long time, TimeUnit unit) {

    }

    @Override
    public void addCount(String name, int increment) {

    }

    @Override
    public void addGauge(String name, Object value) {

    }
}
