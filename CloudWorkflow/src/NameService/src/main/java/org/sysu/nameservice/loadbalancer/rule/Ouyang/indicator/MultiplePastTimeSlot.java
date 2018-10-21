package org.sysu.nameservice.loadbalancer.rule.Ouyang.indicator;

import org.sysu.nameservice.GlobalContext;
import org.sysu.nameservice.loadbalancer.rule.Ouyang.OuYangContext;
import org.sysu.nameservice.loadbalancer.rule.Ouyang.help.FixedSafeDeque;
import org.sysu.nameservice.loadbalancer.rule.Ouyang.help.HelpLevel;
import org.sysu.nameservice.loadbalancer.rule.Ouyang.help.TripleValue;

import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 多时间槽的实现；也就是时间槽分片，每一片就是一个single时间槽；主要用于统计过去的信息
 * 这个时间槽的实现的当前时间就是最后一个分片的时间
 * 在记录busynessIndicator方面，用了定时器，每3秒去记录上一个时间槽的busynessIndicator，同时生成一个新的时间槽
 */
public class MultiplePastTimeSlot {
    /** 每个分片的时间长度，单位为ms；这里是3s*/
    private static long defaultSingleInterval = 3 * 1000;

    /** 表示整体的时间长度；这里是100个，也就是表示可以缓存40 * 3s也就是2分钟的数据 */
    private static final int defaultSize = 40;

    private String name;

    private long singleInterval;

    private long interval;
    /** 表示时间槽的个数 */
    private int size;
    /** 表示具体的时间槽实体*/
    FixedSafeDeque<SingleTimeSlot> timeSlots = null;

    protected AtomicLong counterForSlots = null; //用于统计时间槽个数
    protected Timer timeSlotsManagerTimer = null; //用于定时计算和增加时间槽的定时类
    PrintWriter writer = null;
    PrintWriter writerSlot = null;

    /**
     *
     * @param size
     * @param singleInterval
     * @param name 一般是传入对应的server的serverId; localhost:8080
     */
    public MultiplePastTimeSlot(int size, long singleInterval, String name) {
        this.size = size;
        this.singleInterval = singleInterval;
        this.interval = this.singleInterval * this.size;
        this.name = name;
        timeSlots = new FixedSafeDeque<>(size);
        /**
         * 设置定时任务，每defaultSingleInterval秒去生成一个添加一个新的时间槽，并计算过去的时间槽的busynessIndicator，写入文件中
         */
        timeSlotsManagerTimer = new Timer("MultiplePastTimeSlot-timeSlotsManagerTimer-" + this.name, true);
        timeSlotsManagerTimer.schedule(new DynamicTimeSlotsManagerTask(), 0, singleInterval);
        counterForSlots = new AtomicLong(0L);
        try {
            writer = new PrintWriter(GlobalContext.ACTIVITISERVICE_BUSYNESS_DIRECTORY +  "\\busynessIndicator-" + this.name + "-" + System.currentTimeMillis() + ".txt", "UTF-8");
            writerSlot = new PrintWriter(GlobalContext.ACTIVITISERVICE_BUSYNESS_DIRECTORY +  "\\singleSlot-" + this.name + "-" + System.currentTimeMillis() + ".txt", "UTF-8");
        } catch (Exception e) {

        }
    }

    /**
     * 在响应时进行记录，
     * @param data
     */
    public void noteRequestCompletion(Map<String, String> data) {
        SingleTimeSlot current = getCurrentSlot();
        current.noteRequestCompletion(data);
    }

    public void noteRequestFail(Map<String, String> data) {
        SingleTimeSlot current = getCurrentSlot();
        current.noteReqeustFail(data);
    }

    /**
     * 在发起请求时进行记录
     */
    public void noteRequestStart(Map<String, String> data) {
        SingleTimeSlot current = getCurrentSlot();
        current.noteRequestStart(data);
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
        return timeSlots.getLast();
//        synchronized (this) {
//            if(timeSlots.isEmpty()) {
//                timeSlots.add(new SingleTimeSlot(singleInterval));
//            }
//
//            if(!timeSlots.getLast().isCurrentSlot()) {
//                passSlot();
//            }
//
//            return timeSlots.getLast();
//        }
    }

