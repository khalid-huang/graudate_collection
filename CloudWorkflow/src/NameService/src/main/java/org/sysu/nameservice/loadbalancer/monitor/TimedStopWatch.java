package org.sysu.nameservice.loadbalancer.monitor;

import java.util.concurrent.TimeUnit;

/**
 * Stopwatch that will also record to a timer;
 * 相当于这个stopawatch是用一段段的，比如单个时间槽；而Timer就是整个时间槽区间
 */
public class TimedStopWatch extends BasicStopWatch {
    private final Timer timer;

    public TimedStopWatch(Timer timer) {
        this.timer = timer;
    }

    @Override
    public void stop() {
        super.stop();
        timer.record(getDuration(), TimeUnit.NANOSECONDS);
    }



}
