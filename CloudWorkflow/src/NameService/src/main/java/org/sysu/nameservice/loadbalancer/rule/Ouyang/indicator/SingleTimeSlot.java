package org.sysu.nameservice.loadbalancer.rule.Ouyang.indicator;

import org.sysu.nameservice.GlobalContext;
import org.sysu.nameservice.loadbalancer.monitor.BasicCounter;
import org.sysu.nameservice.loadbalancer.monitor.Monitors;
import org.sysu.nameservice.loadbalancer.monitor.Slot;
import org.sysu.nameservice.loadbalancer.stats.distribution.Distribution;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * busyness indicator算法的时间槽实现
 */
public class SingleTimeSlot implements Slot {
    /** 默认的间隔是三秒 */
    private static final long defaultInterval = 3 * 1000;
    private long interval; //单位为ms;
    private long starTime = getNowTime();

    /** 指标记录 */
    /** the number of requests processed per second*/
    BasicCounter requestNumber;

    /** the average processing time per processed request (in milliseconds)*/
    private Distribution processTimeDist;;

    /**the number of work item starts and completions per second*/
    /** 计算start和completion的workitem的和 */
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
    public void noteRequestCompletion(Map<String,String> data) {
        double rs = Double.parseDouble(data.get("responseTime"));
        String action = data.get("action");

        /** 如果是完成工作项的请求，需要进行工作项相关信息记录*/
        if(action.equals(GlobalContext.ACTION_ACTIVITISERVICE_COMPLETETASK)) {
            int newWorkItem = Integer.parseInt(data.get("newWorkItem"));
            int completeWorkItem = 1;
            this.workItems.increment(newWorkItem + completeWorkItem);
        }
        this.requestNumber.increment();
        this.processTimeDist.noteValue(rs);
        SingleTimeSlot.executingThreads.decrement();
    }

    /**
     * 在请求失败时记录
     */
    public void noteReqeustFail(Map<String, String> data) {
        SingleTimeSlot.executingThreads.decrement();
    }

    /** 在发起请求时进行记录*/
    public void noteRequestStart(Map<String, String> data) {
        this.workItems.increment();
        SingleTimeSlot.executingThreads.increment();
    }

    /**
     * 计算满时间下每秒的请求数量（也就是整个时间槽已经用完）
     * 记信interval是一个ms级别的
     * @return
     */
    public long getRequestNumber() {
        double number = 1.0 * (Long) this.requestNumber.getValue();
        long tempInterval = System.currentTimeMillis() - starTime;

        if(tempInterval > interval) {
            //表示该时间槽已经用完了，是过去的
            return (long)(number / interval * 1000);
        } else {
            //表示是当前时间槽，用目前已经用的时间去除就可以了；
            return (long)(number / tempInterval * 1000);
        }
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
        double number = 1.0 * (Long) this.workItems.getValue();
        long tempInterval = System.currentTimeMillis() - starTime;

        if(tempInterval > interval) {
            //表示该时间槽已经用完了，是过去的
            return (long)(number / interval * 1000);
        } else {
            //表示是当前时间槽，用目前已经用的时间去除就可以了；
            return (long)(number / tempInterval * 1000);
        }
    }

    /**
     * 计算当时正在执行的请求数；其实在论文中是worker threads，但是这个是什么没有搞懂
     * @return
     */
    public long getExecutingThreads() {
        double number = 1.0 * (Long) SingleTimeSlot.executingThreads.getValue();
        long tempInterval = System.currentTimeMillis() - starTime;

        if(tempInterval > interval) {
            //表示该时间槽已经用完了，是过去的
            return (long)(number / interval * 1000);
        } else {
            //表示是当前时间槽，用目前已经用的时间去除就可以了；
            return (long)(number / tempInterval * 1000);
        }
    }


    @Override
    public long getInterval() {
        return interval;
    }

    private static long getNowTime() {
        return System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "SingleTimeSlot{" +
                "requestNumber=" + requestNumber +
                ", processTimeDist=" + processTimeDist +
                ", workItems=" + workItems +
                ", executingThreads=" + SingleTimeSlot.executingThreads +
                '}';
    }
}
