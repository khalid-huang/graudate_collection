package org.sysu.nameservice.loadbalancer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.sysu.nameservice.loadbalancer.rule.IRule;
import org.sysu.nameservice.loadbalancer.stats.IServerStats;
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

    /** 初始化 实验阶段为了方便，把所有的东西都初始化出来;主要是loadBalancer的初始化*/
    @PostConstruct
    public void init() {
        serviceIdToLoadBalancer = new HashMap<String, ILoadBalancer>();
        String serviceIds = environment.getProperty(serviceIdProperty);
        if(serviceIds != null) {
            String[] serviceIdArr = serviceIds.split(",");
            for(String serviceId : serviceIdArr) {
                if(serviceId != null) {
                    BaseLoadBalancer baseLoadBalancer = new BaseLoadBalancer();

                    /** 设置Rule*/
                    String ruleClassName = environment.getProperty(serviceId + ruleClassNameSuffix).trim();
                    try {
                        Class classType = Class.forName(ruleClassName);
                        IRule rule = (IRule) classType.newInstance();
                        baseLoadBalancer.setRule(rule);
                    } catch (Exception e) {
                        logger.warn(String.format("reflect balancer rule %s fail", ruleClassName));
                        throw new RuntimeException(e);
                    }

                    /** 设置serverlist*/
                    serviceId = serviceId.trim();
                    String srvString = serverManager.getServersInfoByServiceId(serviceId);
                    baseLoadBalancer.setServers(srvString);

                    /**往baseloadbalcer里面维护和loadbalancerstats里面加入server对应的serverstats，需要根据IRule来加入*/
                    List<Server> servers = baseLoadBalancer.getAllServers();
                    LoadBalancerStats loadBalancerStats = baseLoadBalancer.getLoadBalancerStats();
                    String statsClassName = baseLoadBalancer.getRule().getStatsClassName();
                    for(Server server : servers) {
                        loadBalancerStats.addServer(server,statsClassName);
                    }

                    serviceIdToLoadBalancer.putIfAbsent(serviceId, baseLoadBalancer);
                }
            }
        }
    }

    @Override
    public <T> T execute(String serviceId, LoadBalancerRequest<T> request) throws Exception {
        /** 针对有状态的activiti做的执行方案；需要根据流程实例调度的*/
//        if(serviceId.equals("activiti-service")) {
//            return executeActivitiService(request);
//        }
        /** 正常的无状态的服务的执行方案*/
        ILoadBalancer loadBalancer = getLoaBalancer(serviceId);
        Server server = getServer(loadBalancer);
        /** 调试信息 */

        /** end */

        if(server == null) {
            throw new IllegalStateException("No instance avaialbe for " + serviceId);
        }
        WorkflowServer workflowServer = new WorkflowServer(serviceId, server);

        return execute(serviceId, workflowServer, request);
    }

    public <T> T executeActivitiService(LoadBalancerRequest<T> request) {
        return null;
    }

    //同步访问
    @Override
    public <T> T execute(String serviceId, ServiceInstance serviceInstance, LoadBalancerRequest<T> request) throws Exception {
        Server server = null;
        if(serviceInstance instanceof WorkflowServer) {
            server = ((WorkflowServer)serviceInstance).getServer();
        }
        if(server == null) {
            throw new IllegalStateException("No instances available for " + serviceId);
        }
        //后面用数据统计的时候需要去仿照RibbonStatsRecorder来保存数据；先把整体逻辑实现，再看这个信息统计的实现

        WorkflowBalancerContext context = new WorkflowBalancerContext(getLoaBalancer(serviceId));
        /** 根据不同的调度策略来生成不同的stats类型*/
        WorkflowStatsRecorder statsRecorder = new WorkflowStatsRecorder(context, server);
        T returnVal = null;
        try {
            returnVal = request.apply(serviceInstance);
            //访问成功的数据统计；构建需要的Map对象；一般的调度是不是需要的，所以就传入一个空的就可以了
            Map<String,Object> data = new HashMap<>();
            data.putIfAbsent("response", returnVal);
            statsRecorder.recordStats(data);
        } catch (Exception ex) {
            //数据统计
        }
        /** 打印调试信息 */
        BaseLoadBalancer baseLoadBalancer = (BaseLoadBalancer) getLoaBalancer(serviceId);
        //打印stats信息
        LoadBalancerStats loadBalancerStats = baseLoadBalancer.getLoadBalancerStats();
        IServerStats serverStats = loadBalancerStats.getSingleServerStat(server);
        System.out.println(serverStats.toString());
        logger.info(serverStats.toString());

        /** end */

        return returnVal;

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

    public ILoadBalancer getLoaBalancer(String serviceId) {
        //这里先只有BaseLoadBalancer；BaseLoadBalancer是默认
        //后期如果需要扩展的话，可以使用反射来生成
        return serviceIdToLoadBalancer.get(serviceId);
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
