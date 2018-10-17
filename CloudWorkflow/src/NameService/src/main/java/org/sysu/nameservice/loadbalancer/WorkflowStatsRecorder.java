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
            trace = context.getExecuteTracer().start();
        }
    }

    public void recordStats(Map<String, Object> data) {
        if(this.trace != null && this.serverStats!= null) {
            this.trace.stop();
            /**获取执行结果*/
            String status = data.get("status");
            if(status.equals("fail")) {
                this.context.();
            }
            long duration = this.trace.getDuration(TimeUnit.MILLISECONDS);
            data.put("duration", duration);
            this.context.noteRequestCompletion(serverStats, data);
        }
    }
}
