package kevin.activiti.activitiservice;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
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
    public void testLeave() {
        //验证是否有加载到processes下面的流程文件
        long count = repositoryService.createProcessDefinitionQuery().count();
        System.out.println(count);

        //启动流程leave
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("apply", "zhangsan");
        variables.put("approve", "lisi");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("leave", variables);

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


        System.out.println(historyService.createHistoricProcessInstanceQuery().finished().count());
    }

}
