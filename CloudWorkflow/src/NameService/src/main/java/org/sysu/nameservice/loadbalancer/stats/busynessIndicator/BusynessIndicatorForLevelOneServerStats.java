package org.sysu.nameservice.loadbalancer.stats.busynessIndicator;

import org.sysu.nameservice.loadbalancer.Server;
import org.sysu.nameservice.loadbalancer.rule.Ouyang.OuYangContext;
import org.sysu.nameservice.loadbalancer.rule.Ouyang.indicator.MultiplePastTimeSlot;
import org.sysu.nameservice.loadbalancer.stats.IServerStats;

public class BusynessIndicatorForLevelOneServerStats extends AbstractBusynessIndicatorServerStats implements IServerStats {

    public BusynessIndicatorForLevelOneServerStats() {
        this(OuYangContext.levelOneMultiplePastTimeSlotSize, OuYangContext.levelOneSingleTimeSlotInterval);
    }

    public BusynessIndicatorForLevelOneServerStats(int multiplePastTimeSlotSize, long singleTimeSlotInterval) {
        this.singleTimeSlotInterval = singleTimeSlotInterval;
        this.multiplePastTimeSlotSize = multiplePastTimeSlotSize;
    }

    /** 只有一个时间槽 */
    public int getBusyness() {
        return timeSlots.calculateBusynessForLevelOne();
    }

    @Override
    public void initialize(Server server) {
        //timeSlots需要这里做，不然没有Server信息
        timeSlots = new MultiplePastTimeSlot(this.multiplePastTimeSlotSize, this.singleTimeSlotInterval, "LevelOne-" + server.getId().replace(':', '-'));

        super.initialize(server);
    }

    @Override
    public String toString() {
        return "BusynessIndicatorForLevelOneServerStats{" +
                "server=" + server +
                ", multiplePastTimeSlotSize=" + multiplePastTimeSlotSize +
                ", singleTimeSlotInterval=" + singleTimeSlotInterval +
                ", timeSlots=" + timeSlots +
                '}';
    }

}
