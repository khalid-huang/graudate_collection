package org.sysu.nameservice;

/**
 * Global data access
 */
public class GlobalContext {
    
    public static final String SERVICEID_ACTIVITISERVICE = "activiti-service";

    public static final String ACTION_ACTIVITISERVICE_STARTPROCESS = "startProcess";

    public static final String ACTION_ACTIVITISERVICE_CLAIMTASK = "claimTask";

    public static final String ACTION_ACTIVITISERVICE_COMPLETETASK = "completeTask";


    /**
     * Service URL for activiti-service start process
     */
    public static final String URL_ACTIVITISERVICE_STARTPROCESS="/startProcess";


    /**
     * Service URL for activiti-service get current tasks
     */
    public static final String URL_ACTIVITISERVICE_GETCURRENTTASKS="/getCurrentTasks";

    /**
     * Service URL for activiti-service get one assignee's current tasks of one processinstance
     */
    public static final String URL_ACTIVITISERVICE_GETCURRENTTASKSOFASSIGNEE="/getCurrentTasksOfAssignee";

    /**
     * Service URL for activiti-service claim task
     */
    public static final String URL_ACTIVITISERVICE_CLAIMTASK="/claimTask";

    /**
     * Service URL for activiti-service complete task
     */
    public static final String URL_ACTIVITISERVICE_COMPLETETASK="/completeTask";

}
