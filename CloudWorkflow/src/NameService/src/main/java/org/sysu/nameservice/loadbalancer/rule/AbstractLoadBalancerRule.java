package org.sysu.nameservice.loadbalancer.rule;

import org.sysu.nameservice.loadbalancer.ILoadBalancer;
import org.sysu.nameservice.loadbalancer.rule.IRule;

public abstract class AbstractLoadBalancerRule implements IRule {
    private ILoadBalancer lb;

    @Override
    public void setLoadBalancer(ILoadBalancer lb) {
        this.lb = lb;
    }

    @Override
    public ILoadBalancer getLoadBalancer() {
        return lb;
    }

    @Override
    public String getStatsClassName() {
        return "EmptyServerStats";
    }
}
