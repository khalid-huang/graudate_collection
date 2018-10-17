package org.sysu.nameservice.loadbalancer.stats;

import org.sysu.nameservice.loadbalancer.Server;
import org.sysu.nameservice.loadbalancer.rule.Ouyang.OuYangContext;
import org.sysu.nameservice.loadbalancer.rule.Ouyang.help.HelpLevel;
import org.sysu.nameservice.loadbalancer.rule.Ouyang.help.TripleValue;
import org.sysu.nameservice.loadbalancer.rule.Ouyang.indicator.MultiplePastTimeSlot;
import org.sysu.nameservice.loadbalancer.rule.Ouyang.indicator.SingleTimeSlot;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用于复现busyness indicator 的serverStats
 */
public class BusynessIndicatorServerStats implements IServerStats {
    Server server;

    private int multiplePastTimeSlotSize;
    private long singleTimeSlotInterval;

    MultiplePastTimeSlot timeSlots;

    public BusynessIndicatorServerStats() {
        this(OuYangContext.levelOneMultiplePastTimeSlotSize, OuYangContext.levelOneSingleTimeSlotInterval);
    }

    public BusynessIndicatorServerStats(int multiplePastTimeSlotSize, long singleTimeSlotInterval) {
        this.singleTimeSlotInterval = singleTimeSlotInterval;
        this.multiplePastTimeSlotSize = multiplePastTimeSlotSize;
        timeSlots = new MultiplePastTimeSlot(this.multiplePastTimeSlotSize, this.singleTimeSlotInterval);
    }

    @Override
    public void noteRequestStart(Map<String, Object> data) {
        timeSlots.noteRequestStart();
    }

    @Override
    public void noteRequestCompletion(Map<String, Object> data) {
        double rs = (double) data.get("responseTime");
        timeSlots.noteRequestCompletion(rs);
    }

    @Override
    public void noteRequestFail(Map<String, Object> data) {
        timeSlots.noteRequestFail();
    }

    @Override
    public void initialize(Server server) {
        this.server = server;
    }



    /** 只有一个时间槽 */
    public int getBusynessForLevelOne() {
        return timeSlots.calculateBusyness();
    }

    /** 多时间槽，其大小由OuYangContext指定; 需不需要传入一个时间呢，表示多久之前*/
    public int getBusynessForLevelTwo() {
        return timeSlots.calculateBusyness();
    }

    /** 表示获取 多少ms之前到目前的数据的均值*/
    /** pastTime 单位ms: 比如5分钟之前就是5 * 60000 */
    public int getBusynessForLevelTwoWithLimitTime(long pastTime) {
        return timeSlots.calculateBusynessWithLimitTime(pastTime);
    }

    public int getBusynessForLevelThree() {
        return 0;
    }
}
