package org.sysu.nameservice.loadbalancer.rule.Ouyang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sysu.nameservice.loadbalancer.AbstractLoadBalancer;
import org.sysu.nameservice.loadbalancer.ILoadBalancer;
import org.sysu.nameservice.loadbalancer.LoadBalancerStats;
import org.sysu.nameservice.loadbalancer.Server;
import org.sysu.nameservice.loadbalancer.rule.AbstractLoadBalancerRule;
import org.sysu.nameservice.loadbalancer.stats.busynessIndicator.BusynessIndicatorForLevelOneServerStats;

import java.util.List;

/**
 *  Applying a general work scheduling or resource allocation mecha- nism (e.g. random choice) without considering how busy each engine is
 */

public class LevelOneRule extends AbstractLoadBalancerRule {
    private static Logger logger = LoggerFactory.getLogger(LevelOneRule.class);

//    private RandomRule randomRule; //初始时使用RandomRule进行选择

    public LevelOneRule() {
//        randomRule = new RandomRule();
    }

    public LevelOneRule(ILoadBalancer lb) {
        this();
        setLoadBalancer(lb);
    }

    public Server levelOneRuleChoose(ILoadBalancer lb, Object key) {
        if(lb == null) {
            logger.warn("no load balancer");
            return null;
        }
        Server server = null;

        int count = 0; //尝试10次；
        while (server == null && count++ < 10) {
            List<Server> reachableServers = lb.getReachableServers();
            List<Server> allServers = lb.getAllServers();
            int upCount = reachableServers.size();
            int serverCount = allServers.size();

            if((upCount == 0) || (serverCount == 0)) {
                logger.warn("no up servers available from load balancer");
                return null;
            }

            AbstractLoadBalancer nlb = (AbstractLoadBalancer) lb;
            LoadBalancerStats stats = nlb.getLoadBalancerStats();
            server = levelOneRule_choose(reachableServers, stats);
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
    private Server levelOneRule_choose(List<Server> reachableServer, LoadBalancerStats stats) {
        if(stats == null) {
            logger.warn("no statistics, nothing to do so");
            return null;
        }
        Server result = null;
        int minBusyness = Integer.MAX_VALUE;
        for(Server server : reachableServer) {
            BusynessIndicatorForLevelOneServerStats ss = (BusynessIndicatorForLevelOneServerStats)stats.getSingleServerStat(server);
            int tempBusyness = ss.getBusyness();
            if(minBusyness > tempBusyness) {
                minBusyness = tempBusyness;
                result = server;
            }
        }
        return result;
    }

    @Override
    public Server choose(Object key) {
        return levelOneRuleChoose(getLoadBalancer(), key);
    }


    @Override
    public String getStatsClassName() {
        return "busynessIndicator.BusynessIndicatorForLevelOneServerStats";
    }
}
