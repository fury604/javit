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

import java.util.ArrayList;
import java.util.Properties;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.event.EventListenerList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import net.nexxus.db.DBManager;
import net.nexxus.nntp.*;
import net.nexxus.event.*;
import net.nexxus.gui.task.TaskInfoPanel;
import net.nexxus.util.ApplicationConstants;
import net.nexxus.util.ComponentManager;


public class DownloadArticleTask extends EventListenerImpl implements RunnableTask {
    
    private NntpArticleHeader header;
    private NntpServer server;
    private NntpClient client;
    private ComponentManager componentManager = new ComponentManager();
    private DBManager dbManager;
    private Object task;
    private boolean interrupted = false;
    private EventListenerList listenerList = new EventListenerList();
    private int taskID;
    private static Logger log = LogManager.getLogger(DownloadArticleTask.class.getName());
    private String cacheDir = ApplicationConstants.CACHE_DIR;
    
    public String errorMsg = new String();
    
    public DownloadArticleTask(NntpArticleHeader header, NntpServer server) {
        this.header = header;
        this.server = server;
        this.dbManager = componentManager.getDBManager();
        this.client = new NntpClientV2(this.server, this.dbManager);
    }
    
    /////////////////////
    // Accessor methods
    /////////////////////
    public Object getSource() {
        return this.header;
    }

    public int getTaskID() {
        return this.taskID;
    }

    public void cancel() {
        interrupted = true;
    }

    public void setTaskID(int id) {
        this.taskID = id;
    }
    
    
    /**
     * this Task must have a means to be stopped
     * or interrupted.
     */
    public void run() {
        try {
            // add Listener hooks
            TaskInfoPanel.getInstance().registerTask(Thread.currentThread().getName(), this);
            client.connect(server);
            log.debug("header was: " + header.getGroup());
            // header.getGroup() is null for some reason
            NntpGroup group = new NntpGroup(server.getServer(), server.getPort(), header.getGroup(), 0);
            client.setGroup(group);
            header = dbManager.getHeader(group, header);
            
            // if Multipart retreive
            if (header.isMultipart() && header.isComplete()) {
                log.debug("downloading multipart article: " + header.getSubject());
                NntpArticlePartID[] ids = header.getParts();
                try {
                    int x = 0;
                    while (!interrupted && x < ids.length) {
                        NntpArticlePartID partID = ids[x];
                        File cachefile =
                                new File(cacheDir +
                                File.separator + header.getServer() + "." +
                                header.getGroup() + "." + partID.getIdAsString());
                        try {
                            cachefile.createNewFile();
                        } 
                        catch (IOException ioe) {
                            throw new NntpException();
                        }
                        
                        try {
                        	client.getArticleBody(partID, cachefile);
                        }
                        catch (Exception e) {
                        	log.warn("could not grab header");
                        }
                        fireEvent( new ArticlePartDownloadedEvent(x, ids.length-1) );
                        x++;
                    }
                    log.info("finished downloading article");
                    if ( !interrupted ) {
                        fireEvent(new ArticleDownloadedEvent(this));
                    }
                } 
                catch (NntpException ne) {
                    log.error("got exception retreiving multipart article: " + 
                            ne.getMessage());
                    handleException();
                    return;
                } 
                catch (NumberFormatException nfe) {
                    log.warn("got exception retreiving multipart article: " + 
                        nfe.toString());
                    handleException();
                    return;
                }
            }
            
            // if Singlepart retreive - typically smaller files here
            else if (! header.isMultipart()) {
                log.info("downloading singlepart article: " + header.getSubject());
                try {
                    File cachefile = new File(cacheDir + File.separator +
                            header.getServer() + "." +
                            header.getGroup() + "." +
                            header.getID());
                    try {
                        cachefile.createNewFile();
                    } 
                    catch (IOException ioe) {
                    	log.warn("cannot create cachefile");
                        throw new NntpException();
                    }
                    
                    NntpArticlePartID partID = new NntpArticlePartID(header.getID(), header.getMsgID());
                    client.getArticleBody(partID, cachefile);
                    //fireEvent(new ArticlePartDownloadedEvent(0, 0));
                    //fireEvent(new ArticleDownloadedEvent(this));
                } 
                catch (Exception ne) {
                    log.warn("got exception retreiving singlepart article: " 
                        + ne.getMessage());
                    handleException();
                    return;
                }
            } 
            else {
                log.warn("article is is not complete!");
                try { 
                    client.disconnect(); 
                } 
                catch (NntpException e) {
                }
                errorMsg = "article is is not complete!";
                fireEvent(new ArticleDownloadErrorEvent(this));
                return;
            }
            
            // if interrupted here
            if ( interrupted ) {
                log.debug("is interrupted");
                try { 
                    client.disconnect(); 
                } 
                catch (NntpException ne) {
                }
                cleanupCache(header);
                header.setStatus(header.STATUS_UNREAD);
                dbManager.updateHeader(header);
                fireEvent(new DownloadCanceledEvent(this));
                return;
            }
            
            client.disconnect();
            
        } // end of initial try, phew!
        catch (Exception e) {
            log.warn("failed downloading article: " + header.getSubject() + " " 
                + e.toString());
            e.printStackTrace();
            handleException();
            return;
        }
    }
    
    private void handleException() {
        try {
            client.disconnect();
        } 
        catch (NntpException e) {
        }

        cleanupCache(header);
        header.setStatus(header.STATUS_ERROR);
        dbManager.updateHeader(header);
        //errorMsg = ne.getMessage();
        fireEvent(new ArticleDownloadErrorEvent(this));
        try {
            Thread.sleep(3000);
        } 
        catch (InterruptedException ie) {
        }
    }
    
    /**
     * remove unneeded files from our cache
     */
    private void cleanupCache(NntpArticleHeader header) {
        log.info("cleaning up cache files");
        NntpArticlePartID[] ids = header.getParts();
        if ( ids.length > 0 ) {
            for (int x=0; x < ids.length; x++) {
                try {
                    NntpArticlePartID partID = ids[x];
                    File cachefile = new File(cacheDir + File.separator +
                            header.getServer() + "." + header.getGroup() + "." 
                            + partID.getIdAsString());
                    
                    if (cachefile.exists()) {
                        if (!cachefile.delete()) {
                            log.warn("failed removing cachefile: " + 
                                cachefile.toString());
                        }
                    }
                } 
                catch (SecurityException se) {
                    log.warn("caught exception removing cachefile: " + 
                        se.toString());
                }
            }
        } 
        else {
            try {
                File cachefile = new File(cacheDir + File.separator + header.getServer() + "." + 
                    header.getGroup() + "." + header.getID());
                if (cachefile.exists() && !cachefile.delete()) {
                    log.warn("failed removing cachefile: " + 
                        cachefile.toString());
                }
            } 
            catch (SecurityException se) {
                log.warn("caught exception removing cachefile: " + 
                    se.toString());
            }
        }
    }

	public void setCacheDir(String cacheDir) {
		this.cacheDir = cacheDir;
	}
    
}
