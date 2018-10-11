package org.sysu.activitiservice.controller;

import com.alibaba.fastjson.JSON;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    //启动指定流程
    @RequestMapping(value = "/startProcess/{processModelKey}", method = RequestMethod.POST)
    public ResponseEntity<?> startProcess(@RequestBody(required = false) Map<String, Object> data,
                                          @PathVariable(value = "processModelKey", required = false) String processModelKey) {
        //取出参数
        //通过NameService的okhttp会使用JSON转变对象为string
        Map<String, Object> variables = JSON.parseObject((String) data.get("variables"), Map.class);

        HashMap<String, String> response = new HashMap<>();

        //做参数校验
        ArrayList<String> missingParams = new ArrayList<>();
        if(variables == null) missingParams.add("variables");
        if(processModelKey == null) missingParams.add("processModelKey");
        if(missingParams.size() > 0) {
            response.put("status", "fail");
            response.put("message", "required parameters missing: " + CommonUtil.ArrayList2String(missingParams, " "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        //启动流程
        ProcessInstance pi =  runtimeService.startProcessInstanceByKey(processModelKey, variables);
        response.put("status", "success");
        response.put("message", "start process " + processModelKey + " success");
        response.put("processId", pi.getId());
        logger.info(response.toString());

        return ResponseEntity.status(HttpStatus.OK).body(response);
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        //获取列表
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        //获取列表
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).taskAssignee(assignee).list();
        List<String> taskIds = new ArrayList<>();
        for(Task task : tasks) {
            taskIds.add(task.getId());
        }
        response.put("status", "success");
        response.put("message", "get " + assignee + "'s task list of processInstanceId of " + processInstanceId + " success");
        response.put("taskIds", taskIds.toString());
        logger.info(response.toString());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //claim任务
    @RequestMapping(value = "claimTask/{taskId}", method = RequestMethod.POST)
    public ResponseEntity<?> claimTask(@RequestBody(required = false) Map<String, Object> data,
                                       @PathVariable(value = "taskId", required = false) String taskId) {
        //取出参数
        String assignee = (String) data.get("assignee");

        HashMap<String, String> response = new HashMap<>();
        //参数校验
        ArrayList<String> missingParams = new ArrayList<>();
        if(taskId == null) missingParams.add("taskId");
        if(assignee == null) missingParams.add("assignee");
        if(missingParams.size() > 0) {
            response.put("status", "fail");
            response.put("message", "required parameters missing: " + CommonUtil.ArrayList2String(missingParams, " "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        //认领任务
        taskService.claim(taskId, assignee);
        response.put("status", "success");
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        response.put("message", "assignee " + assignee + " claim the task of " + taskId + " with taskName " + task.getName());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //完成任务
    @RequestMapping(value = "completeTask/{taskId}", method = RequestMethod.POST)
    public ResponseEntity<?> completeTask(@RequestBody(required = false) Map<String, Object> data, @PathVariable(value = "taskId", required = false) String taskId) {
        //取出参数
        Map<String, Object> variables = (Map<String, Object>) data.get("variables");

        HashMap<String, String> response = new HashMap<>();

        //校验参数
        ArrayList<String> missingParams = new ArrayList<>();
        if(variables == null) missingParams.add("variables");
        if(taskId == null) missingParams.add("taskId");
        if(missingParams.size() > 0) {
            response.put("status", "fail");
            response.put("message", "required parameters missing: " + CommonUtil.ArrayList2String(missingParams, " "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        //完成任务
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        taskService.complete(taskId, variables);
        response.put("status", "message");
        response.put("message", "complete task of taskId " + taskId + "with taskName" + task.getName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

}
