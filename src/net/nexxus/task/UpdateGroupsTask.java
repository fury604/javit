/**
 * This file is part of Javit.
 *
 * Javit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Javit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Javit.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2005-2016 Richard Stride <fury@nexxus.net>
 */
package net.nexxus.task;

import java.util.Iterator;
import java.util.List;

//import javax.swing.event.EventListenerList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//import net.nexxus.gui.task.TaskInfoPanel;

import net.nexxus.nntp.NntpClient;
import net.nexxus.nntp.NntpClientV2;
import net.nexxus.nntp.NntpException;
import net.nexxus.nntp.NntpGroup;
import net.nexxus.nntp.NntpServer;
import net.nexxus.util.ComponentManager;
import net.nexxus.db.DBManager;
import net.nexxus.event.EventListenerImpl;
import net.nexxus.event.GroupsUpdateErrorEvent;
import net.nexxus.gui.groups.ServerNode;

public class UpdateGroupsTask extends EventListenerImpl implements RunnableTask {

	private static Logger log = LogManager.getLogger(UpdateHeadersTask.class.getName());
    private NntpClient client;
    private NntpServer server;
    private DBManager dbManager;
    private int taskID;

    public UpdateGroupsTask(DBManager dbManager, NntpClient client, NntpServer server) {
        this.dbManager = dbManager;
        this.client = client;
        this.server = server;
    }
    
    /////////////////////
    // Accessor methods
    /////////////////////
    public Object getSource() { 
        return server; 
    }
    public int getTaskID() { 
        return this.taskID; 
    }
    public void setTaskID(int id) { 
        this.taskID = id; 
    }
    public void cancel() {}

    /*
     * main method used as Thread payload
     */
    public void run() {
    	try {
            // add Listener hooks
            //TaskInfoPanel.getInstance().registerTask( Thread.currentThread().getName(), this );
            //client.addGUIEventListener(new GUIEventListener() {
            //    public void eventOccurred(GUIEvent event) {
                    //if (event instanceof NewGroupEvent) {}
            //    }
            //});
    	    client.connect(server);
    	    List<NntpGroup> groups = client.getGroupList(server);
    	    client.disconnect();
    	    
	        try {
	            dbManager.addServerGroups(groups);
	        }
	        catch (Exception e) {
	            log.error("failed inserting NntpGroup List into groups table: " + e.getMessage() );
	        }
    	    //fireEvent( new GroupsUpdatedEvent(server) );
    	    log.debug("UpdateGroupsTask done");
    	} 
        catch (NntpException ne) {
    	    log.error("failed collecting groups");
    	    fireEvent( new GroupsUpdateErrorEvent(server) );
    	}
    }

}
