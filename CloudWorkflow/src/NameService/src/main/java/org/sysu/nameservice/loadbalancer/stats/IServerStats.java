package org.sysu.nameservice.loadbalancer.stats;

import org.sysu.nameservice.loadbalancer.Server;

import java.util.Map;

public interface IServerStats {
    /**
     * 根据数据 进行数据记录
     */
    public void noteRequestCompletion(Map<String, Object> data);

    public void initialize(Server server);
}
