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

/**
 * UpdateHeadersTask.java
 *
 * A RunnableTask object that will retrieve headers for a given
 * group from a given server and cache them to disk.
 *
 * This class will generate a HeadersUpdatedEvent on completion
 * OR
 * a HeadersUpdateErrorEvent is an Exception is generated
 *
 */

package net.nexxus.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.nexxus.db.DBManager;
import net.nexxus.event.EventListenerImpl;
import net.nexxus.event.HeadersUpdateErrorEvent;
import net.nexxus.event.HeadersUpdatedEvent;
import net.nexxus.gui.task.TaskInfoPanel;
import net.nexxus.nntp.NntpClient;
import net.nexxus.nntp.NntpException;
import net.nexxus.nntp.NntpGroup;
import net.nexxus.nntp.NntpServer;

public class UpdateHeadersTask extends EventListenerImpl implements RunnableTask {

    public String errorMsg = "";

    private NntpGroup group;
    private NntpServer server;
    private NntpClient client;
    private DBManager dbManager;
    private int taskID;
    private static int WAIT_TIME = 30000;
    private static Logger log = LogManager.getLogger(UpdateHeadersTask.class.getName());
    
    
    public UpdateHeadersTask(DBManager dbManager, NntpClient client, NntpGroup group, NntpServer server) {
        this.group = group;
        this.server = server;
        this.client = client;
        this.dbManager = dbManager;
    }
    
    public Object getSource() { 
        return this.group; 
    }
    
    public int getTaskID() { 
        return this.taskID; 
    }
    
    public void setTaskID(int id) { 
        this.taskID = id; 
    }
    
    public void cancel() {
        try {
            client.disconnect();
        } 
        catch (Exception e) {
            // do nothing
        }
        return;
    }
    
    // main
    public void run() {
        try {
            // addListener here
            TaskInfoPanel.getInstance().registerTask( Thread.currentThread().getName(), this );
            client.connect(server);
            log.debug("about to collect headers, my group has range of: " +
                    group.getLowID() + " - " + group.getHighID());
            log.info("downloading headers for " +  group.getName());
            
            // before we call getHeaders, ensure that our group object knows
            // the real low and high article ID values from the persistent store
            this.group = dbManager.getGroupMinMax(group);
            
            client.getHeaders(group);
            client.disconnect();
            
            log.debug("firing HeadersUpdatedEvent");
            HeadersUpdatedEvent ev = new HeadersUpdatedEvent(this);
            fireEvent(ev);
            log.debug("done collecting headers, my group has range of: " +
                    group.getLowID() + " - " + group.getHighID());
        } 
        catch (Exception e) {
            e.printStackTrace();
            log.warn("failed retrieving headers for " + group.getName() + " " + e.toString());
            errorMsg = e.getMessage(); // expose via method?
            try { 
                client.disconnect(); 
            } 
            catch (NntpException ne) {  // this is not always needed
                log.warn("failed disconnecting: " + ne.toString());
            }
            // give it time to sort itself out
            try { 
                Thread.sleep(WAIT_TIME); 
            } 
            catch (Exception eee) {
                // do nothing
            }
            HeadersUpdateErrorEvent erv = new HeadersUpdateErrorEvent(this);
            fireEvent(erv);
        } 
        catch(Error err) {
            err.printStackTrace();
            log.error("failed updating group: " + group.getName());
            log.error(err.toString());
        } 
        finally {
            client = null;
        }
        
        log.debug("exiting Runnable");
    }
}
