package org.sysu.nameservice.loadbalancer.rule;

import org.sysu.nameservice.loadbalancer.ILoadBalancer;
import org.sysu.nameservice.loadbalancer.Server;

import java.util.List;
import java.util.Random;

public class RandomRule extends AbstractLoadBalancerRule {

    private Random rand;

    public RandomRule() {
        rand = new Random();
    }

    public Server choose(ILoadBalancer lb, Object key) {
        if(lb == null) {
            return null;
        }
        Server server = null;
        while (server == null) {
            if(Thread.interrupted()) {
                return null;
            }

            List<Server> upList = lb.getReachableServers();
            List<Server> allList = lb.getAllServers();

            int serverCount = allList.size();
            if(serverCount == 0) {
                return null;
            }

            int index = rand.nextInt(serverCount);
            server = upList.get(index);
            System.out.println("RandomRule: upSize = " + upList.size() + " " + "allSize = " + serverCount);
            System.out.println("index: " + index);
            if(server == null) {
                /*
                 * The only time this should happen is if the server list were
                 * somehow trimmed. This is a transient condition. Retry after
                 * yielding.
                 */
                Thread.yield();
                continue;
            }
            if(server.isAlive()) {
                return (server);
            }
            // Shouldn't actually happen.. but must be transient or a bug.
            server = null;
            Thread.yield();
        }
        return server;
    }

    @Override
    public Server choose(Object key) {
        return choose(getLoadBalancer(), key);
    }

}
