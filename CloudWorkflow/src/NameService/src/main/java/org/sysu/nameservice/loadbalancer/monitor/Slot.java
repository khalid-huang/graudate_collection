package org.sysu.nameservice.loadbalancer.monitor;

/**
 * 表示时间槽
 */
public interface Slot {
    /**
     * 获取时间槽的时长
     * @return
     */
    public long getInterval();
}
