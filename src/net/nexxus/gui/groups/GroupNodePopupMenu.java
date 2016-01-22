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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.nexxus.task.UpdateHeadersTask;
import net.nexxus.util.ApplicationConstants;


public class GroupNodePopupMenu extends JPopupMenu {

    private final JTree groupTree;
    private final DefaultTreeModel treeModel;
    
    public GroupNodePopupMenu(String label, final JTree groupTree, final DefaultTreeModel treeModel) {
        super(label); // "groups for server"
        this.groupTree = groupTree;
        this.treeModel = treeModel;
        
        this.add(getIncrementalUpdateMenuItem());
        this.add(getAutoUpdateMenuItem());
        this.add(getFullUpdateMenuItem());
        this.add(getReloadMenuItem());
        this.add(getRemoveMenuItem());
    }

    /**
     * the incremental update JMenuItem
     */
    private JMenuItem getIncrementalUpdateMenuItem() {
        JMenuItem menuItem = new JMenuItem("update - incremental");
        menuItem.setFont(ApplicationConstants.LUCIDA_FONT);
        menuItem.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                GroupNode groupNode = (GroupNode) groupTree.getLastSelectedPathComponent();
                ServerNode serverNode = (ServerNode)groupNode.getParent();
                //TaskManager.getInstance().add(new UpdateHeadersTask(node.getNntpGroup(),snode.getServer()));
            }
        });
        
        return menuItem;
    }
    
    /**
     * the auto update JMenuItem
     */
    private JMenuItem getAutoUpdateMenuItem() {
        JMenuItem menuItem = new JMenuItem("toggle autoupdate");
        menuItem.setFont(ApplicationConstants.LUCIDA_FONT);
        menuItem.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                GroupNode groupNode = (GroupNode) groupTree.getLastSelectedPathComponent();
                if ( groupNode.getNntpGroup().isAutoUpdate() ) {
                    groupNode.getNntpGroup().setAutoUpdate(false);
                }
                else {
                    groupNode.getNntpGroup().setAutoUpdate(true);
                }
            }
        });
        
        return menuItem;
    }
    
    /**
     * the full update JMenuItem
     */
    private JMenuItem getFullUpdateMenuItem() {
        JMenuItem menuItem = new JMenuItem("update - full");
        menuItem.setFont(ApplicationConstants.LUCIDA_FONT);
        menuItem.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                GroupNode groupNode = (GroupNode) groupTree.getLastSelectedPathComponent();
                groupNode.getNntpGroup().setHighID(0);
                ServerNode serverNode = (ServerNode)groupNode.getParent();
                //TaskManager.getInstance().add(new UpdateHeadersTask(node.getNntpGroup(),snode.getServer()));
            }
        });
        
        return menuItem;
    }
    
    /**
     * the reload JMenuItem
     */
    private JMenuItem getReloadMenuItem() {
        JMenuItem menuItem = new JMenuItem("reload");
        menuItem.setFont(ApplicationConstants.LUCIDA_FONT);
        menuItem.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                GroupNode groupNode = (GroupNode) groupTree.getLastSelectedPathComponent();
                //ArticleTable.model.fill(
                //        ComponentManager.getCacheManager().getCachedHeaders( ((GroupNode)node).getNntpGroup(), ComponentManager.CUTOFF_VAL )
                //);
            }
        });
        
        return menuItem;
    }

    /**
     * the reload JMenuItem
     */
    private JMenuItem getRemoveMenuItem() {
        JMenuItem menuItem = new JMenuItem("remove");
        menuItem.setFont(ApplicationConstants.LUCIDA_FONT);
        menuItem.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) groupTree.getLastSelectedPathComponent();
                treeModel.removeNodeFromParent(node);
                //ComponentManager.getCacheManager().removeGroup( ((GroupNode)node).getNntpGroup() );
            }
        });
        
        return menuItem;
    }
}
