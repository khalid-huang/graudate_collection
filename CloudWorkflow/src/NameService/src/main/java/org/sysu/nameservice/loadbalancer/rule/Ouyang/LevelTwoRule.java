package org.sysu.nameservice.loadbalancer.rule.Ouyang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sysu.nameservice.loadbalancer.AbstractLoadBalancer;
import org.sysu.nameservice.loadbalancer.ILoadBalancer;
import org.sysu.nameservice.loadbalancer.LoadBalancerStats;
import org.sysu.nameservice.loadbalancer.Server;
import org.sysu.nameservice.loadbalancer.rule.AbstractLoadBalancerRule;
import org.sysu.nameservice.loadbalancer.stats.busynessIndicator.BusynessIndicatorForLevelTwoServerStats;

import java.util.List;

public class LevelTwoRule extends AbstractLoadBalancerRule {
    private static Logger logger = LoggerFactory.getLogger(LevelOneRule.class);

//    private RandomRule randomRule; //初始时使用RandomRule进行选择

    public LevelTwoRule() {
//        randomRule = new RandomRule();
    }

    public LevelTwoRule(ILoadBalancer lb) {
        this();
        setLoadBalancer(lb);
    }

    public Server choose(ILoadBalancer lb, Object key) {
        if(lb == null) {
            logger.warn("no load balancer for level two");
            return null;
        }
        Server server = null;

        int count = 0; //尝试10次；
        while (server == null && count++ < 10) {
            List<Server> allServers = lb.getAllServers();
            List<Server> reachableServers = lb.getReachableServers();
            int upCount = reachableServers.size();
            int serverCount = allServers.size();

            if((upCount == 0) || (serverCount == 0)) {
                logger.warn("no up servers available from load balancer");
                return null;
            }

            AbstractLoadBalancer nlb = (AbstractLoadBalancer) lb;
            LoadBalancerStats stats = nlb.getLoadBalancerStats();
            server = _choose(reachableServers, stats);
            if(server == null) {
                Thread.yield();
                continue;
            }
            if(server.isAlive() && server.isReadyToServe()) {
                return server;
            }
            server = null;
        }
        if(count >= 10) {
            logger.warn("No available alive servers after 10 tries from load balancer: " + lb);
        }
        return server;

    }

    //LoadBalancerStats里面维护了server与Stats的对应关系，根据server获取其stats，而对于stats中有MultiTimeSlot的信息就可以了
    private Server _choose(List<Server> reachableServer, LoadBalancerStats stats) {
        if(stats == null) {
            logger.warn("no statistics, nothing to do yeil");
            return null;
        }
        Server result = null;
        int maxBusyness = Integer.MAX_VALUE;
        for(Server server : reachableServer) {
            BusynessIndicatorForLevelTwoServerStats ss = (BusynessIndicatorForLevelTwoServerStats)  stats.getSingleServerStat(server);
//            int tempBusyness = ss.getBusynessForLevelOne();
//            int tempBusyness = ss.getBusynessForLevelTwo();
//            int tempBusyness = ss.getBusynessForLevelTwoWithLimitTime(OuYangContext.levelTwoPastTime);
            int tempBusyness = ss.getBusyness();
            if(maxBusyness > tempBusyness) {
                maxBusyness = tempBusyness;
                result = server;
            }
        }
        return result;
    }

    @Override
    public Server choose(Object key) {
        return choose(getLoadBalancer(), key);
    }


    @Override
    public String getStatsClassName() {
        return "busynessIndicator.BusynessIndicatorForLevelTwoServerStats";
    }
}
