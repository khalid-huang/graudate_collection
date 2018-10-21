package org.sysu.nameservice.Entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "processInstanceRouting")
public class ProcessInstanceRoutingEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activitiProcessInstanceId")
    private String activitiProcessInstanceId;

    @Column(name = "serverStr")
    private String serverStr;

    @Column(name = "startTime")
    private Date startTime;

    @Column(name = "finishTime")
    private Date finishTime;

    @Column(name = "processModelKey")
    private String processModelKey;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getActivitiProcessInstanceId() {
        return activitiProcessInstanceId;
    }

    public void setActivitiProcessInstanceId(String activitiProcessInstanceId) {
        this.activitiProcessInstanceId = activitiProcessInstanceId;
    }

    public String getServerStr() {
        return serverStr;
    }

    public void setServerStr(String serverStr) {
        this.serverStr = serverStr;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public String getProcessModelKey() {
        return processModelKey;
    }

    public void setProcessModelKey(String processModelKey) {
        this.processModelKey = processModelKey;
    }
}
