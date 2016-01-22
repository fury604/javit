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

public class RootNode extends DefaultMutableTreeNode implements Serializable {
    
    private String label = "Javit";

	// c'tor
    public RootNode() {
        super();
    }

    public boolean isLeaf() { 
        return false; 
    }
    
    public boolean getAllowsChildren() { 
        return true; 
    }
    
    public String toString() { 
        return this.label; 
    }
    
}
