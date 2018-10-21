package org.sysu.nameservice.loadbalancer.stats.busynessIndicator;

import org.sysu.nameservice.loadbalancer.Server;
import org.sysu.nameservice.loadbalancer.rule.Ouyang.OuYangContext;
import org.sysu.nameservice.loadbalancer.rule.Ouyang.indicator.MultiplePastTimeSlot;
import org.sysu.nameservice.loadbalancer.stats.busynessIndicator.AbstractBusynessIndicatorServerStats;

/**
 * 用于复现busyness indicator 的serverStats
 */
public class BusynessIndicatorForLevelTwoServerStats extends AbstractBusynessIndicatorServerStats {

    public BusynessIndicatorForLevelTwoServerStats() {
        this(OuYangContext.levelTwoMultiplePastTimeSlotSize, OuYangContext.levelTwoSingleTimeSlotInterval);
    }

    public BusynessIndicatorForLevelTwoServerStats(int multiplePastTimeSlotSize, long singleTimeSlotInterval) {
        this.singleTimeSlotInterval = singleTimeSlotInterval;
        this.multiplePastTimeSlotSize = multiplePastTimeSlotSize;
    }


    /** 多时间槽，其大小由OuYangContext指定; */
    public int getBusyness() {
        return timeSlots.calculateBusynessForLevelTwo();
    }

    /** 表示获取 多少ms之前到目前的数据的均值*/
    /** pastTime 单位ms: 比如5分钟之前就是5 * 60000 */
    public int getBusynessWithLimitTime(long pastTime) {
        return timeSlots.calculateBusynessForLevelTwoWithLimitTime(pastTime);
    }

    @Override
    public void initialize(Server server) {
        timeSlots = new MultiplePastTimeSlot(this.multiplePastTimeSlotSize, this.singleTimeSlotInterval, "LevelTwo-" + server.getId().replace(':', '-'));
        super.initialize(server);
    }

    @Override
    public String toString() {
        return "BusynessIndicatorForLevelTwoServerStats{" +
                "server=" + server +
                ", multiplePastTimeSlotSize=" + multiplePastTimeSlotSize +
                ", singleTimeSlotInterval=" + singleTimeSlotInterval +
                ", timeSlots=" + timeSlots +
                '}';
    }
}
