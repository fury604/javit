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

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.*;
import java.awt.Component;
import java.awt.Color;

import net.nexxus.util.ApplicationConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GroupTreePanelCellRenderer extends DefaultTreeCellRenderer {

	private ImageIcon groupIcon = new ImageIcon(
			this.getClass().getClassLoader().getResource(ApplicationConstants.RESOURCE_PATH + "/icon-group.gif"));

	private ImageIcon serverIcon = new ImageIcon(
			this.getClass().getClassLoader().getResource(ApplicationConstants.RESOURCE_PATH + "/icon-server.gif"));

	private ImageIcon rootIcon = new ImageIcon(
			this.getClass().getClassLoader().getResource(ApplicationConstants.RESOURCE_PATH + "/icon-root.gif"));

	private static Logger log = LogManager.getLogger(GroupTreePanelCellRenderer.class.getName());

	public GroupTreePanelCellRenderer() {
	}

	public Component getTreeCellRendererComponent( JTree tree,
			Object value,
			boolean sel,
			boolean expanded,
			boolean leaf,
			int row,
			boolean hasFocus) {

		Component cell =
			super.getTreeCellRendererComponent(tree,value,sel,expanded,leaf,row,hasFocus);
		cell.setFont(ApplicationConstants.BITSTREAM_CHARTER_12_FONT);
		if ( value instanceof RootNode ) {
			setIcon(rootIcon);
		}
		else if ( value instanceof ServerNode ) {
			setIcon(serverIcon);
		}
		else if ( value instanceof GroupNode ) {
			GroupNode node = (GroupNode)value;
			long num = System.currentTimeMillis() - node.getLastUpdate();
			// default color
			cell.setForeground(Color.BLACK);
			if ( ! node.getNntpGroup().isAutoUpdate() ) {  
				cell.setForeground(Color.GRAY); 
			}

			// indicate the group is current
			// by using the Component Manager AUTO_INTERVAL_VALUE
			// 21600000 is 6 hours
			if ( num < GroupTreePanel.AUTO_INTERVAL_MULTIPLIER ) { 
				cell.setForeground(Color.BLUE); 
			}

			if ( num < GroupTreePanel.AUTO_INTERVAL_MULTIPLIER && node.getNntpGroup().isAutoUpdate() ) {
				cell.setForeground(Color.RED);
			}

			setIcon(groupIcon);
		}
		return cell;
	}
}