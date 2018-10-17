package org.sysu.nameservice.loadbalancer.monitor;


import java.util.concurrent.TimeUnit;

/**
 * some help functions for creating monitor objects
 */
public class Monitors {
    private static final String DEFAULT_ID = "default";

//    private static class TimerFactory implements

    private Monitors() {
    }

    /**
     * create a new timer instance
     */
    public static Timer newTimer(String name) {
        return newTimer(name, TimeUnit.MILLISECONDS);
    }

    /**
     * create a new timer instance
     */
    public static Timer newTimer(String name, TimeUnit unit) {
        return new BasicTimer(MonitorConfig.builder(name).build(), unit);
    }

    /**
     * create a new counter instance
     */
    public static BasicCounter newBasicCounter(String name) {
        return new BasicCounter(MonitorConfig.builder(name).build());
    }

}
