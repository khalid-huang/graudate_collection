package org.sysu.nameservice.loadbalancer.stats.busynessIndicator;

import org.sysu.nameservice.loadbalancer.stats.IServerStats;

import java.util.Map;

/**
 * @author: Gordan Lin
 * @create: 2018/11/28
 **/
public class BusynessIndicatorForActivitiServerStats extends BusynessIndicatorForLevelOneServerStats implements IServerStats {

    public BusynessIndicatorForActivitiServerStats() {
        super();
    }

    @Override
    public String toString() {
        return "BusynessIndicatorForActivitiServerStats{" +
                "server=" + server +
                ", multiplePastTimeSlotSize=" + multiplePastTimeSlotSize +
                ", singleTimeSlotInterval=" + singleTimeSlotInterval +
                ", timeSlots=" + timeSlots +
                '}';
    }
}
