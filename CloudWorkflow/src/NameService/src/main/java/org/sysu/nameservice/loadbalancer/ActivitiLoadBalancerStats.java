package org.sysu.nameservice.loadbalancer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author: Gordan Lin
 * @create: 2018/11/28
 **/
public class ActivitiLoadBalancerStats extends LoadBalancerStats {

    private Map<String, Set<Server>> serverGroup;

    public ActivitiLoadBalancerStats() {
        super();
        serverGroup = new HashMap<>();
    }

    public ActivitiLoadBalancerStats(String name) {
        super(name);
        serverGroup = new HashMap<>();
    }

    public Set<Server> getServerListByProcessInstanceId(String processInstanceId) {
        return serverGroup.get(processInstanceId);
    }

    public void addServerToServerGroup(String processInstanceId, Server server) {
        Set<Server> servers = serverGroup.get(processInstanceId);
        if (servers == null) {
            servers = new HashSet<>();
        }
        servers.add(server);
        serverGroup.put(processInstanceId, servers);
    }

    public void removeServerFromServerGroup(String processInstanceId, Server server) {
        Set<Server> servers = serverGroup.get(processInstanceId);
        if (servers == null) return;
        servers.remove(server);
    }

}
