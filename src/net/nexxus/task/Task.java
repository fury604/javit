
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
