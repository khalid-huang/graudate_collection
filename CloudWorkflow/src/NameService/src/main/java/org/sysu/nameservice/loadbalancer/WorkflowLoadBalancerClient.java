package org.sysu.nameservice.loadbalancer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.sysu.nameservice.loadbalancer.rule.IRule;
import org.sysu.nameservice.resourceProvider.ServerManager;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.util.*;

/** 需要负责维护serviceId与loadbalcer的对应关系，每个serviceId都会有一个loadBalacner，每个Loadbalancer都维护了一个服务列表，表示这个服务的服务地址 */
@Component
public class WorkflowLoadBalancerClient implements LoadBalancerClient {
    private static Logger logger = LoggerFactory.getLogger(WorkflowLoadBalancerClient.class);

    @Autowired
    ServerManager serverManager;

    @Autowired
    Environment environment;

    /** 维护serviceId与其loadBalancer*/
    Map<String, ILoadBalancer> serviceIdToLoadBalancer;

    /** 在配置文件中配置的需要负载均衡的服务Id*/
    private static final String serviceIdProperty = "workflowBalancerClient.serviceId";

    private static final String ruleClassNameSuffix = ".workflow.LoadBalancerRuleClassName";

    /** 初始化 */
    @PostConstruct
    public void init() {
        serviceIdToLoadBalancer = new HashMap<String, ILoadBalancer>();
        String serviceIds = environment.getProperty(serviceIdProperty);
        if(serviceIds != null) {
            String[] serverIdArr = serviceIds.split(",");
            for(String serverId : serverIdArr) {
                if(serverId != null) {
                    BaseLoadBalancer baseLoadBalancer = new BaseLoadBalancer();
                    /** 设置serverlist*/
                    serverId = serviceIds.trim();
                    String srvString = serverManager.getServersInfoByServiceId(serverId);
                    baseLoadBalancer.setServers(srvString);
                    /** 设置Rule*/
                    String ruleClassName = environment.getProperty(serverId + ruleClassNameSuffix).trim();
                    try {
                        Class classType = Class.forName(ruleClassName);
                        IRule rule = (IRule) classType.newInstance();
                        baseLoadBalancer.setRule(rule);
                    } catch (Exception e) {
                        logger.warn(String.format("reflect balancer rule %s fail", ruleClassName));
                        throw new RuntimeException(e);
                    }
                    serviceIdToLoadBalancer.putIfAbsent(serverId, baseLoadBalancer);
                }
            }
        }
    }

    @Override
    public <T> T execute(String serviceId, LoadBalancerRequest<T> request) throws IOException {
        ILoadBalancer loadBalancer = getLoaBalancer(serviceId);
        Server server = getServer(loadBalancer);
        if(server == null) {
            throw new IllegalStateException("No instance avaialbe for " + serviceId);
        }
        WorkflowServer workflowServer = new WorkflowServer(serviceId, server);
        return execute(serviceId, workflowServer, request);
    }

    //同步访问
    @Override
    public <T> T execute(String serviceId, ServiceInstance serviceInstance, LoadBalancerRequest<T> request) throws IOException {
        Server server = null;
        if(serviceInstance instanceof WorkflowServer) {
            server = ((WorkflowServer)serviceInstance).getServer();
        }
        if(server == null) {
            throw new IllegalStateException("No instances available for " + serviceId);
        }
        //后面用数据统计的时候需要去仿照RibbonStatsRecorder来保存数据；先把整体逻辑实现，再看这个信息统计的实现

        WorkflowBalancerContext context = new WorkflowBalancerContext(getLoaBalancer(serviceId));
        WorkflowStatsRecorder statsRecorder = new WorkflowStatsRecorder(context, server);

        try {
            T returnVal = request.apply(serviceInstance);
            //访问成功的数据统计
            statsRecorder.recordStats(request);
            return returnVal;
        } catch (IOException ex) {
            //IO异常的数据统计
            throw ex;
        } catch (Exception ex) {
            //数据统计
        }
        return null;
    }

    @Override
    public URI reconstructURI(ServiceInstance instance, URI original) {
        return original;
    }

    @Override
    public ServiceInstance choose(String serviceId) {
        Server server = getServer(serviceId);
        if(server == null) {
            return null;
        }
        return new WorkflowServer(serviceId, server);
    }

    protected Server getServer(String serviceId) {
        return getServer(getLoaBalancer(serviceId));
    }

    protected  Server getServer(ILoadBalancer loadBalancer) {
        if(loadBalancer == null) {
            return null;
        }
        return loadBalancer.chooseServer("default");
    }

    protected ILoadBalancer getLoaBalancer(String serviceId) {
        //这里先只有BaseLoadBalancer；BaseLoadBalancer是默认
        //后期如果需要扩展的话，可以使用反射来生成
        return new BaseLoadBalancer();
    }

    public static class WorkflowServer implements ServiceInstance {
        private final String serviceId;
        private final Server server;
        private Map<String, String> metadata;

        public WorkflowServer(String serviceId, Server server) {
            this(serviceId, server, Collections.emptyMap());
        }

        public WorkflowServer(String serviceId, Server server, Map<String, String> metadata) {
            this.serviceId = serviceId;
            this.server = server;
            this.metadata = metadata;
        }

        @Override
        public String getServiceId() {
            return this.serviceId;
        }

        @Override
        public String getHost() {
            return this.server.getHost();
        }

        @Override
        public int getPort() {
            return this.server.getPort();
        }

        @Override
        public URI getUri() {
            String schema = "http";
            String uri = String.format("%s://%s:%s", schema, this.getHost(), this.getPort());
            return URI.create(uri);
        }

        @Override
        public Map<String, String> getMetadata() {
            return this.metadata;
        }

        public Server getServer() {
            return this.server;
        }
        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("RibbonServer{");
            sb.append("serviceId='").append(serviceId).append('\'');
            sb.append(", server=").append(server);
            sb.append(", metadata=").append(metadata);
            sb.append('}');
            return sb.toString();
        }
    }
}
