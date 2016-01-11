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

import net.nexxus.nntp.*;


public class Task implements Comparable {

    private String status;
    private String server;
    private String group;
    private String subject;
    private int id;
    private NntpArticleHeader header;
    
    // public static vars
    public static String QUEUED = "queued";
    public static String DOWNLOADING = "downloading";
    public static String UPDATING = "updating";
    public static String DECODING = "decoding";
    public static String WAITING_TO_DECODE = "waiting to decode";
    public static String TASK_FINISHED = "finished";
    public static String ERROR = "error";
    public static String CANCELLING = "cancelling";
	
    // default c'tor
    public Task() {};
	
    public Task(NntpArticleHeader header) {
    	this.header = header;
    	this.status = QUEUED;
    	this.server = header.getServer();
    	this.group = header.getGroup();
    	this.subject = header.getSubject();
    }

    public Task(NntpGroup group) {
    	this.status = QUEUED;
    	this.server = group.getServer();
    	this.group = group.getName();
    	this.subject = new String();
    }

    public Task(NntpServer server) {
    	this.status = QUEUED;
    	this.server = server.getServer();
    	this.group = new String();
    	this.subject = new String();
    }

    /*
    public Task(DownloadArticleTask t) {
    	this.header = (NntpArticleHeader)t.getSource();
    	this.status = QUEUED;
    	this.server = header.getServer();
    	this.group = header.getGroup();
    	this.subject = header.getSubject();
    }
	*/

    public Task(Object o, String status) {
	
    	if (o instanceof NntpArticleHeader) {
    		this.header = (NntpArticleHeader)o;
    		this.status = status;
    		this.server = header.getServer();
    		this.group = header.getGroup();
    		this.subject = header.getSubject();
    	}
	
    	if (o instanceof NntpGroup) {
    		NntpGroup group = (NntpGroup)o;
    		this.status = status;
    		this.server = group.getServer();
    		this.group = group.getName();
    		this.subject = new String();
    	}

    	if (o instanceof NntpServer) {
    		NntpServer server = (NntpServer)o;
    		this.status = status;
    		this.server = server.getServer();
    		this.group = new String();
    		this.subject = new String();
    	}

    }  

    public boolean equals(Object o) {
    	Task t = (Task)o;
    	if (t.getStatus().equals(status) && 
    		t.getServer().equals(server) &&
			t.getGroup().equals(group) &&
			t.getSubject().equals(subject)) {
    		return true;
    	}
    	return false;
    }

    
    public int compareTo(Object o) {
    	Task t = (Task)o;
    	if ( t.getTaskID() > id ) {
    	    return -1;
    	}
    	if ( t.getTaskID() < id ) {
    	    return 1;
    	}
    	return 0;
    }
    
    
    public String getStatus() { 
    	return this.status; 
    }

    public String getServer() { 
    	return this.server; 
    }
    
    public String getGroup()  { 
    	return this.group; 
    }
    
    public String getSubject() { 
    	return this.subject; 
    }
    
    public int getTaskID() { 
    	return this.id; 
    }
    
    public NntpArticleHeader getHeader() { 
    	return this.header; 
    }

    public void setStatus(String status) { 
    	this.status = status; 
    }
    
    public void setTaskID(int id) { 
    	this.id = id; 
    }
}
