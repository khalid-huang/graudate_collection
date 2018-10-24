package org.sysu.nameservice.org.sysu.nameservice.simulation;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.sysu.nameservice.service.ActivitiService;

import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * @author: Gordan Lin
 * @create: 2018/10/22
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class TravelBookingJob implements Job {

    @Autowired
    ActivitiService activitiService;

    private Random random = new Random();

    static final CountDownLatch INSTANCE_COUNT = new CountDownLatch(300);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        //随机产生流程路径
        String traveler = "Mike";
        String hotel = "0";
        String flight = "0";
        String car = "0";
        while (hotel.equals("0") && flight.equals("0") && car.equals("0")) {
            hotel = String.valueOf(random.nextInt(2));
            flight = String.valueOf(random.nextInt(2));
            car = String.valueOf(random.nextInt(2));
        }
        ActivitiService activitiService = (ActivitiService) context.getJobDetail().getJobDataMap().get("service");
        try {
            simulationTravelBooking(traveler, hotel, flight, car, activitiService);
            INSTANCE_COUNT.countDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void schedulerJob() throws SchedulerException, InterruptedException {
        // 创建调度器
        SchedulerFactory schedulerFactory = new StdSchedulerFactory("quartz.properties");
        Scheduler scheduler = schedulerFactory.getScheduler();

        // 创建任务
        JobDetail jobDetail = JobBuilder.newJob(TravelBookingJob.class).withIdentity("job1", "group1").build();
        jobDetail.getJobDataMap().put("service", activitiService);

        // 创建触发器 每1.5秒钟执行一次 共执行300次
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity("trigger1", "group1")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMilliseconds(1500).withRepeatCount(300)).build();

        // 将任务及其触发器放入调度器
        scheduler.scheduleJob(jobDetail, trigger);

        // 调度器开始调度任务
        scheduler.start();
        INSTANCE_COUNT.await();
    }

    private void simulationTravelBooking(String traveler, String hotel, String flight, String car, ActivitiService activitiService) throws Exception {
//      启动流程:
        Map<String, Object> variables = new HashMap<String, Object>();
        Map<String, Object> subVariables = new HashMap<String, Object>();
        String processModelKey = "travel-booking";

        String responseString;
        Map<String, String> response;
        responseString = activitiService.startProcess(variables, processModelKey);
        response = JSON.parseObject(responseString, Map.class);
        System.out.println("startProcess: " + responseString);
        Thread.sleep(2000);

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
//      traveler完成任务
        for(String tempTaskId : taskIds) {
//            System.out.println("Task for " + task.getAssignee() + ": " + task.getName());
            responseString = activitiService.completeTask(variables, processInstanceId, tempTaskId);
            System.out.println(responseString);
        }
        Thread.sleep(2000);

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
        Thread.sleep(2000);

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
        Thread.sleep(2000);
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
        Thread.sleep(2000);

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
