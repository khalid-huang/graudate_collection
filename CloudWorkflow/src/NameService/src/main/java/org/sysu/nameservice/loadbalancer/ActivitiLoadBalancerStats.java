package org.sysu.nameservice.loadbalancer;

import org.sysu.nameservice.loadbalancer.stats.busynessIndicator.BusynessIndicatorForActivitiServerStats;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: Gordan Lin
 * @create: 2018/11/28
 **/
public class ActivitiLoadBalancerStats extends LoadBalancerStats {

    private final Map<String, ServerGroup> proInstanceIdToServerGroup = new ConcurrentHashMap<>();

    public ActivitiLoadBalancerStats() {
        super();
    }

    public ActivitiLoadBalancerStats(String name) {
        super(name);
    }

    public ServerGroup getServerGroupByProcessInstanceId(String processInstanceId) {
        return proInstanceIdToServerGroup.get(processInstanceId);
    }

    public void addServerToServerGroup(String processInstanceId, Server server) {
        ServerGroup serverGroup = proInstanceIdToServerGroup.get(processInstanceId);
        if (serverGroup == null) {
            serverGroup = new ServerGroup();
        }
        serverGroup.getServerSets().add(server);
        proInstanceIdToServerGroup.put(processInstanceId, serverGroup);
    }

    public void checkRemoveServer() {
        Set<String> processinstanceIds =  proInstanceIdToServerGroup.keySet();
        for (String processInstanceId : processinstanceIds) {
            ServerGroup serverGroup = proInstanceIdToServerGroup.get(processInstanceId);
            // 保证至少有一个Server
            if (serverGroup.getServerSets().size() == 1) continue;
            if (serverGroup.getModified() == true) {
                removeServerFromServerGroup(processInstanceId, serverGroup.getServerSets());
            }
            serverGroup.setModified();
        }
    }

    public Map<String, ServerGroup> getProInstanceIdToServerGroup() {
        return proInstanceIdToServerGroup;
    }

    private void removeServerFromServerGroup(String processInstanceId, Set<Server> servers) {
        Server server = chooseMaxBusyness(servers);
        if (server != null) {
            proInstanceIdToServerGroup.get(processInstanceId).deleteServerFromServerSets(server);
        }
    }

    private Server chooseMaxBusyness(Set<Server> servers) {
        Server result = null;
        int maxBusyness = Integer.MIN_VALUE;
        for(Server server : servers) {
            BusynessIndicatorForActivitiServerStats ss = (BusynessIndicatorForActivitiServerStats)super.getSingleServerStat(server);
            int tempBusyness = ss.getBusyness();
            if(maxBusyness < tempBusyness) {
                maxBusyness = tempBusyness;
                result = server;
            }
        }
        return result;
    }

}
