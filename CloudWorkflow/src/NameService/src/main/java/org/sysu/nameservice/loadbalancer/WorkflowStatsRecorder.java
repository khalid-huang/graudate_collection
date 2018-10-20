package org.sysu.nameservice.loadbalancer;

import org.sysu.nameservice.loadbalancer.monitor.StopWatch;
import org.sysu.nameservice.loadbalancer.stats.IServerStats;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WorkflowStatsRecorder {
    private WorkflowBalancerContext context;
    private IServerStats serverStats;
    private StopWatch trace;

    public WorkflowStatsRecorder(WorkflowBalancerContext context, Server server) {
        this.context = context;
        if(server != null) {
            serverStats = context.getServerStats(server);
            //跟踪请求时间
            trace = context.getExecuteTracer().start();
        }
    }

    public void recordRequestStartStarts(Map<String, Object> data) {
        serverStats.noteRequestStart(data);
    }

    public void recordRequestCompelteStats(Map<String, Object> data) {
        if(this.trace != null && this.serverStats!= null) {
            this.trace.stop();
            /**获取执行结果*/
            String status = (String) data.get("status");
            long duration = this.trace.getDuration(TimeUnit.MILLISECONDS);
            data.put("responseTime", String.valueOf(duration));
            if(status.equals("fail")) {
                this.context.noteRequestFail(this.serverStats, data);
            } else {
                this.context.noteRequestCompletion(this.serverStats, data);
            }
        }
    }
}
