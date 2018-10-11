package org.sysu.nameservice.loadbalancer.monitor;

import java.util.concurrent.TimeUnit;

/**
 * Monitor type for tracking how much time something is taking
 */
public interface Timer extends NumericMonitor<Long> {
    /**
     * return a stopwatch that has started and will automatically record its result to this timer when stopeed
     * @return
     */
    StopWatch start();


    TimeUnit getTimeUnit();

    /**
     * Record a new value that was collected with TimeUnit
     * @param duration
     * @param timeUnit
     */
    void record(long duration, TimeUnit timeUnit);
}
