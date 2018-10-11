package org.sysu.nameservice.loadbalancer;

import java.net.URI;
import java.util.Map;

/**
 * 服务发现系统上的服务实例；
 * 为了方便，不使用服务发现与注册
 */
public interface ServiceInstance {
    /**
     * @return 服务ID
     */
    String getServiceId();

    String getHost();

    int getPort();

    /**
     * @return 服务URI地址
     */
    URI getUri();

    /**
     * @return 与该服务实例相关的键值元数据
     */
    Map<String ,String> getMetadata();
}
