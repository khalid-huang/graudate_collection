package org.sysu.nameservice.loadbalancer;

import java.net.URI;
import java.util.Map;

/**
 * 服务实例类
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
