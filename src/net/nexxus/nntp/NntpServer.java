/**
 *  NntpServer.java
 * 
 * a class to represent an NNTP Server
 * 
 */
package net.nexxus.nntp;

import java.io.Serializable;

public class NntpServer implements Serializable {

	private String server;
    private int port;
    private String username;
    private String password;
	private static final long serialVersionUID = -8509550459520146635L;

	// c'tor
    public NntpServer() {};

    public NntpServer(String server, int port) {
		this.server = server;
		this.port = port;
    }

    public NntpServer(String server, int port, String user, String pass) {
		this.server = server;
		this.port = port;
		this.username = user;
		this.password = pass;
    }

    public String getServer() { 
    	return server; 
    }
    
    public int getPort() { 
    	return port; 
    }
    
    public String getUsername() { 
    	return username; 
    }
    public String getPassword() { 
    	return password; 
    }
}
