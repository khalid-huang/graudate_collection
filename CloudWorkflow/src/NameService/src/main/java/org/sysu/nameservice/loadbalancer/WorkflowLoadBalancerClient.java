package org.sysu.nameservice.loadbalancer;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.sysu.nameservice.Entity.ProcessInstanceRoutingEntity;
import org.sysu.nameservice.GlobalContext;
import org.sysu.nameservice.loadbalancer.rule.IRule;
import org.sysu.nameservice.loadbalancer.rule.Ouyang.OuYangContext;
import org.sysu.nameservice.loadbalancer.stats.IServerStats;
import org.sysu.nameservice.resourceProvider.ServerManager;
import org.sysu.nameservice.service.ProcessInstanceRoutingService;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/** 需要负责维护serviceId与loadbalcer的对应关系，每个serviceId都会有一个loadBalacner，每个Loadbalancer都维护了一个服务列表，表示这个服务的服务地址 */
@Component
public class WorkflowLoadBalancerClient implements LoadBalancerClient {
    private static Logger logger = LoggerFactory.getLogger(WorkflowLoadBalancerClient.class);

    @Autowired
    ServerManager serverManager;

    @Autowired
    Environment environment;

    @Autowired
    ProcessInstanceRoutingService processInstanceRoutingService;

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
    public String execute(String serviceId, LoadBalancerRequest request) throws Exception {
        /** 针对特定的服务做的调度方案 */
        //activiti-service这种有状态的调度方案；这里主要的不同在于它不是根据请求来确定调度到的服务器，而是根据之前的请求情况，通过它的流程实例确定的服务器，所以需要单独处理下
        if(serviceId.equals("activiti-service")) {
            return executeActivitiService(request);
        }

        //其他服务的调度方案

        /**通用的无状态的服务的执行方案*/
        ILoadBalancer loadBalancer = getLoaBalancer(serviceId);
        Server server = getServer(loadBalancer);
        /** 调试信息 */

        /** end */

        if(server == null) {
            throw new IllegalStateException("No instance  for " + serviceId);
        }
        WorkflowServer workflowServer = new WorkflowServer(serviceId, server);

        return execute(serviceId, workflowServer, request);
    }



    @SuppressWarnings("unchecked")
    public String executeActivitiService(LoadBalancerRequest request) throws Exception {
        WorkflowLoadBalancerRequest workflowLoadBalancerRequest = (WorkflowLoadBalancerRequest) request;


        Server server = null;
        /** 针对测试函数的处理方式 */
        if(workflowLoadBalancerRequest.getAction().equals("TEST")) {
            server = getFirstServer(getLoaBalancer(GlobalContext.SERVICEID_ACTIVITISERVICE));
            WorkflowServer workflowServer = new WorkflowServer(GlobalContext.SERVICEID_ACTIVITISERVICE, server);
            return execute(GlobalContext.SERVICEID_ACTIVITISERVICE, workflowServer, request);
        }


        /** 进行流程实例调度 */
        //新建一个映射对象
        ProcessInstanceRoutingEntity pire;

        if(workflowLoadBalancerRequest.getAction().equals(GlobalContext.ACTION_ACTIVITISERVICE_STARTPROCESS)) {
           /** 新流程实例 */
           ILoadBalancer loadBalancer = getLoaBalancer(GlobalContext.SERVICEID_ACTIVITISERVICE);
           server = getServer(loadBalancer);
           //实例化映射对象
            pire = new ProcessInstanceRoutingEntity();
            pire.setStartTime(new Timestamp(new Date().getTime()));
            pire.setProcessModelKey(workflowLoadBalancerRequest.getValue("processModelKey"));
            pire.setServerStr(server.getId());

        } else {
            /** 从数据库中获取引导哪里去；这里其实是要引入缓存的(缓存应该实现在持久层上，不应该实现在逻辑层这里)；先简单实现, */
            /** 这里还需要将processInstanceId替换成相应的引擎的真实的id； */
            pire = processInstanceRoutingService.findById(Long.parseLong(workflowLoadBalancerRequest.getValue("processInstanceId")));

            server = new Server(pire.getServerStr());
            workflowLoadBalancerRequest.setKeyValue("processInstanceId", pire.getActivitiProcessInstanceId());

        }
        if(server == null) {
            throw new IllegalStateException("No instance available for " + GlobalContext.SERVICEID_ACTIVITISERVICE);
        }

        System.out.println("所选择的服务器" + server.getId());

        WorkflowServer workflowServer = new WorkflowServer(GlobalContext.SERVICEID_ACTIVITISERVICE, server);

        String response =  execute(GlobalContext.SERVICEID_ACTIVITISERVICE, workflowServer, request);
        /** 做数据更新 */
        Map<String, String> responseMap = JSON.parseObject(response, Map.class);
        /** 如果流程完成，记录完成时间；如果流程刚启动，记录流程ID*/
        if(workflowLoadBalancerRequest.getAction().equals(GlobalContext.ACTION_ACTIVITISERVICE_COMPLETETASK)) {
            if(responseMap.get("isEnded").equals("1")) {
                //更新数据
                pire.setFinishTime(new Timestamp(new Date().getTime()));
                processInstanceRoutingService.saveOrUpdate(pire);
            }
        } else if(workflowLoadBalancerRequest.getAction().equals(GlobalContext.ACTION_ACTIVITISERVICE_STARTPROCESS)) {
            pire.setActivitiProcessInstanceId(responseMap.get("processInstanceId"));
            //进行持久化
            pire = processInstanceRoutingService.saveOrUpdate(pire);
            //这里要注意返回给前端的ProcessInstanceId应该是nameService的processInstanceRouting的id；而不是真实的引擎的processInstanceId;
            responseMap.put("processInstanceId", String.valueOf(pire.getId()));
            response = JSON.toJSONString(responseMap);
        }

        return response;
    }

    @Override
    public String execute(String serviceId, ServiceInstance serviceInstance, LoadBalancerRequest request) throws Exception {
        Server server = null;
        if(serviceInstance instanceof WorkflowServer) {
            server = ((WorkflowServer)serviceInstance).getServer();
        }
        if(server == null) {
            throw new IllegalStateException("No instances available for " + serviceId);
        }
        WorkflowBalancerContext context = new WorkflowBalancerContext(getLoaBalancer(serviceId));
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
            //数据统计
        }

        /** 打印调试信息 */
        BaseLoadBalancer baseLoadBalancer = (BaseLoadBalancer) getLoaBalancer(serviceId);
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
        return getServer(getLoaBalancer(serviceId));
    }

    protected  Server getServer(ILoadBalancer loadBalancer) {
        if(loadBalancer == null) {
            return null;
        }
        return loadBalancer.chooseServer("default");
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

    public ILoadBalancer getLoaBalancer(String serviceId) {
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
