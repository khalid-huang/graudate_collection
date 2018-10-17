package org.sysu.nameservice.loadbalancer.rule.Ouyang;

import org.sysu.nameservice.loadbalancer.ILoadBalancer;
import org.sysu.nameservice.loadbalancer.Server;
import org.sysu.nameservice.loadbalancer.rule.AbstractLoadBalancerRule;
import org.sysu.nameservice.loadbalancer.rule.RandomRule;

import java.util.List;
import java.util.Random;

/**
 * 论文 Towards the Design of a Scalable Business Process Management System Architecture in the Cloud 的Level 负载分配的四种策略中的level 0
 * 本质就是服务先到先服务；服务器选择是采用随机选择，不考虑机器本身的负载状况
 */
public class LevelZeroRule extends AbstractLoadBalancerRule {

    private RandomRule randomRule;

    public LevelZeroRule() {
        randomRule = new RandomRule();
    }

    public Server choose(ILoadBalancer lb, Object key) {
        return randomRule.choose(lb, key);
    }

    @Override
    public Server choose(Object key) {
        return choose(getLoadBalancer(), key);
    }
}
