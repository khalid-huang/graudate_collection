package org.sysu.nameservice.loadbalancer.monitor;

import java.util.concurrent.TimeUnit;

/**
 * Measures the time taken for execution of some code
 */
public interface StopWatch {
    /**
     * Mark the start time
     */
    void start();

    /**
     * Mark the end time
     */
    void stop();

    /**
     * reset the stopwatch so that it can be used again
     */
    void reset();

    /**
     * return the duration in the specified time unit
     * @param timeUnit
     * @return
     */
    long getDuration(TimeUnit timeUnit);

    /**
     * return the duration in nanoseconds
     * @return
     */
    long getDuration();

}
