package org.sysu.nameservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sysu.nameservice.service.ActivitiService;
import org.sysu.nameservice.util.CommonUtil;

import javax.xml.ws.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ActivitiController {
    @Autowired
    ActivitiService activitiService;



    @RequestMapping(value = "startProcess/{processModelKey}", method = RequestMethod.POST)
    public ResponseEntity<?> startProcess(@RequestBody(required = false)Map<String, Object> data,
                                          @PathVariable(value = "processModelKey", required = false) String processModelKey) {
        HashMap<String, String> response = new HashMap<>();

        //做参数校验
        ArrayList<String> missingParams = new ArrayList<>();
        if(data == null) missingParams.add("data");
        if(processModelKey == null) missingParams.add("processModelKey");
        if(missingParams.size() > 0) {
            response.put("status", "fail");
            response.put("message", "required parameters missing: " + CommonUtil.ArrayList2String(missingParams, " "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        try {
            String responseString = activitiService.startProcess(data, processModelKey);
            response.put("status", "success");
            response.put("response", responseString);
        } catch (Exception e) {
            response.put("status", "fail");
            response.put("message", e.toString());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @RequestMapping(value = "getCurrentTasks/{processInstanceId}", method = RequestMethod.GET)
    public ResponseEntity<?> getCurrentTasks(@PathVariable(value = "processInstanceId", required = false) String processInstanceId) {
        HashMap<String,String> response = new HashMap<>();

        //参数检查
        ArrayList<String> missingParams = new ArrayList<>();
        if(processInstanceId == null) missingParams.add("processInstanceId");
        if(missingParams.size() > 0) {
            response.put("status", "fail");
            response.put("message", "required parameters missing: " + CommonUtil.ArrayList2String(missingParams, " "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            String responseString = activitiService.getCurrentTasks(processInstanceId);
            response.put("status", "success");
            response.put("response", responseString);
        } catch (Exception e) {
            response.put("status", "fail");
            response.put("message", e.toString());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

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
        try {
            String responseString = activitiService.getCurrentTasksOfAssignee(assignee, processInstanceId);
            response.put("status", "success");
            response.put("response", responseString);
        } catch (Exception e) {
            response.put("status", "fail");
            response.put("message", e.toString());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @RequestMapping(value = "claimTask/{taskId}", method = RequestMethod.POST)
    public ResponseEntity<?> claimTask(@RequestBody(required = false) Map<String, Object> data,
                                       @PathVariable(value = "taskId", required = false) String taskId) {
        HashMap<String, String> response = new HashMap<>();
        //参数校验
        ArrayList<String> missingParams = new ArrayList<>();
        if(taskId == null) missingParams.add("taskId");
        if(data == null) missingParams.add("data");
        if(missingParams.size() > 0) {
            response.put("status", "fail");
            response.put("message", "required parameters missing: " + CommonUtil.ArrayList2String(missingParams, " "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        try {
            String responseString = activitiService.claimTask(data, taskId);
            response.put("status", "success");
            response.put("response", responseString);
        } catch (Exception e) {
            response.put("status", "fail");
            response.put("message", e.toString());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @RequestMapping(value = "completeTask/{taskId}", method = RequestMethod.POST)
    public ResponseEntity<?> completeTask(@RequestBody(required = false) Map<String, Object> data, @PathVariable(value = "taskId", required = false) String taskId) {
        HashMap<String, String> response = new HashMap<>();

        //校验参数
        ArrayList<String> missingParams = new ArrayList<>();
        if(data == null) missingParams.add("daa");
        if(taskId == null) missingParams.add("taskId");
        if(missingParams.size() > 0) {
            response.put("status", "fail");
            response.put("message", "required parameters missing: " + CommonUtil.ArrayList2String(missingParams, " "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        try {
            String responseString = activitiService.completeTask(data,taskId);
            response.put("status", "success");
            response.put("response", responseString);
        } catch (Exception e) {
            response.put("status", "fail");
            response.put("message", e.toString());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
