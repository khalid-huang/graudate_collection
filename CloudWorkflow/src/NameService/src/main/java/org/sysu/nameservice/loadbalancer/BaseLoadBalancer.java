package org.sysu.nameservice.loadbalancer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sysu.nameservice.loadbalancer.rule.IRule;
import org.sysu.nameservice.loadbalancer.rule.RoundRobinRule;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BaseLoadBalancer extends AbstractLoadBalancer {
    private static Logger logger = LoggerFactory.getLogger(BaseLoadBalancer.class);

    private final static IRule DEFAULT_RULE = new RoundRobinRule();
    private static final String DEFAULT_NAME = "default";

    protected IRule rule = DEFAULT_RULE;
    protected volatile List<Server> allServerList = Collections.synchronizedList(new ArrayList<Server>());
    protected volatile List<Server> upServerList = Collections.synchronizedList(new ArrayList<Server>());
    protected String name = DEFAULT_NAME;

    protected ReadWriteLock allServerLock = new ReentrantReadWriteLock();
    protected ReadWriteLock upServerLock = new ReentrantReadWriteLock();

    protected Timer lbTimer = null;
    protected Comparator<Server> serverComparator = new ServerComparator();

    protected LoadBalancerStats loadBalancerStats;

//    public BaseLoadBalancer() {
//        setRule(DEFAULT_RULE);
//        loadBalancerStats = new LoadBalancerStats(DEFAULT_NAME);
//    }

    // 使用Activiti时使用
    public BaseLoadBalancer() {
        setRule(DEFAULT_RULE);
        loadBalancerStats = new ActivitiLoadBalancerStats(DEFAULT_NAME);
    }

    public BaseLoadBalancer(IRule rule) {
        this(rule, new LoadBalancerStats(DEFAULT_NAME));
    }

    public BaseLoadBalancer(IRule rule, LoadBalancerStats loadBalancerStats) {
        setRule(rule);
        this.loadBalancerStats = loadBalancerStats;
    }

    public void setRule(IRule rule) {
        if(rule != null) {
            this.rule = rule;
        } else {
            this.rule = new RoundRobinRule();
        }
        if(this.rule.getLoadBalancer() != this) {
            this.rule.setLoadBalancer(this);
        }
    }

    public IRule getRule() {
        return rule;
    }

    void setName(String name) {
        this.name = name;
        if (loadBalancerStats == null) {
            loadBalancerStats = new LoadBalancerStats(name);
        } else {
            loadBalancerStats.setName(name);
        }
    }

    public String getName() {
        return name;
    }

    public int getServerCount(boolean onlyAvailable) {
        if(onlyAvailable) {
            return upServerList.size();
        } else {
            return allServerList.size();
        }
    }

    @Override
    public List<Server> getServerList(ServerGroup serverGroup) {
        switch (serverGroup) {
            case ALL:
                return allServerList;
            case STATUS_UP:
                return upServerList;
            case STATUS_NOT_UP:
                ArrayList<Server> notAvailableServers = new ArrayList<Server>(allServerList);
                ArrayList<Server> upServers = new ArrayList<Server>(upServerList);
                notAvailableServers.removeAll(upServers);
                return notAvailableServers;

        }
        return new ArrayList<Server>();
    }

    @Override
    public LoadBalancerStats getLoadBalancerStats() {
        return loadBalancerStats;
    }

    public void setLoadBalancerStats(LoadBalancerStats loadBalancerStats) {
        this.loadBalancerStats = loadBalancerStats;
    }

    public Lock lockAllserverList(boolean write) {
        Lock aproposLock = write ? allServerLock.writeLock(): allServerLock.readLock();
        aproposLock.lock();
        return aproposLock;
    }

    /**
     * Add a server to the 'allServer' list; does not verify uniqueness, so you
     * could give a server a greater share by adding it more than once.
     */
    public void addServer(Server newServer) {
        if(newServer != null) {
            try {
                ArrayList<Server> newList = new ArrayList<>();
                newList.addAll(allServerList);
                newList.add(newServer);
                setServersList(newList);
            } catch (Exception e) {
                logger.error("LoadBalancer [{}]: Error adding newServer {}", name, newServer.getHost(), e);
            }
        }
    }

    public void setServersList(List lsrv) {
        Lock writeLock = allServerLock.writeLock();
        ArrayList<Server> newServers = new ArrayList<Server>();
        writeLock.lock();
        try {
            ArrayList<Server> allServers = new ArrayList<Server>();
            for(Object server : lsrv) {
                if(server == null) {
                    continue;
                }

                if(server instanceof String) {
                    server = new Server((String) server);
                }

                if(server instanceof Server) {
                    allServers.add((Server) server);
                }
                //可以用于判断服务器列表是否发生变化
            }
            allServerList = allServers;
            for(Server s : allServers) {
                s.setAlive(true);
            }
            upServerList = allServerList;

        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void addServers(List<Server> newServers) {
        if(newServers != null && newServers.size() > 0) {
            try {
                ArrayList<Server> newList = new ArrayList<Server>();
                newList.addAll(allServerList);
                newList.addAll(newServers);
                setServersList(newList);
            } catch (Exception e) {
                logger.error("LoadBalancer [{}]: Error adding servers", name, e);
            }
        }
    }

    /*List in strign form */
    void setServers(String srvString) {
        if(srvString != null) {
            try {
                String[] serverArr = srvString.split(",");
                ArrayList<Server> newList = new ArrayList<Server>();
                for(String serverString : serverArr) {
                    if(serverArr != null) {
                        serverString = serverString.trim();
                        if(serverString.length() > 0) {
                            Server svr = new Server(serverString);
                            newList.add(svr);
                        }
                    }
                }
                setServersList(newList);
            } catch (Exception e) {
                logger.error("LoadBalancer [{}]: Exception while adding Servers", name, e);
            }
        }
    }

    /**
     * 根据下标获取服务实例
     * @param index
     * @param availableOnly 是否只获取活跃的
     * @return
     */
    public Server getServerByIndex(int index, boolean availableOnly) {
        try {
            return (availableOnly ? upServerList.get(index) : allServerList.get(index));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Server chooseServer(Object key) {
        if(rule == null) {
            return  null;
        } else {
            try {
                return rule.choose(key);
            } catch (Exception e) {
                logger.warn("LoadBalancer [{}]:  Error choosing server for key {}", name, key, e);
                return null;
            }
        }
    }

    @Override
    public Server chooseFirstServer(Object key) {
        return this.upServerList.get(0);
    }

    /* Returns either null, or "server:port/servlet" */
    public String choose(Object key) {
        if(rule == null) {
            return null;
        } else {
            try {
                Server svr = rule.choose(key);
                return ((svr) == null) ? null :svr.getId();
            } catch (Exception e) {
                logger.warn("LoadBalancer [{}]:  Error choosing server", name, e);
                return null;
            }
        }
    }

    @Override
    public void markServerDown(Server server) {
        if(server == null || !server.isAlive()) {
            return;
        }
        server.setAlive(false);
    }

    public void markServerDown(String id) {
        id = Server.normalizeId(id); //host:port
        if(id == null) {
            return;
        }
        Lock writeLock = upServerLock.writeLock();
        writeLock.lock();
        try {
            for(Server svr : upServerList) {
                if(svr.isAlive() && (svr.getId().equals(id))) {
                    svr.setAlive(false);
                }
            }
        } finally {
            writeLock.unlock();
        }

    }

    @Override
    public List<Server> getServerList(boolean availableOnly) {
        return (availableOnly ? getReachableServers() : getAllServers());
    }

    @Override
    public List<Server> getReachableServers() {
        return Collections.unmodifiableList(upServerList);
    }

    @Override
    public List<Server> getAllServers() {
        return Collections.unmodifiableList(allServerList);
    }
}
