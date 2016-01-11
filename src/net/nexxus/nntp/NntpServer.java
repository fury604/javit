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
