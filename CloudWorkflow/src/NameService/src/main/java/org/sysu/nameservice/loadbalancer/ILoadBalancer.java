package org.sysu.nameservice.loadbalancer;

import java.util.List;

/** 每个serviceId都会对应一个ILoadBalancer ；这个ILoadBalancer里面维护了这个serviceId的服务列表信息*/
public interface ILoadBalancer {

    /**
     * Initial list of servers.
     * This API also serves to add additional ones at a later time
     * The same logical server (host:port) could essentially be added multiple times
     * (helpful in cases where you want to give more "weightage" perhaps ..)
     *
     * @param newServers new servers to add
     */
    public void addServers(List<Server> newServers);

    /**
     * Choose a server from load balancer.
     *
     * @param key An object that the load balancer may use to determine which server to return. null if
     *         the load balancer does not use this parameter.
     * @return server chosen
     */
    public Server chooseServer(Object key);

    /** 用于测试的方式，总返回第一个服务器 */
    public Server chooseFirstServer(Object key);

    /**
     * To be called by the clients of the load balancer to notify that a Server is down
     * else, the LB will think its still Alive until the next Ping cycle - potentially
     * (assuming that the LB Impl does a ping)
     *
     * @param server Server to mark as down
     */
    public void markServerDown(Server server);

    /**
     * @deprecated 2016-01-20 This method is deprecated in favor of the
     * cleaner {@link #getReachableServers} (equivalent to availableOnly=true)
     * and {@link #getAllServers} API (equivalent to availableOnly=false).
     *
     * Get the current list of servers.
     *
     * @param availableOnly if true, only live and available servers should be returned
     */
    @Deprecated
    public List<Server> getServerList(boolean availableOnly);

    /**
     * @return Only the servers that are up and reachable.
     */
    public List<Server> getReachableServers();

    /**
     * @return All known servers, both reachable and unreachable.
     */
    public List<Server> getAllServers();
}
