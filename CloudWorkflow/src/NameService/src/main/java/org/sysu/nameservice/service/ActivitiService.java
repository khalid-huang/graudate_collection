package org.sysu.nameservice.service;

import jdk.nashorn.internal.objects.Global;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.sysu.nameservice.GlobalContext;
import org.sysu.nameservice.loadbalancer.*;
import org.sysu.nameservice.loadbalancer.rule.Ouyang.OuYangContext;
import org.sysu.nameservice.loadbalancer.stats.BaseServerStats;
import org.sysu.nameservice.loadbalancer.stats.IServerStats;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ActivitiService {
    private static Logger logger = LoggerFactory.getLogger(ActivitiService.class);

    @Autowired
    WorkflowLoadBalancerClient workflowLoadBalancerClient;

    private static final String serviceId = GlobalContext.SERVICEID_ACTIVITISERVICE;

    public String getHelloworld() throws Exception {
        //主要目标是构建出request就可以了
        String urlWithoutServerInfo = "getHelloworld";
        Map<String, String> info = new HashMap<>();
        info.put("action", GlobalContext.ACTION_ACTIVITISERVICE_TEST);
        info.put("url", urlWithoutServerInfo);
        WorkflowLoadBalancerRequest request = new WorkflowLoadBalancerRequest(false, "GET", info, null, null, null);
        return workflowLoadBalancerClient.execute(serviceId, request);
    }

    public String helloworld(Map<String, Object> data) throws Exception {
        //构建request
        //测试下在data下面放一个variables，variable下面放test，看看Activiti是否可以收到
        String urlWithoutServerInfo = "/hellowold";
        Map<String, String> headers = null;
        Map<String, Object> params = data;
        Map<String, String> info = new HashMap<>();
        info.put("action", GlobalContext.ACTION_ACTIVITISERVICE_TEST);
        info.put("url", urlWithoutServerInfo);
        WorkflowLoadBalancerRequest request = new WorkflowLoadBalancerRequest(false, "POST", info, headers, params, null);

        return workflowLoadBalancerClient.execute(serviceId, request);
    }

    public String startProcess(Map<String, Object> data, String processModelKey) throws Exception {
        //构建request
        Map<String, String> headers = null;
        Map<String, Object> params = data;
        Map<String, String> info = new HashMap<>();
        info.put("action", GlobalContext.ACTION_ACTIVITISERVICE_STARTPROCESS);
        info.put("url", GlobalContext.URL_ACTIVITISERVICE_STARTPROCESS);
        info.put("processModelKey", processModelKey);
        WorkflowLoadBalancerRequest request = new WorkflowLoadBalancerRequest(false, "POST", info , headers, data, null);

        String responseString =  workflowLoadBalancerClient.execute(serviceId, request);
        logger.info("startProcess: " + responseString);
        return responseString;
    }

    public String getCurrentTasks(String processInstanceId) throws  Exception {
        //构建request
        Map<String, String> headers = null;
        Map<String, Object> params = null;
        Map<String, String> info = new HashMap<>();
        info.put("action", GlobalContext.ACTION_ACTIVITISERVICE_GETCURRENTTASKS);
        info.put("url", GlobalContext.URL_ACTIVITISERVICE_GETCURRENTTASKS);
        info.put("processInstanceId", processInstanceId);
        WorkflowLoadBalancerRequest workflowLoadBalancerRequest = new WorkflowLoadBalancerRequest(false, "GET", info, headers, params, null);
        String responseString = workflowLoadBalancerClient.execute(serviceId, workflowLoadBalancerRequest);
        return responseString;
    }

    public String getCurrentSingleTask(String processInstanceId) throws Exception {
        //构建request
        Map<String, String> headers = null;
        Map<String, Object> params = null;
        Map<String, String> info = new HashMap<>();
        info.put("action", GlobalContext.ACTION_ACTIVITISERVICE_GETCURRENTSINGLETASK);
        info.put("url", GlobalContext.URL_ACTIVITISERVICE_GETCURRENTSINGLETASK);
        info.put("processInstanceId", processInstanceId);
        WorkflowLoadBalancerRequest workflowLoadBalancerRequest = new WorkflowLoadBalancerRequest(false, "GET", info , headers, params, null);
        String responseString = workflowLoadBalancerClient.execute(serviceId, workflowLoadBalancerRequest);
        return responseString;
    }

    public String getCurrentTasksOfAssignee(String assignee, String processInstanceId) throws Exception {
        //构建request
        String urlWithoutServerInfo = GlobalContext.URL_ACTIVITISERVICE_GETCURRENTTASKSOFASSIGNEE + "/" + processInstanceId;
        Map<String, String> headers = null;
        Map<String, Object> params = new HashMap<>();
        params.put("assignee", assignee);
        Map<String, String>  info = new HashMap<>();
        info.put("action", GlobalContext.ACTION_ACTIVITISERVICE_GETCURRENTTASKSOFASSIGNEE);
        info.put("url", GlobalContext.URL_ACTIVITISERVICE_GETCURRENTTASKSOFASSIGNEE);
        info.put("processInstanceId", processInstanceId);
        WorkflowLoadBalancerRequest workflowLoadBalancerRequest = new WorkflowLoadBalancerRequest(false, "GET", info, headers, params, null);
        String responseString = workflowLoadBalancerClient.execute(serviceId, workflowLoadBalancerRequest);
        return responseString;
    }

    public String claimTask(String assignee, String processInstanceId, String taskId) throws Exception {
        //构建request
        String urlWithoutServerInfo = GlobalContext.URL_ACTIVITISERVICE_CLAIMTASK + "/" + processInstanceId + "/" + taskId;
        Map<String, String> headers = null;
        Map<String, Object> params = new HashMap<>();
        params.put("assignee", assignee);
        Map<String,String> info = new HashMap<>();
        info.put("action", GlobalContext.ACTION_ACTIVITISERVICE_CLAIMTASK);
        info.put("url", GlobalContext.URL_ACTIVITISERVICE_CLAIMTASK);
        info.put("processInstanceId", processInstanceId);
        info.put("taskId", taskId);

        WorkflowLoadBalancerRequest workflowLoadBalancerRequest = new WorkflowLoadBalancerRequest(false, "POST", info, headers, params, null);
        String responseString = workflowLoadBalancerClient.execute(serviceId, workflowLoadBalancerRequest);
        return responseString;
    }

    public String completeTask(Map<String, Object> data, String processInstanceId, String taskId) throws Exception  {
        //构建request
        Map<String, String> headers = null;
        Map<String, Object> params = data;
        Map<String, String> info = new HashMap<>();
        info.put("action", GlobalContext.ACTION_ACTIVITISERVICE_COMPLETETASK);
        info.put("url", GlobalContext.URL_ACTIVITISERVICE_COMPLETETASK);
        info.put("processInstanceId", processInstanceId);
        info.put("taskId", taskId);

        WorkflowLoadBalancerRequest workflowLoadBalancerRequest = new WorkflowLoadBalancerRequest(false, "POST", info, headers, params, null);
        String responseString = workflowLoadBalancerClient.execute(serviceId, workflowLoadBalancerRequest);
        return responseString;
    }

}
