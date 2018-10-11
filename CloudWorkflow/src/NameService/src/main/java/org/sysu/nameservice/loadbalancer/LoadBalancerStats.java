package org.sysu.nameservice.loadbalancer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class that acts as a repository of operational charateristics and statistics
 * of every Node/Server in the LaodBalancer.
 *
 * This information can be used to just observe and understand the runtime
 * behavior of the loadbalancer or more importantly for the basis that
 * determines the loadbalacing strategy
 *
 */
public class LoadBalancerStats {
    String name;

    /** 如果有性能问题，可以考虑使用Google的CacheBuilder来实现缓存*/
    private final ConcurrentHashMap<Server, ServerStats> serverStatsMap = new ConcurrentHashMap<>();

    public LoadBalancerStats() {

    }

    public LoadBalancerStats(String name) {
        this();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private ServerStats createServerStats(Server server) {
        ServerStats ss = new ServerStats(this);
        ss.initialize(server);
        return ss;
    }

    public void addServer(Server server) {
        if(serverStatsMap.get(server) == null) {
            ServerStats ss = createServerStats(server);
            serverStatsMap.putIfAbsent(server,ss);
        }
    }

    public void updateServerList(List<Server> servers) {
        for(Server s : servers) {
            addServer(s);
        }
    }

    public ServerStats getServerStats(Server server) {
        ServerStats ss = serverStatsMap.get(server);
        if(ss == null) {
            ss = createServerStats(server);
            serverStatsMap.putIfAbsent(server, ss);
        }
        return ss;
    }

    /**
     * Method that updates the internal stats of Response times maintained on a per Server
     * basis
     * @param server
     * @param msecs
     */
    public void noteResponseTime(Server server, double msecs){
        ServerStats ss = getServerStats(server);
        ss.noteResponseTime(msecs);
    }

    public void incrementActiveRequestsCount(Server server) {
        ServerStats ss = getServerStats(server);
        ss.incrementActiveRequestsCount();
    }

    public void decrementActiveRequestCount(Server server) {
        ServerStats ss = getServerStats(server);
        ss.decrementActiveRequestCount();
    }

    public void incrementNumRequest(Server server) {
        ServerStats ss = getServerStats(server);
        ss.incrementNumRequests();
    }


    public ServerStats getSingleServerStat(Server server) {
        return getServerStats(server);
    }

    public Map<Server, ServerStats> getServerStats() {
        return serverStatsMap;
    }
}
