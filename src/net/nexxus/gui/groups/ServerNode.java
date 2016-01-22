/**
 * ServerNode.java
 *
 * a representation of an nntp group
 * for a JTree
 */

package net.nexxus.gui.groups;

import java.io.Serializable;
import javax.swing.tree.DefaultMutableTreeNode;
import net.nexxus.nntp.NntpServer;

public class ServerNode extends DefaultMutableTreeNode implements Serializable {

    private NntpServer server;

    // c'tor
    public ServerNode(NntpServer server) {
    	super();
    	this.server = server;
    }

    public String toString() { 
        return server.getServer(); 
    }

    public String getLabel()  { 
        return server.getServer();
    }
    
    public String getName() { 
        return server.getServer(); 
    }
    
    public NntpServer getServer() { 
        return server; 
    }
    
    public int getPort()   { 
        return server.getPort(); 
    }
    
    public String getUsername() { 
        return server.getUsername(); 
    }
    
    public String getPassword() { 
        return server.getPassword(); 
    }
}


