package org.sysu.nameservice;

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
        System.out.println(activitiService.getHelloworld());
        System.out.println(activitiService.getHelloworld());
        System.out.println(activitiService.getHelloworld());
        System.out.println(activitiService.getHelloworld());
        System.out.println(activitiService.getHelloworld());
        System.out.println(activitiService.getHelloworld());
    }

    @Test
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
//
//        //启动流程:
        Map<String, Object> variables = new HashMap<String, Object>();
        Map<String, Object> subVariables = new HashMap<String, Object>();
        String processModelKey = "travel-booking";
//
//        ProcessInstance pi = runtimeService.startProcessInstanceByKey("travel-booking", variables);
        String response;
        response = activitiService.startProcess(variables, processModelKey);
        System.out.println(response);

//        System.out.println(pi);
//        //完成第一步：register
//        Task registerTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
//        System.out.println(registerTask.getName());
//        // -- traveler认领任务
//        taskService.claim(registerTask.getId(), traveler);
//        System.out.println("taskId:" + registerTask.getId());
//        List<Task> tasks = taskService.createTaskQuery().taskAssignee(traveler).list();
//        // -- traveler完成任务
//        for(Task task : tasks) {
//            System.out.println("Task for " + task.getAssignee() + ": " + task.getName());
//            taskService.complete(task.getId());
//        }
//
//        //进入子流程
//        // -- 完成子流程第一步register
//        Task registerItineraryTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
//        // -- -- traveler 认领任务
//        taskService.claim(registerItineraryTask.getId(), traveler);
//        // -- -- 完成任务
//        tasks = taskService.createTaskQuery().taskAssignee(traveler).list();
//        subVariables.put("hotel", hotel);
//        subVariables.put("car", car);
//        subVariables.put("flight", flight);
//        for(Task task : tasks) {
//            System.out.println("Task for " + task.getAssignee() + ": " + task.getName());
//            taskService.complete(task.getId(), subVariables);
//        }
//        // -- 子流程第二步：book
//        tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
//        System.out.println("包容网关通过的数量：" + tasks.size());
//        // -- -- 认领多个任务，包容网关
//        for(Task task : tasks) {
//            System.out.println("Task for " + traveler + ": " + task.getName());
//            taskService.claim(task.getId(), traveler);
//        }
//        // -- -- 完成子流程任务
//        tasks = taskService.createTaskQuery().taskAssignee(traveler).list();
//        // -- traveler完成任务
//        for(Task task : tasks) {
//            System.out.println("Task for " + task.getAssignee() + ": " + task.getName());
//            taskService.complete(task.getId());
//        }
//
//        // -- 完成子流程第三步：prepare pay
//        Task preparePayTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
//        // -- -- 认领prepare pay
//        taskService.claim(preparePayTask.getId(), traveler);
//        // -- -- 完成prepare pay
//        tasks = taskService.createTaskQuery().taskAssignee(traveler).list();
//        // -- traveler完成任务
//        for(Task task : tasks) {
//            System.out.println("Task for " + task.getAssignee() + ": " + task.getName());
//            taskService.complete(task.getId());
//        }
//
//        //完成主流程pay任务
//        Task payTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
//        // -- -- 认领prepare pay
//        taskService.claim(payTask.getId(), traveler);
//        // -- -- 完成prepare pay
//        tasks = taskService.createTaskQuery().taskAssignee(traveler).list();
//        // -- traveler完成任务
//        for(Task task : tasks) {
//            System.out.println("Task for " + task.getAssignee() + ": " + task.getName());
//            taskService.complete(task.getId());
//        }
//
//        //判断是否完成
//        System.out.println(historyService.createHistoricProcessInstanceQuery().finished().count());
    }

}
