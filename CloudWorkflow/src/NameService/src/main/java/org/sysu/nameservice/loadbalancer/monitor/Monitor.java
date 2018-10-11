package org.sysu.nameservice.loadbalancer.monitor;

/**
 * Providers a way to sample a value tied to a particular configuration
 * @param <T>
 */
public interface Monitor<T> {

    /**
     * Returns the current value for the monitor for the default polling interval;
     */
    T getValue();

    /**
     * Return the current value for the monitor for the nth poller
     */
//    T getValue(int pollerIndex);

    /**
     * Configuration used to identify a monitor
     * @return
     */
    MonitorConfig getConfig();

}
