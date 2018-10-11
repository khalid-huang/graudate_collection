package org.sysu.nameservice.loadbalancer.monitor;

/**
 * Monitor type for tracking how often some event is occurring
 */
public interface Counter extends NumericMonitor<Number> {
    /** Update the count by on .*/
    void increment();

    /** Update the count by specified amount .*/
    void increment(long amount);
}
