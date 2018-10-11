package org.sysu.nameservice.loadbalancer;

import java.util.List;

public abstract class AbstractLoadBalancer implements ILoadBalancer {
    public enum ServerGroup {
        ALL,
        STATUS_UP,
        STATUS_NOT_UP
    }

    /**
     * List of Servers that this LoadBalancer knows ablout
     *
     * @param serverGroup Servers group bystatus
     * @return
     */
    public abstract List<Server> getServerList(ServerGroup serverGroup);


    /**
     * Obtain LoadBalancer related Statistics
     *
     * @return
     */
    public abstract LoadBalancerStats getLoadBalancerStats();

}
