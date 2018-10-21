package org.sysu.nameservice.loadbalancer.stats.busynessIndicator;

import com.alibaba.fastjson.JSON;
import org.sysu.nameservice.GlobalContext;
import org.sysu.nameservice.loadbalancer.Server;
import org.sysu.nameservice.loadbalancer.WorkflowLoadBalancerRequest;
import org.sysu.nameservice.loadbalancer.rule.Ouyang.indicator.MultiplePastTimeSlot;
import org.sysu.nameservice.loadbalancer.stats.IServerStats;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractBusynessIndicatorServerStats implements IServerStats {
    Server server;

    protected int multiplePastTimeSlotSize;
    protected long singleTimeSlotInterval;

    MultiplePastTimeSlot timeSlots;

    @Override
    public void noteRequestStart(Map<String, Object> data) {
        Map<String, String> recordData = new HashMap<>();
        timeSlots.noteRequestStart(recordData);
    }

    /**
     * 需要处理记录响应时间，记录是否是新的工作项的发起（claim）
     *
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
        recordData.put("responseTime", (String) data.get("responseTime"));
        recordData.put("action", action);
        if (action.equals(GlobalContext.ACTION_ACTIVITISERVICE_COMPLETETASK)) {
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
}
