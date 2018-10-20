package org.sysu.activitiservice.service;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ActivitiService {
    private final static Logger logger = LoggerFactory.getLogger(ActivitiService.class);

    @Autowired
    TaskService service;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    TaskService taskService;

    public ProcessInstance startProcessInstanceByKey(String processModelKey, Map<String,Object> variables) {
        return runtimeService.startProcessInstanceByKey(processModelKey, variables);
    }

    /** 根据流程实例获取当前任务列表 */
    public List<Task> getCurrentTask(String processInstancedId) {
        return taskService.createTaskQuery().processInstanceId(processInstancedId).list();
    }

    /**根据流程实例和配置人，获取配置人的任务列表*/
    public List<Task> getCurrentTask(String processInstancedId, String assignee) {
        return taskService.createTaskQuery().processInstanceId(processInstancedId).taskAssignee(assignee).list();
    }

    /** 认领任务 */
    public void claimTask(String taskId, String assignee) {
        taskService.claim(taskId, assignee);
    }

    /**
     * 完成任务，并返回新启动的工作项
     * 新工作项的获取方式是对比前后的工作项列表的差值就可以了
     * @param taskId
     * @param variables
     * @return
     */
    public Map<String, String> completeTask(String processInstanceId,String taskId, Map<String, Object> variables) {
        Map<String, String> result = new HashMap<>();
        int beforeCompleteTaskNumber = getCurrentTask(processInstanceId).size();
        taskService.complete(taskId, variables);
        int afterCompleteTaskNumber = getCurrentTask(processInstanceId).size();
        result.put("newWorkItemNumber", String.valueOf(afterCompleteTaskNumber - (beforeCompleteTaskNumber - 1))); //beforeCompleteTaskNumber需要减少完成的任务；
        return result;

    }

    /**
     * 根据流程实例Id判断流程实例是否结束
     * @return
     */
    public boolean isEnded(String processInstanceId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        //如果事务提交之后，流程实例会被删除的，所以要判断
        if(processInstance != null) {
            return processInstance.isEnded();
        }
        return false;
    }




}
