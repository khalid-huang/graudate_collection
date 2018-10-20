package org.sysu.nameservice.loadbalancer.stats;

import com.alibaba.fastjson.JSON;
import org.sysu.nameservice.GlobalContext;
import org.sysu.nameservice.loadbalancer.Server;
import org.sysu.nameservice.loadbalancer.WorkflowLoadBalancerRequest;
import org.sysu.nameservice.loadbalancer.rule.Ouyang.OuYangContext;
import org.sysu.nameservice.loadbalancer.rule.Ouyang.indicator.MultiplePastTimeSlot;

import java.util.HashMap;
import java.util.Map;

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
    public void noteRequestStart(Map<String,Object> data) {
        Map<String, String> recordData = new HashMap<>();
        timeSlots.noteRequestStart(recordData);
    }

    /**
     * 需要处理记录响应时间，记录是否是新的工作项的发起（claim）
     * @param data 这里是最原始的数据，主要是request和response
     */
    @Override
    @SuppressWarnings("unchecked")
    public void noteRequestCompletion(Map<String, Object> data) {
        /** 这里需要做数据包装,将对象全部扁平化 */
        Map<String, String> recordData = new HashMap<>();
        //获取操作类型，同时进行数据转换
        WorkflowLoadBalancerRequest request = (WorkflowLoadBalancerRequest) data.get("request");
        Map<String, String> response = JSON.parseObject((String) data.get("response"), Map.class);
        String action = request.getAction();

        /** 填充数据 */
        recordData.put("responseTime", (String)data.get("responseTime"));
        recordData.put("action", action);
        if(action.equals(GlobalContext.ACTION_ACTIVITISERVICE_COMPLETETASK)) {
            recordData.put("newWorkItem", response.get("newWorkItem"));
        }
        timeSlots.noteRequestCompletion(recordData);
    }

    @Override
    public void noteRequestFail(Map<String, Object> data) {
        Map<String, String> recordData = new HashMap<>();

        timeSlots.noteRequestFail(recordData);
    }

    @Override
    public void initialize(Server server) {
        this.server = server;
    }



    /** 只有一个时间槽 */
    public int getBusynessForLevelOne() {
        return timeSlots.calculateBusynessForLevelOne();
    }

    /** 多时间槽，其大小由OuYangContext指定; */
    public int getBusynessForLevelTwo() {
        return timeSlots.calculateBusynessForLevelTwo();
    }

    /** 表示获取 多少ms之前到目前的数据的均值*/
    /** pastTime 单位ms: 比如5分钟之前就是5 * 60000 */
    public int getBusynessForLevelTwoWithLimitTime(long pastTime) {
        return timeSlots.calculateBusynessForLevelTwoWithLimitTime(pastTime);
    }

    public int getBusynessForLevelThree() {
        return 0;
    }

    @Override
    public String toString() {
        return "BusynessIndicatorServerStats{" +
                "server=" + server +
                ", multiplePastTimeSlotSize=" + multiplePastTimeSlotSize +
                ", singleTimeSlotInterval=" + singleTimeSlotInterval +
                ", timeSlots=" + timeSlots +
                '}';
    }
}
