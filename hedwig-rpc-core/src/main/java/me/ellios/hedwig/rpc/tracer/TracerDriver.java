package me.ellios.hedwig.rpc.tracer;

import java.util.concurrent.TimeUnit;

/**
 * Mechanism for timing methods and recording counters.
 *
 * @author George
 * @since 13-3-4 下午3:42
 */

public interface TracerDriver {

    /**
     * Default tracer, just discard all info.
     */
    static final TracerDriver DISCARD_TRACER = new TracerDriver() {
        @Override
        public void addTrace(String name, long time, TimeUnit unit) {
            // no-op
        }

        @Override
        public void addCount(String name, int increment) {
            // no-op
        }

        @Override
        public void addGauge(String name, Object value) {
            // no-op
        }
    };

    /**
     * Record the given trace event
     *
     * @param name of the event
     * @param time time event took
     * @param unit time unit
     */
    public void addTrace(String name, long time, TimeUnit unit);

    /**
     * Add to a named counter
     *
     * @param name      name of the counter
     * @param increment amount to increment
     */
    public void addCount(String name, int increment);

    /**
     * Record a named stat info.
     *
     * @param name  name of the stat.
     * @param value value of this stat
     */
    public void addGauge(String name, Object value);
}
