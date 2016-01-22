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
package net.nexxus.gui.groups;

import java.io.Serializable;
import javax.swing.tree.DefaultMutableTreeNode;
import net.nexxus.nntp.NntpGroup;

public class GroupNode extends DefaultMutableTreeNode implements Serializable {

    private NntpGroup group;

    // c'tor
    public GroupNode(NntpGroup group) {
        super();
        this.group = group;
    }

    public String toString() { 
        return this.group.getName(); 
    }
    
    public String getServer() { 
        return this.group.getServer(); 
    }
    
    public int getPort() { 
        return this.group.getPort(); 
    }
    
    public String getGroup() { 
        return this.group.getName();
    }
    
    public NntpGroup getNntpGroup() { 
        return this.group; 
    }
    
    public long getLastUpdate() { 
        return this.group.getLastUpdate(); 
    }
    
    public void setLastUpdate(long update) { 
        this.group.setLastUpdate(update); 
    }
}
