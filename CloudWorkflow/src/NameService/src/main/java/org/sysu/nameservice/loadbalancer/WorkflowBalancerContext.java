package org.sysu.nameservice.loadbalancer;

public class WorkflowBalancerContext extends LoadBalancerContext {
    public WorkflowBalancerContext(ILoadBalancer lb) {
        super(lb);
    }

    @Override
    public void noteRequestCompletion(ServerStats serverStats, Object response, long responseTime) {
        super.noteRequestCompletion(serverStats, response, responseTime);
    }
}
