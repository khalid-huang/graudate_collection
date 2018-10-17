package org.sysu.nameservice.loadbalancer.monitor;

/**
 * Monitor type for tracking how often some event is occurring
 * 也就是说用于追求某些事件的发生次数，比如在一段时间内的请求数；单位就是次/时间；与Timer接口不同，它是时间/次
 */
public interface Counter extends NumericMonitor<Number> {
    /** Update the count by on .*/
    void increment();

    /** Update the count by specified amount .*/
    void increment(long amount);

    void decrement();

    void decrement(long amount);
}
