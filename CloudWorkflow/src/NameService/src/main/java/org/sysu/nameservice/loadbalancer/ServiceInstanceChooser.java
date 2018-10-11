package org.sysu.nameservice.loadbalancer;

/**
 * 模仿Ribbon实现的
 * 用于选择一个服务实例去发送请求
 */
public interface ServiceInstanceChooser {

   /**
    * 根据服务ID，使用负载均衡器返回一个服务ID对应的一个服务实例
    * @param serviceId
    * @return 跟服务ID相匹配的服务实例
    */
   ServiceInstance choose(String serviceId);
}
