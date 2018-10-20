package org.sysu.nameservice.loadbalancer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sysu.nameservice.loadbalancer.monitor.Monitors;
import org.sysu.nameservice.loadbalancer.monitor.Timer;
import org.sysu.nameservice.loadbalancer.stats.BaseServerStats;
import org.sysu.nameservice.loadbalancer.stats.IServerStats;

import java.util.Map;
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

    private void recordStats(BaseServerStats baseServerStats, long responseTime) {
        if(baseServerStats == null) {
            return;
        }
        baseServerStats.decrementActiveRequestCount();
        baseServerStats.incrementNumRequests();
        baseServerStats.noteResponseTime(responseTime);
    }

    //可能需要重新发起请求什么的
    public void noteRequestCompletion(IServerStats serverStats, Map<String, Object> data) {
        if(serverStats == null) {
            return;
        }
        serverStats.noteRequestCompletion(data);
    }

    public void noteRequestStart(IServerStats serverStats, Map<String,Object> data) {
        if(serverStats == null) {
            return;
        }
        serverStats.noteRequestStart(data);
    }

    public void noteRequestFail(IServerStats serverStats, Map<String, Object> data) {
        if(serverStats == null) {
            return;
        }
        serverStats.noteRequestFail(data);
    }

    /** 获取与服务相关的服务stat*/
    public final IServerStats getServerStats(Server server) {
        IServerStats serverStats = null;
        ILoadBalancer lb = this.getLoadBalancer();
        if(lb instanceof AbstractLoadBalancer) {
            LoadBalancerStats lbStats = ((AbstractLoadBalancer) lb).getLoadBalancerStats();
            serverStats = lbStats.getSingleServerStat(server);
        }
        return serverStats;
    }

}
