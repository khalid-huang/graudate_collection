package org.sysu.nameservice.loadbalancer;

/**
 * LoadBalancerClient发送请求的接口
 * @param <T>
 */
public interface LoadBalancerRequest<T> {
    public T apply(ServiceInstance instance) throws Exception;
}
