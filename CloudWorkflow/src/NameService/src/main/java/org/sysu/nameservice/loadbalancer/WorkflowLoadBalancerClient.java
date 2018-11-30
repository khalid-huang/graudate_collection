package org.sysu.nameservice.loadbalancer;

import com.alibaba.fastjson.JSON;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.sysu.nameservice.loadbalancer.rule.IRule;
import org.sysu.nameservice.loadbalancer.stats.IServerStats;
import org.sysu.nameservice.resourceProvider.ServerManager;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.*;

/** 需要负责维护serviceId与loadbalcer的对应关系，每个serviceId都会有一个loadBalacner，
 *  每个Loadbalancer都维护了一个服务列表，表示这个服务的服务地址
 * */
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

                    /**往baseloadbalancer里面维护和loadbalancerstats里面加入server对应的serverstats，需要根据IRule来加入*/
                    List<Server> servers = baseLoadBalancer.getAllServers();
                    LoadBalancerStats loadBalancerStats = baseLoadBalancer.getLoadBalancerStats();
                    String statsClassName = baseLoadBalancer.getRule().getStatsClassName();
                    for(Server server : servers) {
                        loadBalancerStats.addServer(server, statsClassName);
                    }

                    serviceIdToLoadBalancer.putIfAbsent(serviceId, baseLoadBalancer);
                }
            }
        }
    }

    @Override
    public String execute(String serviceId, LoadBalancerRequest request) throws Exception {
        ILoadBalancer loadBalancer = getLoadBalancer(serviceId);
        Server server = null;

        // 相同流程实例需要确保引擎尽量少
        String processInstanceId = ((WorkflowLoadBalancerRequest)request).getValue("processInstanceId");
        if (processInstanceId == null) {
            server = getServer(loadBalancer, "default");
        }
        else {
            server = getServer(loadBalancer, processInstanceId);
        }

        if(server == null) {
            throw new IllegalStateException("No instance available for " + serviceId);
        }
        WorkflowServer workflowServer = new WorkflowServer(serviceId, server);

        return execute(serviceId, workflowServer, request);
    }

    @Override
    public String execute(String serviceId, ServiceInstance serviceInstance, LoadBalancerRequest request) throws Exception {
        Server server = null;
        if(serviceInstance instanceof WorkflowServer) {
            server = ((WorkflowServer)serviceInstance).getServer();
        }
        if(server == null) {
            throw new IllegalStateException("No instance available for " + serviceId);
        }
        ILoadBalancer loadBalancer = getLoadBalancer(serviceId);
        WorkflowBalancerContext context = new WorkflowBalancerContext(loadBalancer);
        /** 根据不同的调度策略来生成不同的stats类型; 如果是之前已经存在的，这个WorkflowStatsRecorder只是一个辅助类，用来获取保存在LoadBalancer里面的stats*/
        WorkflowStatsRecorder statsRecorder = new WorkflowStatsRecorder(context, server);
        /** 进行请求时数据统计 */
        Map<String, Object> requstStartData = new HashMap<>();
        statsRecorder.recordRequestStartStarts(requstStartData);

        String returnVal = null;
        try {
            returnVal = request.apply(serviceInstance);
            //访问成功的数据统计；构建需要的Map对象；一般的调度是不是需要的，所以就传入一个空的就可以了
            Map<String,Object> data = new HashMap<>();
            data.put("request", request);
            data.put("response", returnVal);
            data.put("status", "success");
            //直接传入request和response，如何处理交由特定的ServerStats处理就可以了
            statsRecorder.recordRequestCompelteStats(data);
        } catch (Exception ex) {
            Map<String,Object> data = new HashMap<>();
            data.put("status", "fail");
            statsRecorder.recordRequestCompelteStats(data);
        }

        // 启动流程后加入ServerGroup
        Map<String, String> response = JSON.parseObject(returnVal, Map.class);
        String processInstanceId = response.get("processInstanceId");
        if (processInstanceId != null) {
            ((ActivitiLoadBalancerStats)((BaseLoadBalancer)loadBalancer).getLoadBalancerStats())
                    .addServerToServerGroup(processInstanceId, server);
        }

        /** 打印调试信息 */
        BaseLoadBalancer baseLoadBalancer = (BaseLoadBalancer) getLoadBalancer(serviceId);
        //打印stats信息
        LoadBalancerStats loadBalancerStats = baseLoadBalancer.getLoadBalancerStats();
        IServerStats serverStats = loadBalancerStats.getSingleServerStat(server);
        System.out.println(serverStats.toString());
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
        return getServer(getLoadBalancer(serviceId), "default");
    }

    protected  Server getServer(ILoadBalancer loadBalancer, String key) {
        if(loadBalancer == null) {
            return null;
        }
        return loadBalancer.chooseServer(key);
    }

    /**
     * 用于测试的方式，用于第一个server
     * @param loadBalancer
     * @return
     */
    protected Server getFirstServer(ILoadBalancer loadBalancer) {
        if(loadBalancer == null) {
            return null;
        }
        return loadBalancer.chooseFirstServer("default");
    }

    public ILoadBalancer getLoadBalancer(String serviceId) {
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

    public Map<String, ILoadBalancer> getServiceIdToLoadBalancer() {
        return serviceIdToLoadBalancer;
    }
}
