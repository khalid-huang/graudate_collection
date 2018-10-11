package org.sysu.nameservice.loadbalancer;

import org.sysu.nameservice.loadbalancer.monitor.StopWatch;

import java.util.concurrent.TimeUnit;

public class WorkflowStatsRecorder {
    private WorkflowBalancerContext context;
    private ServerStats serverStats;
    private StopWatch trace;

    public WorkflowStatsRecorder(WorkflowBalancerContext context, Server server) {
        this.context = context;
        if(server != null) {
            serverStats = context.getServerStats(server);
            trace = context.getExecuteTracer().start();
        }
    }

    public void recordStats(Object entity) {
        if(this.trace != null && this.serverStats != null) {
            this.trace.stop();
            long duration = this.trace.getDuration(TimeUnit.MILLISECONDS);
            this.context.noteRequestCompletion(serverStats,entity, duration);
        }
    }
}
