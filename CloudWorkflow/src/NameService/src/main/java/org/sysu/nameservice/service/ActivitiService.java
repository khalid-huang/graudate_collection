package org.sysu.nameservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.sysu.nameservice.GlobalContext;
import org.sysu.nameservice.loadbalancer.WorkflowLoadBalancerClient;
import org.sysu.nameservice.loadbalancer.WorkflowLoadBalancerRequest;

import java.io.IOException;
import java.util.Map;

@Service
public class ActivitiService {
    private static Logger logger = LoggerFactory.getLogger(ActivitiService.class);

    @Autowired
    WorkflowLoadBalancerClient workflowLoadBalancerClient;

    private static final String serviceId = GlobalContext.SERVICEID_ACTIVITISERVICE;

    public ResponseEntity<?> getHelloworld() {
        //主要目标是构建出request就可以了
        String urlWithoutServerInfo = "getHelloworld";
        WorkflowLoadBalancerRequest request = new WorkflowLoadBalancerRequest(false, "GET", urlWithoutServerInfo, null, null, null);
        try {
            String responseString = (String) workflowLoadBalancerClient.execute(serviceId, request);
            logger.info(responseString);
            return ResponseEntity.status(HttpStatus.OK).body(responseString);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.OK).body("can not connected");
        }
    }

    public ResponseEntity<?> helloworld(Map<String, Object> data) {
        //构建request
        //测试下在data下面放一个variables，variable下面放test，看看Activiti是否可以收到
        String urlWithoutServerInfo = "/hellowold";
        Map<String, String> headers = null;
        Map<String, Object> params = data;
        WorkflowLoadBalancerRequest request = new WorkflowLoadBalancerRequest(false, "POST", urlWithoutServerInfo, headers, params, null);

        try {
            String responseString =  workflowLoadBalancerClient.execute(serviceId, request);
            logger.info("helloworld" + responseString);
            return ResponseEntity.status(HttpStatus.OK).body(responseString);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.OK).body("连接不上");
        }
    }

    public ResponseEntity<?> startProcess(Map<String, Object> data, String processModelKey) {
        //构建request
        String urlWithoutServerInfo = GlobalContext.URL_ACTIVITISERVICE_STARTPROCESS + "/" + processModelKey;
        Map<String, String> headers = null;
        Map<String, Object> params = data;
        WorkflowLoadBalancerRequest request = new WorkflowLoadBalancerRequest(false, "POST", urlWithoutServerInfo, headers, data, null);

        try {
            String responseString =  workflowLoadBalancerClient.execute(serviceId, request);
            logger.info("startProcess: " + responseString);
            return ResponseEntity.status(HttpStatus.OK).body(responseString);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.OK).body("连接不上");
        }
    }

    public ResponseEntity<?> getCurrentTasks(String processInstanceId) {
        return null;
    }

    public ResponseEntity<?> getCurrentTasksOfAssignee(String assignee, String processInstanceId) {
        return null;
    }

    public ResponseEntity<?> claimTask(Map<String, Object> data, String taskId) {
        return null;
    }

    public ResponseEntity<?> completeTask(Map<String, Object> data, String taskId) {
        return null;
    }

}
