package org.sysu.nameservice.loadbalancer;

/**
 * LoadBalancerClient发送请求的接口
 * @param
 */
public interface LoadBalancerRequest {
    public String apply(ServiceInstance instance) throws Exception;
}
