package org.sysu.activitiservice;


import net.sourceforge.sizeof.SizeOf;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ActivitiServiceApplicationTests {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RepositoryService repositoryService;

    @Test
    public void contextLoads() {
        System.out.println("test");
    }

    @Test
    public void testLeave() {
        //验证是否有加载到processes下面的流程文件
        long count = repositoryService.createProcessDefinitionQuery().count();
        System.out.println(count);

        //启动流程leave
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("apply", "zhangsan");
        variables.put("approve", "lisi");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("leave", variables);
        ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("leave", variables);
        ExecutionEntity executionEntity = (ExecutionEntity) processInstance;
        ExecutionEntity executionEntity1 = (ExecutionEntity) processInstance1;
        System.out.println(executionEntity.getActivity());
        System.out.println(executionEntity1.getActivity());


        System.out.println(SizeOf.humanReadable(SizeOf.deepSizeOf(executionEntity.getActivity())));
        System.out.println(SizeOf.humanReadable(SizeOf.deepSizeOf(variables)));
        System.out.println(SizeOf.humanReadable(SizeOf.deepSizeOf(new Integer(0))));
        System.out.println(SizeOf.humanReadable(SizeOf.deepSizeOf(new Object())));

        if(executionEntity.getActivity() == executionEntity1.getActivity()) {
            System.out.println("equal");
        } else {
            System.out.println("no equal");
        }


        //完成第一步申请
        String processId = processInstance.getId();
        System.out.println("processId: " + processId);
        Task task1 = taskService.createTaskQuery().processInstanceId(processId).singleResult();
        System.out.println("task1_Id: " + task1.getId());
        System.out.println("task1_Name: " + task1.getName());
        taskService.complete(task1.getId(), variables);

        //完成第二步请求
        Task task2 = taskService.createTaskQuery().processInstanceId(processId).singleResult();
        variables.put("pass", true);
        taskService.complete(task2.getId(), variables);


        System.out.println("完成数：" + historyService.createHistoricProcessInstanceQuery().finished().count());
    }

    //multi instance如何设置的问题，先用instance 为1 就可以了
    //有点问题，需要调试
    @Test
    public void testTravelBooking() {
        //验证是否有加载
        long count = repositoryService.createProcessDefinitionQuery().count();
        System.out.println(count);

        //参数设定
        String traveler = "Mike";
        String hotel = "1";
        String flight = "0";
        String car = "1";

        //启动流程:
        Map<String, Object> variables = new HashMap<String, Object>();
        Map<String, Object> subVariables = new HashMap<String, Object>();

        ProcessInstance pi = runtimeService.startProcessInstanceByKey("travel-booking", variables);
        System.out.println(pi);
        //完成第一步：register
        Task registerTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
        System.out.println(registerTask.getName());
        // -- traveler认领任务
        taskService.claim(registerTask.getId(), traveler);
        System.out.println("taskId:" + registerTask.getId());
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(traveler).list();
        // -- traveler完成任务
        for(Task task : tasks) {
            System.out.println("Task for " + task.getAssignee() + ": " + task.getName());
            taskService.complete(task.getId());
        }

        //进入子流程
        // -- 完成子流程第一步register
         Task registerItineraryTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
        // -- -- traveler 认领任务
        taskService.claim(registerItineraryTask.getId(), traveler);
        // -- -- 完成任务
        tasks = taskService.createTaskQuery().taskAssignee(traveler).list();
        subVariables.put("hotel", hotel);
        subVariables.put("car", car);
        subVariables.put("flight", flight);
        for(Task task : tasks) {
            System.out.println("Task for " + task.getAssignee() + ": " + task.getName());
            taskService.complete(task.getId(), subVariables);
        }
        // -- 子流程第二步：book
        tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
        System.out.println("包容网关通过的数量：" + tasks.size());
        // -- -- 认领多个任务，包容网关
        for(Task task : tasks) {
            System.out.println("Task for " + traveler + ": " + task.getName());
            taskService.claim(task.getId(), traveler);
        }
        // -- -- 完成子流程任务
        tasks = taskService.createTaskQuery().taskAssignee(traveler).list();
        // -- traveler完成任务
        for(Task task : tasks) {
            System.out.println("Task for " + task.getAssignee() + ": " + task.getName());
            taskService.complete(task.getId());
        }

        // -- 完成子流程第三步：prepare pay
        Task preparePayTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
        // -- -- 认领prepare pay
        taskService.claim(preparePayTask.getId(), traveler);
        // -- -- 完成prepare pay
        tasks = taskService.createTaskQuery().taskAssignee(traveler).list();
        // -- traveler完成任务
        for(Task task : tasks) {
            System.out.println("Task for " + task.getAssignee() + ": " + task.getName());
            taskService.complete(task.getId());
        }

        //完成主流程pay任务
        Task payTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
        // -- -- 认领prepare pay
        taskService.claim(payTask.getId(), traveler);
        // -- -- 完成prepare pay
        tasks = taskService.createTaskQuery().taskAssignee(traveler).list();
        // -- traveler完成任务
        for(Task task : tasks) {
            System.out.println("Task for " + task.getAssignee() + ": " + task.getName());
            taskService.complete(task.getId());
        }

        //判断是否完成
        System.out.println(historyService.createHistoricProcessInstanceQuery().finished().count());
    }
}
