package org.sysu.nameservice.loadbalancer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sysu.nameservice.loadbalancer.monitor.Monitors;
import org.sysu.nameservice.loadbalancer.monitor.Timer;

import java.util.concurrent.TimeUnit;

/**
 * A class contains APIs intened to be used be load balancing client which is subclass of this class
 */
public class LoadBalancerContext {
    private static final Logger logger = LoggerFactory.getLogger(LoadBalancerContext.class);

    protected String clientName = "default";

    private ILoadBalancer lb;
    private volatile Timer tracer;

    public LoadBalancerContext(ILoadBalancer lb) {
        this.lb = lb;
    }

    public Timer getExecuteTracer() {
        if(tracer == null) {
            synchronized (this) {
                if(tracer == null) {
                    tracer = Monitors.newTimer(clientName + "_LoadBalancerExecutionTimer", TimeUnit.MICROSECONDS);
                }
            }
        }
        return tracer;
    }

    public String getClientName() {
        return clientName;
    }

    public ILoadBalancer getLoadBalancer() {
        return lb;
    }

    public void setLoadBalancer(ILoadBalancer lb) {
        this.lb = lb;
    }

    private void recordStats(ServerStats serverStats, long responseTime) {
        if(serverStats == null) {
            return;
        }
        serverStats.decrementActiveRequestCount();
        serverStats.incrementNumRequests();
        serverStats.noteResponseTime(responseTime);
    }

    //可能需要重新发起请求什么的
    public void noteRequestCompletion(ServerStats serverStats, Object response, long responseTime) {
        if(serverStats == null) {
            return;
        }
        recordStats(serverStats, responseTime);
    }

    /** 获取与服务相关的服务stat*/
    public final ServerStats getServerStats(Server server) {
        ServerStats serverStats = null;
        ILoadBalancer lb = this.getLoadBalancer();
        if(lb instanceof AbstractLoadBalancer) {
            LoadBalancerStats lbStats = ((AbstractLoadBalancer) lb).getLoadBalancerStats();
            serverStats = lbStats.getSingleServerStat(server);
        }
        return serverStats;
    }

}
