package org.sysu.nameservice.loadbalancer.monitor;

import java.util.Objects;

public class MonitorConfig {
    /**
     * A builder to assist in creating monitor config objects
     */
    public static class Builder {
        private final String name;

        public Builder(MonitorConfig config) {
            this(config.getName());
        }
        public Builder(String name) {
            this.name = name;
        }

        public MonitorConfig build() {
            return new MonitorConfig(this);
        }

        public String getName() {
            return name;
        }
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }
    private final String name;

    private MonitorConfig(Builder builder) {
        this.name = builder.name;
    }
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MonitorConfig that = (MonitorConfig) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
