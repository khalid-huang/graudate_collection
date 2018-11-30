package org.sysu.nameservice.loadbalancer.monitor;


import org.sysu.nameservice.loadbalancer.util.Clock;
import org.sysu.nameservice.loadbalancer.util.ClockWithOffset;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class BasicTimer extends AbstractMonitor<Long> implements Timer {

    private final TimeUnit timeUnit;

    /** 与纳秒的转化倍数*/
    private final double timeUnitNanosFactor;

    /** 整体的时间，会记录多个stopwatch的duration和 */
//    private final StepCounter totalTime;
    private final AtomicLong totalTime;

    /** 记录了几个stopwatch的时间 */
//    private final StepCounter count;
    private final AtomicLong count;

    /** duration中的最小值*/
//    private final MinGauge min;
    /** duration中的最大值*/
//    private final MaxGauge max;

    public BasicTimer(MonitorConfig config) {
        this(config, TimeUnit.MILLISECONDS);
    }

    public BasicTimer(MonitorConfig config, TimeUnit unit) {
        this(config, unit, ClockWithOffset.INSTANCE);
    }

    BasicTimer(MonitorConfig config, TimeUnit unit, Clock clock) {
        super(config);
        timeUnit = unit;
        timeUnitNanosFactor = 1.0 / timeUnit.toNanos(1);
        totalTime = new AtomicLong(0L);
        count = new AtomicLong(0L);
    }

    @Override
    public StopWatch start() {
        //StopWatch负责了控制时间的运行
        StopWatch s = new TimedStopWatch(this);
        s.start();
        return s;
    }

    @Override
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    private void recordNanos(long nanos) {
        if(nanos > 0) {
            totalTime.getAndAdd(nanos);
            count.incrementAndGet();
        }
    }

    @Override
    public void record(long duration, TimeUnit timeUnit) {
        long nanos = timeUnit.toNanos(duration);
        recordNanos(nanos);
    }

    private double getTotal() {
        return totalTime.get();
    }

    //怪怪的
    @Override
    public Long getValue() {
        final long cnt = count.get();
        final long value = (long)(getTotal() / cnt);
        return cnt == 0 ? 0L : value;
    }
}
