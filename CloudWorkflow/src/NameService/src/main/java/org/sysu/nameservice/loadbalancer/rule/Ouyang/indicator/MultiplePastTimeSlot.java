package org.sysu.nameservice.loadbalancer.rule.Ouyang.indicator;

import org.sysu.nameservice.loadbalancer.rule.Ouyang.OuYangContext;
import org.sysu.nameservice.loadbalancer.rule.Ouyang.help.FixedSafeDeque;
import org.sysu.nameservice.loadbalancer.rule.Ouyang.help.HelpLevel;
import org.sysu.nameservice.loadbalancer.rule.Ouyang.help.TripleValue;

import java.util.Deque;
import java.util.LinkedList;

/**
 * 多时间槽的实现；也就是时间槽分片，每一片就是一个single时间槽；主要用于统计过去的信息
 * 这个时间槽的实现的当前时间就是最后一个分片的时间
 */
public class MultiplePastTimeSlot {
    /** 每个分片的时间长度，单位为ms；这里是30s*/
    private static long defaultSingleInterval = 30000;

    /** 表示整体的时间长度；这里是10个，也就是表示可以缓存10 * 30s也就是5分钟的数据 */
    private static final int defaultSize = 10;

    private long singleInterval;

    private long interval;
    /** 表示时间槽的个数 */
    private int size;
    /** 表示具体的时间槽实体*/
    FixedSafeDeque<SingleTimeSlot> timeSlots;

    public MultiplePastTimeSlot(int size, long singleInterval) {
        this.size = size;
        this.singleInterval = singleInterval;
        this.interval = this.singleInterval * this.size;
        timeSlots = new FixedSafeDeque<>(size);
    }

    public MultiplePastTimeSlot() {
        this(defaultSize, defaultSingleInterval);
    }

    /**
     * 在响应时进行记录，
     * @param rs 响应时间，单位是ms
     */
    public void noteRequestCompletion(double rs) {
        SingleTimeSlot current = getCurrentSlot();
        current.noteRequestCompletion(rs);
    }

    public void noteRequestFail() {
        SingleTimeSlot current = getCurrentSlot();
        current.noteReqeustFail();
    }

    /**
     * 在发起请求时进行记录
     */
    public void noteRequestStart() {
        SingleTimeSlot current = getCurrentSlot();
        current.noteRequestStart();
    }

    /**
     * 表示度过一个时间槽
     */
    private void passSlot() {
        SingleTimeSlot newSlot = new SingleTimeSlot(singleInterval);
        //下面的函数返回了需要被放掉的时间槽；如果数据持久化，需要在这里做
        timeSlots.add(newSlot);
    }

    /**
     * 获取当前的时间槽
     */
    public SingleTimeSlot getCurrentSlot() {
        synchronized (this) {
            if(timeSlots.isEmpty()) {
                timeSlots.add(new SingleTimeSlot(singleInterval));
            }

            if(!timeSlots.getLast().isCurrentSlot()) {
                passSlot();
            }

            return timeSlots.getLast();
        }
    }

    private int calculateBusynessFromSingleTimeSlot(SingleTimeSlot timeSlot) {
        TripleValue requestNum = new TripleValue(timeSlot.getRequestNumber(), OuYangContext.levelOneRequestNumberLimit, OuYangContext.levelOneRequestNumberWeight);
        TripleValue workItems = new TripleValue(timeSlot.getWorkItems(), OuYangContext.levelOneWorkItemLimit,OuYangContext.levelOneWorkItemWeight);
        TripleValue processTime = new TripleValue(timeSlot.getProcessTimeAvg(), OuYangContext.levelOneAverageProcessTimeLimit, OuYangContext.levelOneAverageProcessTimeWeight);
        TripleValue executingThreads = new TripleValue(timeSlot.getExecutingThreads(), OuYangContext.levelOneExecutingThreadsLimit, OuYangContext.levelOneExecutingThreadsWeight);
        return HelpLevel.calculateBusyness(requestNum, processTime, workItems, executingThreads);
    }

    /**
     * 根据当前的时间槽计算得到busyness
     * @return
     */
    public int calculateBusyness() {
        Deque<SingleTimeSlot> singleTimeSlots = timeSlots.getContainer();
        int size = singleTimeSlots.size();
        double sum = 0.0;
        for(SingleTimeSlot timeSlot : singleTimeSlots) {
            sum += calculateBusynessFromSingleTimeSlot(timeSlot);
        }
        return (int)(sum / size);
    }

    /** 表示获取 多少ms之前到目前的数据的均值*/
    /** pastTime 单位ms: 比如5分钟之前就是5 * 60000 */
    public int calculateBusynessWithLimitTime(long pastTime) {
        long pastLimit = System.currentTimeMillis() - pastTime;
        Deque<SingleTimeSlot> singleTimeSlots = timeSlots.getContainer();
        int size = singleTimeSlots.size();
        double sum = 0.0;
        for(SingleTimeSlot timeSlot : singleTimeSlots) {
            if(pastLimit < timeSlot.getStarTime()) {
                break;
            }
            sum += calculateBusynessFromSingleTimeSlot(timeSlot);
        }
        return (int)(sum / size);
    }
}
