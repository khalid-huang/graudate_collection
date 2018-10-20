package org.sysu.nameservice.loadbalancer;

import java.io.IOException;
import java.net.URI;

public interface LoadBalancerClient extends ServiceInstanceChooser {

    /**
     * execute request using a ServiceInstance from the LoadBalancer for the specified service
     * @param serviceId the service id to look up the LoadBalancer
     * @param request allows implementations to execute pre and post actions such as incrementing metrics
     * @return the result of the LoadBalancerRequest callback on the selected ServiceInstance
     * @throws Exception
     */
    String execute(String serviceId, LoadBalancerRequest request) throws Exception;


    /**
     * execute request using a ServiceInstance from the LoadBalancer for the specified
     * service
     * @param serviceId the service id to look up the LoadBalancer
     * @param serviceInstance the service to execute the request to
     * @param request allows implementations to execute pre and post actions such as
     * incrementing metrics
     * @return the result of the LoadBalancerRequest callback on the selected
     * ServiceInstance
     */
    String execute(String serviceId, ServiceInstance serviceInstance, LoadBalancerRequest request) throws Exception;


    /**
     * Create a proper URI with a real host and port for systems to utilize.
     * Some systems use a URI with the logical serivce name as the host,
     * such as http://myservice/path/to/service.  This will replace the
     * service name with the host:port from the ServiceInstance.
     * @param instance
     * @param original a URI with the host as a logical service name
     * @return a reconstructed URI
     */
    URI reconstructURI(ServiceInstance instance, URI original);
}
