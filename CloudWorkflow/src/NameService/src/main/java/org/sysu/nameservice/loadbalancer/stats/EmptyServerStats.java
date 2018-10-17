package org.sysu.nameservice.loadbalancer.stats;

import org.sysu.nameservice.loadbalancer.Server;

import java.util.Map;

/** 空记录 */
public class EmptyServerStats implements IServerStats {
    @Override
    public void noteRequestCompletion(Map<String, Object> data) {
    }

    @Override
    public void initialize(Server server) {
    }

    @Override
    public void noteRequestFail(Map<String, Object> data) {
    }

    @Override
    public void noteRequestStart(Map<String, Object> data) {

    }
}
