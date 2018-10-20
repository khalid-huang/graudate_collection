package org.sysu.activitiservice.controller;

import com.alibaba.fastjson.JSON;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sysu.activitiservice.service.ActivitiService;
import org.sysu.activitiservice.util.CommonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@SuppressWarnings("unchecked")
public class ActivitiController {
    private final static Logger logger = LoggerFactory.getLogger(ActivitiController.class);

    @Autowired
    private ActivitiService activitiService;

    @Autowired
    private TaskService taskService;

    //启动指定流程
    @RequestMapping(value = "/startProcess/{processModelKey}", method = RequestMethod.POST)
    public ResponseEntity<?> startProcess(@RequestBody(required = false) Map<String, Object> variables,
                                          @PathVariable(value = "processModelKey", required = false) String processModelKey) {

        HashMap<String, String> response = new HashMap<>();

        //做参数校验
        ArrayList<String> missingParams = new ArrayList<>();
        if(variables == null) missingParams.add("variables");
        if(processModelKey == null) missingParams.add("processModelKey");
        if(missingParams.size() > 0) {
            response.put("status", "fail");
            response.put("message", "required parameters missing: " + CommonUtil.ArrayList2String(missingParams, " "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(JSON.toJSONString(response));
        }

        //启动流程
//        ProcessInstance pi =  runtimeService.startProcessInstanceByKey(processModelKey, variables);
        ProcessInstance pi =  activitiService.startProcessInstanceByKey(processModelKey, variables);
        response.put("status", "success");
        response.put("message", "start process " + processModelKey + " success");
        response.put("processInstanceId", pi.getId());
        logger.info(response.toString());

        return ResponseEntity.status(HttpStatus.OK).body(JSON.toJSONString(response));
    }

    //获取指定流程的当前任务列表
    @RequestMapping(value = "getCurrentTasks/{processInstanceId}", method = RequestMethod.GET)
    public ResponseEntity<?> getCurrentTasks(@PathVariable(value = "processInstanceId", required = false) String processInstanceId) {
        HashMap<String, String> response = new HashMap<>();

        //校验参数
        ArrayList<String> missingParams = new ArrayList<>();
        if(processInstanceId == null) missingParams.add("processInstanceId");
        if(missingParams.size() > 0) {
            response.put("status", "fail");
            response.put("message", "required parameters missing: " + CommonUtil.ArrayList2String(missingParams, " "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(JSON.toJSONString(response));
        }

        //获取列表
//        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        List<Task> tasks = activitiService.getCurrentTask(processInstanceId);
        List<String> taskIds = new ArrayList<>();
        for(Task task : tasks) {
            taskIds.add(task.getId());
        }
        response.put("status", "success");
        response.put("message", "get task list of processInstanceId of " + processInstanceId + " success");
        response.put("taskIds", taskIds.toString());
        logger.info(response.toString());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //获取指定流程的指定用户的任务列表
    @RequestMapping(value = "getCurrentTasksOfAssignee/{processInstanceId}", method = RequestMethod.GET)
    public ResponseEntity<?> getCurrentTasksOfAssignee(@RequestParam(value = "assignee", required = false) String assignee,
                                                       @PathVariable(value = "processInstanceId", required = false) String processInstanceId) {
        HashMap<String, String> response = new HashMap<>();

        //校验参数
        ArrayList<String> missingParams = new ArrayList<>();
        if(processInstanceId == null) missingParams.add("pid");
        if(assignee == null) missingParams.add("assignee");
        if(missingParams.size() > 0) {
            response.put("status", "fail");
            response.put("message", "required parameters missing: " + CommonUtil.ArrayList2String(missingParams, " "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(JSON.toJSONString(response));
        }

        //获取列表
//        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).taskAssignee(assignee).list();
        List<Task> tasks = activitiService.getCurrentTask(processInstanceId, assignee);
        List<String> taskIds = new ArrayList<>();
        for(Task task : tasks) {
            taskIds.add(task.getId());
        }
        response.put("status", "success");
        response.put("message", "get " + assignee + "'s task list of processInstanceId of " + processInstanceId + " success");
        response.put("taskIds", taskIds.toString());
        logger.info(response.toString());
        return ResponseEntity.status(HttpStatus.OK).body(JSON.toJSONString(response));
    }

    //claim任务
    @RequestMapping(value = "claimTask/{processInstanceId}/{taskId}", method = RequestMethod.POST)
    public ResponseEntity<?> claimTask(@RequestBody(required = false) Map<String, Object> data,
                                       @PathVariable(value = "processInstanceId", required = false) String processInstanceId,
                                       @PathVariable(value = "taskId", required = false) String taskId) {
        //取出参数
        String assignee = (String) data.get("assignee");

        HashMap<String, String> response = new HashMap<>();
        //参数校验
        ArrayList<String> missingParams = new ArrayList<>();
        if(taskId == null) missingParams.add("taskId");
        if(processInstanceId == null) missingParams.add("processInstanceId");
        if(assignee == null) missingParams.add("assignee");
        if(missingParams.size() > 0) {
            response.put("status", "fail");
            response.put("message", "required parameters missing: " + CommonUtil.ArrayList2String(missingParams, " "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(JSON.toJSONString(response));
        }

        //认领任务
//        taskService.claim(taskId, assignee);
        activitiService.claimTask(taskId, assignee);
        response.put("status", "success");
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        response.put("message", "assignee " + assignee + " claim the task of " + taskId + " with taskName " + task.getName());
        return ResponseEntity.status(HttpStatus.OK).body(JSON.toJSONString(response));
    }

    //完成任务
    @RequestMapping(value = "completeTask/{processInstanceId}/{taskId}", method = RequestMethod.POST)
    public ResponseEntity<?> completeTask(@RequestBody(required = false) Map<String, Object> variables, @PathVariable(value = "processInstanceId", required = false) String processInstanceId ,@PathVariable(value = "taskId", required = false) String taskId) {

        HashMap<String, String> response = new HashMap<>();

        //校验参数
        ArrayList<String> missingParams = new ArrayList<>();
        if(variables == null) missingParams.add("variables");
        if(processInstanceId == null) missingParams.add("processInstanceId");
        if(taskId == null) missingParams.add("taskId");
        if(missingParams.size() > 0) {
            response.put("status", "fail");
            response.put("message", "required parameters missing: " + CommonUtil.ArrayList2String(missingParams, " "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(JSON.toJSONString(response));
        }

        //完成任务
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
//        taskService.complete(taskId, variables);
        Map<String, String> temp = activitiService.completeTask(processInstanceId, taskId, variables);
        response.put("status", "message");
        response.put("message", "complete task of taskId " + taskId + "with taskName" + task.getName());
        response.put("newWorkItemNumber", temp.get("newWorkItemNumber"));
        response.put("isEnded", activitiService.isEnded(processInstanceId) ? "1" : "0");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(JSON.toJSONString(response));
    }

}
