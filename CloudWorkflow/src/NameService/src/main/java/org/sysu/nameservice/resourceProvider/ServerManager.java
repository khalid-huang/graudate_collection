package org.sysu.nameservice.resourceProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.sysu.nameservice.loadbalancer.Server;

import java.util.ArrayList;
import java.util.List;

/** 负责所有与server信息相关的管理，比如设置Server，关掉Server，但不去维护，先简易实现，因为目前的Server信息其实是维护在了每个serverId的BaseLoadBalancer里面，所以ServerManager*/
@Component
public class ServerManager implements IResourceManager<Server> {
    @Autowired
    private Environment environment;

    private static final String suffix = ".workflow.listOfServers";

    /** 通过ServericeId来获取Servers信息；格式是localhost:8800,localhsot:9999*/
    public String getServersInfoByServiceId(String serviceId) {
        return environment.getProperty(serviceId + suffix);
    }

    public List<Server> getServerListByServiceId(String serviceId) {
        String srvString = environment.getProperty(serviceId);
        ArrayList<Server> servers = new ArrayList<>();
        if(srvString != null) {
            String[] serverArr = srvString.split(",");
            for(String server : serverArr) {
                if(server != null) {
                    server = server.trim();
                    if(server.length() > 0) {
                        servers.add(new Server(server));
                    }
                }
            }
        }
        return servers;
    }

    /** 设置server的状态为不可用*/
    public void markServerDown(Server server) {
        if(server == null || !server.isAlive()) {
            return;
        } else {
            server.setAlive(false);
        }
    }

    /** 设置server 的状态为可用*/
    public void markServerUp(Server server) {
        if(server == null || server.isAlive()) {
            return;
        } else {
            server.setAlive(true);
        }
    }

}
