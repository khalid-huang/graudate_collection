package org.sysu.nameservice.loadbalancer.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sysu.nameservice.loadbalancer.ILoadBalancer;
import org.sysu.nameservice.loadbalancer.Server;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinRule extends AbstractLoadBalancerRule {
    private static Logger logger = LoggerFactory.getLogger(RoundRobinRule.class) ;

    private AtomicInteger nextServerCyclicCounter;
    private static final boolean AVAILABLE_ONLY_SERVERS = true;

    public RoundRobinRule() {
        nextServerCyclicCounter = new AtomicInteger(0);
    }

    public RoundRobinRule(ILoadBalancer lb) {
        this();
        setLoadBalancer(lb);
    }

    public Server choose(ILoadBalancer lb, Object key) {
        if(lb == null) {
            logger.warn("no load balancer");
            return null;
        }

        Server server = null;
        int count = 0;
        while (server == null && count++ < 10) {
            List<Server> reachableServers = lb.getReachableServers();
            List<Server> allServers = lb.getAllServers();
            int upCount = reachableServers.size();
            int serverCount = allServers.size();

            if((upCount == 0) || (serverCount == 0)) {
                logger.warn("no up servers available from load balancer");
                return null;
            }

            int nextServerIndex = incrementAndGetModulo(serverCount);
            server = allServers.get(nextServerIndex);

            System.out.println("RoundRobinRule: upSize = " + upCount + " " + "allSize = " + serverCount);
            System.out.println("index: " + nextServerIndex);

            if(server == null) {
                Thread.yield();
                continue;
            }
            if(server.isAlive() && (server.isReadyToServe())) {
                return (server);
            }
            server = null;
        }
        if(count >= 10) {
            logger.warn("No available alive servers after 10 tries from load balancer: " + lb);
        }
        return server;
    }

    @Override
    public Server choose(Object key) {
        return choose(getLoadBalancer(), key);
    }

    @Override
    public String getStatsClassName() {
        return "BaseServerStats";
    }

    private int incrementAndGetModulo(int modulo) {
        for(;;) {
            int current = nextServerCyclicCounter.get();
            int next = (current + 1) % modulo;
            if(nextServerCyclicCounter.compareAndSet(current, next)) {
                return next;
            }
        }
    }
}
