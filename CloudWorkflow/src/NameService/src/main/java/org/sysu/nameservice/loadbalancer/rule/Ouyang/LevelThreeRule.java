package org.sysu.nameservice.loadbalancer.rule.Ouyang;

import org.sysu.nameservice.loadbalancer.Server;
import org.sysu.nameservice.loadbalancer.rule.AbstractLoadBalancerRule;

public class LevelThreeRule extends AbstractLoadBalancerRule {
    @Override
    public Server choose(Object key) {
        return null;
    }
}
