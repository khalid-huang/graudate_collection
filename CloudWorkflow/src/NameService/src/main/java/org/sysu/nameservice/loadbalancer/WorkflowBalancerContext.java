package org.sysu.nameservice.loadbalancer;

import org.sysu.nameservice.loadbalancer.stats.IServerStats;

import java.util.Map;

/** 在workflow的环境下BalancerContext的执行环境 */
public class WorkflowBalancerContext extends LoadBalancerContext {
    public WorkflowBalancerContext(ILoadBalancer lb) {
        super(lb);
    }

    @Override
    public void noteRequestCompletion(IServerStats serverStats, Map<String, Object> data) {
        super.noteRequestCompletion(serverStats, data);
    }

    @Override
    public void noteRequestStart(IServerStats serverStats, Map<String, Object> data) {
        super.noteRequestStart(serverStats, data);
    }

    @Override
    public void noteRequestFail(IServerStats serverStats, Map<String, Object> data) {
        super.noteRequestFail(serverStats, data);
    }
}
