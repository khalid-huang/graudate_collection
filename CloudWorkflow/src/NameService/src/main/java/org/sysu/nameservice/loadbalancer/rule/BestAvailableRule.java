package org.sysu.nameservice.loadbalancer.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sysu.nameservice.loadbalancer.AbstractLoadBalancer;
import org.sysu.nameservice.loadbalancer.ILoadBalancer;
import org.sysu.nameservice.loadbalancer.LoadBalancerStats;
import org.sysu.nameservice.loadbalancer.Server;
import org.sysu.nameservice.loadbalancer.stats.BaseServerStats;

import java.util.List;

/**
 * @author: Gordan Lin
 * @create: 2018/11/26
 **/
public class BestAvailableRule extends AbstractLoadBalancerRule {

    private static Logger logger = LoggerFactory.getLogger(BestAvailableRule.class);

    public BestAvailableRule() {}

    public BestAvailableRule(ILoadBalancer lb) {
        setLoadBalancer(lb);
    }

    @Override
    public Server choose(Object key) {
        return choose(getLoadBalancer(), key);
    }

    private Server choose(ILoadBalancer lb, Object key) {
        if(lb == null) {
            logger.warn("no load balancer");
            return null;
        }
        List<Server> serverList = lb.getReachableServers();
        LoadBalancerStats loadBalancerStats = ((AbstractLoadBalancer)lb).getLoadBalancerStats();
        int minimalConcurrentConnections = Integer.MAX_VALUE;
        Server server = null;
        for (Server s : serverList) {
            BaseServerStats serverStats = (BaseServerStats)loadBalancerStats.getSingleServerStat(s);
            int concurrentConnections = serverStats.getActiveRequestsCount();
            logger.info("服务器连接数量：{}",concurrentConnections);
            if (concurrentConnections < minimalConcurrentConnections) {
                minimalConcurrentConnections = concurrentConnections;
                server = s;
            }
        }
        System.out.println("BestAvailableRule............");
        System.out.println(server.toString());
        return server;
    }

    @Override
    public String getStatsClassName() {
        return "BaseServerStats";
    }
}
