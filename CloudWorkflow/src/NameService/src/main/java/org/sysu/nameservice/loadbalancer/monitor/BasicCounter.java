package org.sysu.nameservice.loadbalancer.monitor;

import javax.transaction.Synchronization;
import java.util.concurrent.atomic.AtomicLong;

public class BasicCounter extends AbstractMonitor<Number> implements Counter {

    private final AtomicLong count = new AtomicLong();

    public BasicCounter(MonitorConfig config) {
        super(config);
    }

    @Override
    public void increment() {
        count.incrementAndGet();
    }

    @Override
    public void increment(long amount) {
        count.getAndAdd(amount);
    }

    @Override
    public void decrement() {
        synchronized ( this) {
            if (count.decrementAndGet() < 0) {
                count.set(0L);
            }
        }
    }

    @Override
    public void decrement(long amount) {
        synchronized (this) {
            if(count.getAndAdd(-amount) < 0 ) {
                count.set(0L);
            }
        }
    }

    @Override
    public Number getValue() {
        return count.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasicCounter that = (BasicCounter) o;
        return count.get() == that.count.get();
    }

    @Override
    public int hashCode() {
        int result = config.hashCode();
        long n = count.get();
        result = 31 * result +  (int) (n ^ (n >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "BasicCounter{config=" + config + ", count=" + count.get() + '}';
    }
}
