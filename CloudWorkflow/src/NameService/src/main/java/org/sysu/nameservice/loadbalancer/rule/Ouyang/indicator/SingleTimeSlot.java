package org.sysu.nameservice.loadbalancer.rule.Ouyang.indicator;

import org.sysu.nameservice.loadbalancer.monitor.BasicCounter;
import org.sysu.nameservice.loadbalancer.monitor.Monitor;
import org.sysu.nameservice.loadbalancer.monitor.Monitors;
import org.sysu.nameservice.loadbalancer.monitor.Slot;
import org.sysu.nameservice.loadbalancer.stats.distribution.Distribution;

/**
 * busyness indicator算法的时间槽实现
 */
public class SingleTimeSlot implements Slot {
    private static final long defaultInterval = 30000;
    private long interval; //单位为ms;
    private long starTime = getNowTime();

    /** 指标记录 */
    /** the number of requests processed per second*/
    BasicCounter requestNumber;

    /** the average processing time per processed request (in milliseconds)*/
    private Distribution processTimeDist;;

    /**the number of work item starts and completions per second*/
    /** 计算start和completeion的workitem的和 */
    private BasicCounter workItems;

    /** the number of worker threads currently executing in the engine’s container */
    /** 是一个类变量 */
    private static BasicCounter executingThreads;

    /**
     *
     * @param interval 单位是ms
     */
    public SingleTimeSlot(long interval) {
        this.interval = interval;
        this.requestNumber = Monitors.newBasicCounter("the number of requests processed per second*");
        this.processTimeDist = new Distribution();
        this.workItems = Monitors.newBasicCounter("the number of work item starts and completions per second");
        this.executingThreads = Monitors.newBasicCounter("the number of worker threads currently executing in the engine’s container");
    }

    /**
     * 这里是以30s为一个单位，也就是说统计是统计每30s里面的，然后计算每秒的请求数时，就是总数量去除以30就可以了；
     */
    public SingleTimeSlot() {
        this(defaultInterval);
    }

    public boolean isCurrentSlot() {
        synchronized (this) {
            long now = getNowTime();
            if(now < starTime + interval) {
                return true;
            } else {
                return false;
            }
        }
    }

    public long getStarTime() {
        return this.starTime;
    }

    /** 在响应时进行记录 */
    /** ms : 响应时间，单位是ms*/
    public void noteRequestCompletion(double rs) {
        //现在的处理策略是一个请求对应一个task，每个task对应一个工作项的概念；但是往往一个请求可能不只会对应一个task，而是会引起多个task的执行；这种情况下的workItem的统计可能就需要在收到响应的时候由服务器发送消息回来统计了；
        this.requestNumber.increment();
        this.processTimeDist.noteValue(rs);
        this.workItems.increment();
        SingleTimeSlot.executingThreads.decrement();
    }

    /**
     * 在请求失败时记录
     */
    public void noteReqeustFail() {
        SingleTimeSlot.executingThreads.decrement();
    }

    /** 在发起请求时进行记录*/
    public void noteRequestStart() {
        this.workItems.increment();
        SingleTimeSlot.executingThreads.increment();
    }

    /**
     * 计算每秒的请求数量
     * @return
     */
    public long getRequestNumber() {
        Long number = (Long) this.requestNumber.getValue();
        return number.longValue() / interval;
    }

    /**
     * 获取每个请求的处理时间，单位是ms
     * @return
     */
    public long getProcessTimeAvg() {
        return new Double(this.processTimeDist.getMean()).longValue();
    }

    /**
     * 获取每秒钟开始和完成的工作项的数目
     * @return
     */
    public long getWorkItems() {
        Long number = (Long) this.workItems.getValue();
        return number.longValue() / interval;
    }

    /**
     * 计算当时正在执行的请求数；其实在论文中是worker threads，但是这个是什么没有搞懂
     * @return
     */
    public long getExecutingThreads() {
        Long number = (Long) SingleTimeSlot.executingThreads.getValue();
        return number.longValue();
    }


    @Override
    public long getInterval() {
        return interval;
    }

    private static long getNowTime() {
        return System.currentTimeMillis();
    }
}
