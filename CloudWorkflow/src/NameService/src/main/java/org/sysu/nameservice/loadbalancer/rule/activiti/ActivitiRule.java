package org.sysu.nameservice.loadbalancer.rule.activiti;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sysu.nameservice.loadbalancer.ActivitiLoadBalancerStats;
import org.sysu.nameservice.loadbalancer.AbstractLoadBalancer;
import org.sysu.nameservice.loadbalancer.ILoadBalancer;
import org.sysu.nameservice.loadbalancer.LoadBalancerStats;
import org.sysu.nameservice.loadbalancer.Server;
import org.sysu.nameservice.loadbalancer.rule.Ouyang.LevelOneRule;
import org.sysu.nameservice.loadbalancer.stats.busynessIndicator.BusynessIndicatorForActivitiServerStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 实现同一流程实例的引擎数尽量少
 * @author: Gordan Lin
 * @create: 2018/11/28
 **/
public class ActivitiRule extends LevelOneRule {

    private static Logger logger = LoggerFactory.getLogger(ActivitiRule.class);

    // 指定负载
    private final int LOADFACTOR = 1000;

    public ActivitiRule() {}

    public ActivitiRule(ILoadBalancer lb) {
        this();
        setLoadBalancer(lb);
    }

    public Server choose(ILoadBalancer lb, Object key) {
        if(lb == null) {
            logger.warn("no load balancer");
            return null;
        }
        Server server = null;

        int count = 0; //尝试10次；
        while (server == null && count++ < 10) {
            List<Server> reachableServers = lb.getReachableServers();
            int upCount = reachableServers.size();

            if((upCount == 0)) {
                logger.warn("no up servers available from load balancer");
                return null;
            }

            AbstractLoadBalancer nlb = (AbstractLoadBalancer) lb;
            LoadBalancerStats stats = nlb.getLoadBalancerStats();
            server = _choose(reachableServers, stats, key);
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

    private Server _choose(List<Server> reachableServer, LoadBalancerStats stats, Object key) {
        if(stats == null) {
            logger.warn("no statistics, nothing to do so");
            return null;
        }
        logger.info("ActivitiRule......");

        /* 根据流程实例id选择Server */
        String processInstanceId = (String) key;
        Server result = null;
        Set<Server> serverGroup = ((ActivitiLoadBalancerStats)stats).getServerListByProcessInstanceId(processInstanceId);
        // 第一次执行该流程实例
        if (serverGroup == null) {
            logger.info("第一次执行流程实例......");
            result = chooseMinBusyness(reachableServer, stats);
            // 添加到服务器组
            ((ActivitiLoadBalancerStats)stats).addServerToServerGroup(processInstanceId, result);
        }
        // 从之前执行的引擎中选择
        else {
            logger.info("从之前执行过的引擎中选择......");
            Set<Server> previousServers = ((ActivitiLoadBalancerStats)stats).getServerListByProcessInstanceId(processInstanceId);
            List<Server> previousServersList = new ArrayList<>(previousServers);
            result = chooseMinBusynessFromServerGroup(reachableServer, previousServersList, stats);
            ((ActivitiLoadBalancerStats)stats).addServerToServerGroup(processInstanceId, result);
        }
        logger.info("选择引擎{}", result.getId());
        return result;
    }

    private Server chooseMinBusyness(List<Server> servers, LoadBalancerStats stats) {
        Server result = null;
        int minBusyness = Integer.MAX_VALUE;
        for(Server server : servers) {
            BusynessIndicatorForActivitiServerStats ss = (BusynessIndicatorForActivitiServerStats)stats.getSingleServerStat(server);
            int tempBusyness = ss.getBusyness();
            if(minBusyness > tempBusyness) {
                minBusyness = tempBusyness;
                result = server;
            }
        }
        return result;
    }

    private Server chooseMinBusynessFromServerGroup(List<Server> reachableServer,
                                                    List<Server> previousServersList,
                                                    LoadBalancerStats stats) {
        Server result = null;
        int minBusyness = Integer.MAX_VALUE;
        for(Server server : previousServersList) {
            BusynessIndicatorForActivitiServerStats ss = (BusynessIndicatorForActivitiServerStats)stats.getSingleServerStat(server);
            int tempBusyness = ss.getBusyness();
            if(minBusyness > tempBusyness) {
                minBusyness = tempBusyness;
                result = server;
            }
        }
        if (minBusyness > LOADFACTOR) {
            reachableServer.removeAll(previousServersList);
            for (Server server : reachableServer) {
                BusynessIndicatorForActivitiServerStats ss = (BusynessIndicatorForActivitiServerStats) stats.getSingleServerStat(server);
                int tempBusyness = ss.getBusyness();
                if (minBusyness > tempBusyness) {
                    minBusyness = tempBusyness;
                    result = server;
                }
            }
        }
        return result;
    }

    @Override
    public Server choose(Object key) {
        if ("default".equals(key)) return super.choose(key);
        return choose(getLoadBalancer(), key);
    }

    @Override
    public String getStatsClassName() {
        return "busynessIndicator.BusynessIndicatorForActivitiServerStats";
    }

}
