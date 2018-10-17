package org.sysu.nameservice.loadbalancer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sysu.nameservice.loadbalancer.stats.BaseServerStats;
import org.sysu.nameservice.loadbalancer.stats.IServerStats;

import java.lang.reflect.Constructor;
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
    private static final Logger logger = LoggerFactory.getLogger(LoadBalancerStats.class);


    private static final String statsClassNameSuffix = "org.sysu.nameservice.loadbalancer.stats";

    private static final String DEFAULTSTATSCLASSNAME = "EmptyServerStats";

    String name;

    /** 如果有性能问题，可以考虑使用Google的CacheBuilder来实现缓存*/
    private final ConcurrentHashMap<Server, IServerStats> serverStatsMap = new ConcurrentHashMap<>();

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

    private IServerStats createServerStats(Server server, String statsClassName) {
        try {
            statsClassName = statsClassNameSuffix + "." + statsClassName;
            Class classType = Class.forName(statsClassName);
            IServerStats ss = (IServerStats) classType.newInstance();
            ss.initialize(server);
            return ss;
        } catch (Exception e) {
            logger.info("反射构建" + statsClassName + "失败: " + e.toString());
            return null;
        }
    }

    public void addServer(Server server) {
        addServer(server,DEFAULTSTATSCLASSNAME);
    }

    public void addServer(Server server, String statsClassName) {
        if(serverStatsMap.get(server) == null) {
            IServerStats ss = createServerStats(server, statsClassName);
            serverStatsMap.putIfAbsent(server,ss);
        }
    }

    public void updateServerList(List<Server> servers) {
        for(Server s : servers) {
            addServer(s);
        }
    }

    /** 会保证在加入server的时候就已经创建了相应的Stats*/
    public IServerStats getServerStats(Server server) {
        return serverStatsMap.get(server);
    }

//    public void noteResponseTime(Server server, double msecs){
//        BaseServerStats ss = getServerStats(server);
//        ss.noteResponseTime(msecs);
//    }
//
//    public void incrementActiveRequestsCount(Server server) {
//        BaseServerStats ss = getServerStats(server);
//        ss.incrementActiveRequestsCount();
//    }
//
//    public void decrementActiveRequestCount(Server server) {
//        BaseServerStats ss = getServerStats(server);
//        ss.decrementActiveRequestCount();
//    }
//
//    public void incrementNumRequest(Server server) {
//        BaseServerStats ss = getServerStats(server);
//        ss.incrementNumRequests();
//    }


    public IServerStats getSingleServerStat(Server server) {
        return getServerStats(server);
    }

    public Map<Server, IServerStats> getServerStatsMap() {
        return serverStatsMap;
    }
}
