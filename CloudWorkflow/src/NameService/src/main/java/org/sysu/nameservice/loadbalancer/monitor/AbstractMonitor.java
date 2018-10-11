package org.sysu.nameservice.loadbalancer.monitor;

public abstract class AbstractMonitor<T> implements Monitor<T> {
    protected final MonitorConfig config;

    protected AbstractMonitor(MonitorConfig config) {
        this.config = config;
    }

    @Override
    public MonitorConfig getConfig() {
        return config;
    }

}
