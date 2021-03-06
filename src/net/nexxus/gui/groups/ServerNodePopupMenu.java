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
import javax.swing.tree.DefaultTreeModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.nexxus.db.DBManager;
import net.nexxus.nntp.NntpClient;
import net.nexxus.nntp.NntpClientV2;
import net.nexxus.task.TaskManager;
import net.nexxus.task.UpdateGroupsTask;
import net.nexxus.util.ApplicationConstants;

public class ServerNodePopupMenu extends JPopupMenu {

    private final JTree groupTree;
    private final DefaultTreeModel treeModel;
    private final DBManager dbManager;
    private static Logger log = LogManager.getLogger(ServerNodePopupMenu.class.getName());
            
    public ServerNodePopupMenu(String label, final JTree groupTree, 
            final DefaultTreeModel treeModel, final DBManager dbManager) {
        super(label); //"server name"
        this.groupTree = groupTree;
        this.treeModel = treeModel;
        this.dbManager = dbManager;
        
        this.add(getUpdateGroupsMenuItem());
        this.add(getRemoveServerMenuItem());
    }
    
    private JMenuItem getUpdateGroupsMenuItem() {
        JMenuItem menuItem = new JMenuItem("update groups");
        menuItem.setFont(ApplicationConstants.LUCIDA_FONT);
        menuItem.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                ServerNode serverNode = (ServerNode)groupTree.getLastSelectedPathComponent();
                NntpClient client = new NntpClientV2(serverNode.getServer(), dbManager);
                UpdateGroupsTask updateTask = new UpdateGroupsTask(dbManager, client, serverNode.getServer());
                TaskManager.getInstance().add(updateTask);
            }
        });
        
        return menuItem;
    }
    
    private JMenuItem getRemoveServerMenuItem() {
        JMenuItem menuItem = new JMenuItem("remove server");
        menuItem.setFont(ApplicationConstants.LUCIDA_FONT);
        menuItem.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                try {
                    ServerNode node = (ServerNode)groupTree.getLastSelectedPathComponent();
                    treeModel.removeNodeFromParent(node);
                    try {
                        dbManager.removeServer(node.getServer());
                    }
                    catch (Exception ex) {
                        log.error("failed removing Server from DB: " + ex.getMessage());
                    }
                }
                catch (NullPointerException npe) {
                    log.error("no node for lastSelected: " + npe.getMessage());
                }
            }
        });
        
        return menuItem;
    }
}