    private int calculateBusynessFromSingleTimeSlot(SingleTimeSlot timeSlot) {
        TripleValue requestNum = new TripleValue(timeSlot.getRequestNumber(), OuYangContext.levelOneRequestNumberLimit, OuYangContext.levelOneRequestNumberWeight);
        TripleValue workItems = new TripleValue(timeSlot.getWorkItems(), OuYangContext.levelOneWorkItemLimit,OuYangContext.levelOneWorkItemWeight);
        TripleValue processTime = new TripleValue(timeSlot.getProcessTimeAvg(), OuYangContext.levelOneAverageProcessTimeLimit, OuYangContext.levelOneAverageProcessTimeWeight);
        TripleValue executingThreads = new TripleValue(timeSlot.getExecutingThreads(), OuYangContext.levelOneExecutingThreadsLimit, OuYangContext.levelOneExecutingThreadsWeight);

        return HelpLevel.calculateBusyness(requestNum, processTime, workItems, executingThreads);
    }

    /** 单时间槽的，直接计算 */
    public int calculateBusynessForLevelOne() {
        Deque<SingleTimeSlot> singleTimeSlots = timeSlots.getContainer();
        return calculateBusynessFromSingleTimeSlot(singleTimeSlots.getFirst());
    }

    /**
     * 根据当前的时间槽计算得到level Two busyness; 超参数由Context提供
     * @return
     */
    public int calculateBusynessForLevelTwo() {
        Deque<SingleTimeSlot> singleTimeSlots = timeSlots.getContainer();
        double sum = 0.0;
        int i = 0;
        for(SingleTimeSlot timeSlot : singleTimeSlots) {
            if(i == 0) {
                sum = calculateBusynessFromSingleTimeSlot(timeSlot);
            } else {
                /** 对于level two 的平滑*/
                sum = OuYangContext.levelTwoAlpha * calculateBusynessFromSingleTimeSlot(timeSlot) + (1-OuYangContext.levelTwoAlpha) * sum;
            }
            ++i;
        }
        return (int)sum;
    }

    /** 表示获取 多少ms之前到目前的数据的均值*/
    /** pastTime 单位ms: 比如5分钟之前就是5 * 60000 */
    public int calculateBusynessForLevelTwoWithLimitTime(long pastTime) {
        long pastLimit = System.currentTimeMillis() - pastTime;
        Deque<SingleTimeSlot> singleTimeSlots = timeSlots.getContainer();
        double sum = 0.0;
        int i = 0;
        for(SingleTimeSlot timeSlot : singleTimeSlots) {
            if(pastLimit < timeSlot.getStarTime()) {
                break;
            }
            if(i == 0) {
                sum = calculateBusynessFromSingleTimeSlot(timeSlot);
            } else {
                /** 对于level two 的平滑*/
                sum = OuYangContext.levelTwoAlpha * calculateBusynessFromSingleTimeSlot(timeSlot) + (1-OuYangContext.levelTwoAlpha) * sum;
            }
            ++i;
        }
        return (int)sum;
    }

    @Override
    public String toString() {
        return "MultiplePastTimeSlot{" +
                "singleInterval=" + singleInterval +
                ", interval=" + interval +
                ", size=" + size +
                ", timeSlots=" + timeSlots +
                '}';
    }

    class DynamicTimeSlotsManagerTask extends TimerTask {
        public void run() {
            /** 计算busynessIndicator,并写入文件 */
            if(timeSlots.size() != 0) {
                int busyness = calculateBusynessForLevelTwo();
                writer.println(String.valueOf(counterForSlots.getAndIncrement()) + " " + String.valueOf(busyness));
                writer.flush();
                writerSlot.println(getCurrentSlot().toString());
                writerSlot.flush();
                }
            /** 增加新的timeSlot */
            timeSlots.add(new SingleTimeSlot(singleInterval));
        }
    }
}
