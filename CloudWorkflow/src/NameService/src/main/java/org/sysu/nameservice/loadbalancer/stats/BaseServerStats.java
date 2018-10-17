package org.sysu.nameservice.loadbalancer.stats;

import org.sysu.nameservice.loadbalancer.LoadBalancerStats;
import org.sysu.nameservice.loadbalancer.Server;
import org.sysu.nameservice.loadbalancer.stats.IServerStats;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Capture various stats per Server(node) in the LoadBalancer
 */
public class BaseServerStats implements IServerStats {
    Server server;

    //关于响应时间在ribbon中的设置方式是使用了用分布来取代响应时间的设置

    //正在执行的请求
    AtomicInteger activeRequestCount = new AtomicInteger(0);

    AtomicLong totalRequests = new AtomicLong(0L);


    public BaseServerStats() {

    }

    public void initialize(Server server) {
        this.server = server;
    }

    public void incrementNumRequests() {
        totalRequests.incrementAndGet();
    }

    public void incrementActiveRequestsCount() {
        activeRequestCount.incrementAndGet();
    }

    public void decrementActiveRequestCount() {
        if(activeRequestCount.decrementAndGet() < 0) {
            activeRequestCount.set(0);
        }
    }

    public int getActiveRequestsCount() {
        int count = activeRequestCount.get();
        return count;
    }

    public void noteResponseTime(double msecs) {
        //
    }

    @Override
    public void noteRequestCompletion(Map<String, Object> data) {
        long duration = (long) data.get("duration");
        decrementActiveRequestCount();
        incrementNumRequests();
        noteResponseTime(duration);
    }

    @Override
    public void noteRequestFail(Map<String, Object> data) {
        decrementActiveRequestCount();
    }

    @Override
    public void noteRequestStart(Map<String, Object> data) {
        incrementActiveRequestsCount();
    }

    @Override
    public String toString() {
        return "BaseServerStats{" +
                "server=" + server +
                ", activeRequestCount=" + activeRequestCount +
                ", totalRequests=" + totalRequests +
                '}';
    }
}
