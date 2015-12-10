/**
 * NntpGroup.java
 *
 * a class that represents an NNTP group
 *
 */

package net.nexxus.nntp;

import java.io.Serializable;

@SuppressWarnings("rawtypes")
public class NntpGroup implements Serializable, Comparable {

	public static int CUTOFF_VAL = 0;	

	private StringBuffer server;
	private int port = 0;
	private StringBuffer group;
	private long count = 0;
	private long lowID = 0;
	private long highID = 0;
	private int autoUpdateInterval = 0;
	private boolean autoUpdate = false;
	private long lastUpdate = 0;
	private static final long serialVersionUID = 1L;

	// c'tor
	public NntpGroup() {};

	// c'tor
	public NntpGroup(String server, int port, String group, long count) {
		this.server = new StringBuffer(server);
		this.port = port;
		this.group = new StringBuffer(group);
		this.count = count;
	}

	public String getName() { 
		return this.group.toString(); 
	}
	
	public void setName(String groupName) {
		this.group = new StringBuffer(groupName);
	}

	public long getCount() { 
		if (count == 0) {
			if (lowID > 0 && highID > 0 ) {
				return highID - lowID;
			}
		}
		return this.count; 
	}

	public String getServer() { 
		return this.server.toString(); 
	}
	
	public void setServer(String srv) {
		this.server = new StringBuffer(srv);
	}

	public int getPort() { 
		return this.port; 
	}

	public void setLowID(long id) { 
		this.lowID = id; 
	}

	public void setHighID(long id) { 
		this.highID = id; 
	}
	
	public void setCount(long count) { 
		this.count = count; 
	}

	public long getLowID() { 
		return this.lowID; 
	}

	public long getHighID() { 
		return this.highID; 
	}

	public int getAutoUpdateInterval() { 
		return this.autoUpdateInterval; 
	}

	public void setAutoUpdateInterval (int interval) { 
		this.autoUpdateInterval = interval; 
	}

	public boolean isAutoUpdate() { 
		return this.autoUpdate; 
	}

	public void setAutoUpdate(boolean auto) { 
		this.autoUpdate = auto; 
	}

	public long getLastUpdate() { 
		return this.lastUpdate; 
	}

	public void setLastUpdate(long l) { 
		this.lastUpdate = l; 
	}

	public int compareTo(Object o) {
		NntpGroup group = (NntpGroup)o;
		return this.group.toString().compareTo(group.getName());
	}

	public boolean equals(Object a) {
		NntpGroup g = (NntpGroup)a;
		if ( this.group.toString().equals(g.getName()) && this.server.toString().equals(g.getServer()) )
			return true;
		return false;
	}

}
