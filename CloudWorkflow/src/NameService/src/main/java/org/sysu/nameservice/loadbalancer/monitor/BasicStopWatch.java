package org.sysu.nameservice.loadbalancer.monitor;


import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class BasicStopWatch implements StopWatch {
    private final AtomicLong startTime = new AtomicLong(0L);
    private final AtomicLong endTime = new AtomicLong(0L);
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public void start() {
        startTime.set(System.nanoTime());
        running.set(true);
    }

    @Override
    public void stop() {
        endTime.set(System.nanoTime());
        running.set(false);
    }

    @Override
    public void reset() {
        startTime.set(0L);
        endTime.set(0L);
        running.set(false);
    }

    @Override
    public long getDuration(TimeUnit timeUnit) {
        return timeUnit.convert(getDuration(), TimeUnit.NANOSECONDS);
    }

    @Override
    public long getDuration() {
        final long end = running.get() ? System.nanoTime() : endTime.get();
        return end - startTime.get();
    }
}
