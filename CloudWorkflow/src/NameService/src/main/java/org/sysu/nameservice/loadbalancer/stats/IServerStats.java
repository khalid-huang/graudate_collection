package org.sysu.nameservice.loadbalancer.stats;

import org.sysu.nameservice.loadbalancer.Server;

import java.util.Map;

public interface IServerStats {
    /**
     * 根据数据 进行数据记录（请求成功时）
     */
    public void noteRequestCompletion(Map<String, Object> data);

    /**
     * 根据数据进行数据记录（请求失败时）
     */
    public void noteRequestFail(Map<String, Object> data);

    /**
     * 在请求开始时进行统计
     * @param data
     */
    public void noteRequestStart(Map<String,Object> data);


    public void initialize(Server server);
}
