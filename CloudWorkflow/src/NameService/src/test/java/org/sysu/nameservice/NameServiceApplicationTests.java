package org.sysu.nameservice;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.sysu.nameservice.service.ActivitiService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NameServiceApplicationTests {
    @Autowired
    ActivitiService activitiService;

    @Test
    public void contextLoads() throws Exception {
//        System.out.println(activitiService.getHelloworld());
//        System.out.println(activitiService.getHelloworld());
//        System.out.println(activitiService.getHelloworld());
//        System.out.println(activitiService.getHelloworld());
//        System.out.println(activitiService.getHelloworld());
//        System.out.println(activitiService.getHelloworld());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void contextLoads_simulationTravelBooking() throws Exception {
        //验证是否有加载
//        long count = repositoryService.createProcessDefinitionQuery().count();
//        System.out.println(count);
//
        //参数设定
        String traveler = "Mike";
        String hotel = "1";
        String flight = "0";
        String car = "1";
        simulationTravelBooking(traveler, hotel, flight, car);
    }

    @SuppressWarnings("unchecked")
    private void simulationTravelBooking(String traveler, String hotel, String flight, String car) throws Exception {
        //
//        //启动流程:
        Map<String, Object> variables = new HashMap<String, Object>();
        Map<String, Object> subVariables = new HashMap<String, Object>();
        String processModelKey = "travel-booking";
//
        String responseString;
        Map<String, String> response;
        responseString = activitiService.startProcess(variables, processModelKey);
        response = JSON.parseObject(responseString, Map.class);
        System.out.println("startProcess: " + responseString);

        String processInstanceId = response.get("processInstanceId");
        //完成第一步：register
        responseString = activitiService.getCurrentSingleTask(processInstanceId);
        response = JSON.parseObject(responseString, Map.class);
        System.out.println("getCurrentSingleTask: " + responseString);
        String registerTaskId = response.get("taskId");
        responseString = activitiService.claimTask(traveler, processInstanceId, registerTaskId);
        System.out.println(responseString);

        responseString = activitiService.getCurrentTasksOfAssignee(traveler,processInstanceId);
        System.out.println("getCurrentTasksOfAssignee: " +  responseString);
        response = JSON.parseObject(responseString, Map.class);
        List<String> taskIds = JSON.parseArray(response.get("taskIds"), String.class);
        System.out.println(taskIds);
//        // -- traveler完成任务
        for(String tempTaskId : taskIds) {
//            System.out.println("Task for " + task.getAssignee() + ": " + task.getName());
            responseString = activitiService.completeTask(variables, processInstanceId, tempTaskId);
            System.out.println(responseString);
        }

        //进入子流程
        // -- 完成子流程第一步register
        System.out.println("进入子流程");
        responseString = activitiService.getCurrentSingleTask(processInstanceId);
        response = JSON.parseObject(responseString, Map.class);
        String registerItineraryTaskId = response.get("taskId");
        System.out.println("getCurrentSingleTask: " + responseString);
        // -- -- traveler 认领任务
//        taskService.claim(registerItineraryTask.getId(), traveler);
        responseString = activitiService.claimTask(traveler, processInstanceId, registerItineraryTaskId);
        System.out.println("claimTask: " + responseString);
//        // -- -- 完成任务
//        tasks = taskService.createTaskQuery().taskAssignee(traveler).list();
        responseString = activitiService.getCurrentTasksOfAssignee(traveler,processInstanceId);
        response = JSON.parseObject(responseString, Map.class);
        taskIds = JSON.parseArray(response.get("taskIds"), String.class);
        System.out.println(responseString);
        subVariables.put("hotel", hotel);
        subVariables.put("car", car);
        subVariables.put("flight", flight);
        for(String tempTaskId : taskIds) {
//            System.out.println("Task for " + task.getAssignee() + ": " + task.getName());
//            taskService.complete(task.getId(), subVariables);
            responseString = activitiService.completeTask(subVariables, processInstanceId, tempTaskId);
            System.out.println("网关任务数：" + responseString);
        }
        // -- 子流程第二步：book
        responseString = activitiService.getCurrentTasks(processInstanceId);
        response = JSON.parseObject(responseString, Map.class);
        taskIds = JSON.parseArray(response.get("taskIds"), String.class);
        System.out.println("包容网关通过的数量：" + taskIds.size());
//        // -- -- 认领多个任务，包容网关
        for(String tempTaskId : taskIds) {
            responseString = activitiService.claimTask(traveler,processInstanceId, tempTaskId);
            System.out.println(responseString);
        }
        // -- -- 完成子流程任务
        responseString = activitiService.getCurrentTasksOfAssignee(traveler, processInstanceId);
        response = JSON.parseObject(responseString, Map.class);
        taskIds = JSON.parseArray(response.get("taskIds"), String.class);
        // -- traveler完成任务
        for(String tempTaskId : taskIds) {
//            System.out.println("Task for " + task.getAssignee() + ": " + task.getName());
            responseString = activitiService.completeTask(variables, processInstanceId, tempTaskId);
            System.out.println(responseString);
        }
//
//        // -- 完成子流程第三步：prepare pay
        responseString = activitiService.getCurrentSingleTask(processInstanceId);
        response = JSON.parseObject(responseString, Map.class);
        String preparePayTaskId = response.get("taskId");

        // -- -- 认领prepare pay
        responseString = activitiService.claimTask(traveler, processInstanceId, preparePayTaskId);
        System.out.println(responseString);
        // -- -- 完成prepare pay
        responseString = activitiService.getCurrentTasksOfAssignee(traveler, processInstanceId);
        response = JSON.parseObject(responseString, Map.class);
        taskIds = JSON.parseArray(response.get("taskIds"), String.class);
//        // -- traveler完成任务
        for(String tempTaskId : taskIds) {
            responseString = activitiService.completeTask(variables, processInstanceId, tempTaskId);
            System.out.println(responseString);
        }

        //完成主流程pay任务
        responseString = activitiService.getCurrentSingleTask(processInstanceId);
        response = JSON.parseObject(responseString, Map.class);
        String payTaskId = response.get("taskId");
        //认领
        responseString = activitiService.claimTask(traveler, processInstanceId, payTaskId);
        System.out.println(responseString);
        // -- -- 完成prepare pay
        responseString = activitiService.getCurrentTasksOfAssignee(traveler, processInstanceId);
        response = JSON.parseObject(responseString, Map.class);
        taskIds = JSON.parseArray(response.get("taskIds"), String.class);
//        // -- traveler完成任务
        for(String tempTaskId : taskIds) {
            responseString = activitiService.completeTask(variables, processInstanceId, tempTaskId);
            System.out.println("prepay: " + responseString);
        }

//        //判断是否完成
//        System.out.println(historyService.createHistoricProcessInstanceQuery().finished().count());
    }
}
