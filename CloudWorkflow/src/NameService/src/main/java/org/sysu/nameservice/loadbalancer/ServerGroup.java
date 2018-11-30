package org.sysu.nameservice.loadbalancer;

import java.util.HashSet;
import java.util.Set;

/**
 * @author: Gordan Lin
 * @create: 2018/11/30
 **/
public class ServerGroup {

    private Set<Server> serverSets;

    private boolean modified;

    public ServerGroup() {
        this.serverSets = new HashSet<>();
        this.modified = false;
    }

    public Set<Server> getServerSets() {
        return serverSets;
    }

    public void addServerToServerSets(Server server) {
        if (serverSets == null) {
            serverSets = new HashSet<>();
        }
        serverSets.add(server);
        modified = true;
    }

    public void deleteServerFromServerSets(Server server) {
        if (serverSets != null) {
            serverSets.remove(server);
        }
    }

    public boolean getModified() {
        return modified;
    }

    public void setModified() {
        modified = false;
    }

}
