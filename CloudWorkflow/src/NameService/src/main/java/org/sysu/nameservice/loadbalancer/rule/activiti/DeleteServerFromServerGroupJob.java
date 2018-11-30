package org.sysu.nameservice.loadbalancer.rule.activiti;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.sysu.nameservice.loadbalancer.*;
import java.util.Map;
import java.util.Set;

/**
 * @author: Gordan Lin
 * @create: 2018/11/29
 **/
public class DeleteServerFromServerGroupJob implements Job {

    public DeleteServerFromServerGroupJob() {
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Map<String, ILoadBalancer> serviceIdToLoadBalancer = (Map<String, ILoadBalancer>) context.getJobDetail().getJobDataMap().get("serviceIdToLoadBalancer");
        Set<String> serviceIds = serviceIdToLoadBalancer.keySet();
        for (String serviceId : serviceIds) {
            BaseLoadBalancer baseLoadBalancer = (BaseLoadBalancer) serviceIdToLoadBalancer.get(serviceId);
            ActivitiLoadBalancerStats loadBalancerStats = (ActivitiLoadBalancerStats) baseLoadBalancer.getLoadBalancerStats();
            loadBalancerStats.checkRemoveServer();
        }
    }

    public void schedulerJob(Map<String, ILoadBalancer> serviceIdToLoadBalancer) throws SchedulerException, InterruptedException {
        // 创建调度器
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();

        // 创建任务
        JobDetail jobDetail = JobBuilder.newJob(DeleteServerFromServerGroupJob.class).withIdentity("job1", "group1").build();
        jobDetail.getJobDataMap().put("serviceIdToLoadBalancer", serviceIdToLoadBalancer);

        // 创建触发器 每5秒钟执行一次
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity("trigger1", "group1")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMilliseconds(5000).repeatForever()).build();

        // 将任务及其触发器放入调度器
        scheduler.scheduleJob(jobDetail, trigger);

        // 调度器开始调度任务
        scheduler.start();
    }

}
