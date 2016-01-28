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
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.nexxus.db.DBManager;
import net.nexxus.gui.article.ArticleTable;
import net.nexxus.nntp.NntpArticleHeader;
import net.nexxus.nntp.NntpClient;
import net.nexxus.nntp.NntpClientV2;
import net.nexxus.nntp.NntpGroup;
import net.nexxus.task.TaskManager;
import net.nexxus.task.UpdateHeadersTask;
import net.nexxus.util.ApplicationConstants;
import net.nexxus.util.ComponentManager;


public class GroupNodePopupMenu extends JPopupMenu {

    private final JTree groupTree;
    private final DefaultTreeModel treeModel;
    private final ComponentManager componentManager = new ComponentManager();
    private final DBManager dbManager;
    private static Logger log = LogManager.getLogger(GroupNodePopupMenu.class);
    
    public GroupNodePopupMenu(String label, final JTree groupTree, final DefaultTreeModel treeModel) {
        super(label); // "groups for server"
        this.groupTree = groupTree;
        this.treeModel = treeModel;
        this.dbManager = componentManager.getDBManager();
        
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
                GroupNode groupNode = (GroupNode)groupTree.getLastSelectedPathComponent();
                ServerNode serverNode = (ServerNode)groupNode.getParent();
                NntpClient client = new NntpClientV2(serverNode.getServer(), dbManager);
                TaskManager.getInstance().add(
                        new UpdateHeadersTask(
                                dbManager, client, groupNode.getNntpGroup(), serverNode.getServer())
                );
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
                try {
                    dbManager.updateGroup(groupNode.getNntpGroup());
                }
                catch (Exception ex) {
                    log.error("failed updating NntpGroup: " + ex.getMessage());
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
                NntpClient client = new NntpClientV2(serverNode.getServer(), dbManager);
                TaskManager.getInstance().add(
                        new UpdateHeadersTask(
                                dbManager, client, groupNode.getNntpGroup(), serverNode.getServer())
                );
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
                List<NntpArticleHeader> headers = 
                        dbManager.getHeaders(groupNode.getNntpGroup(), ApplicationConstants.CUTOFF);
                ArticleTable.model.fill(headers);
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
                GroupNode node = (GroupNode) groupTree.getLastSelectedPathComponent();
                treeModel.removeNodeFromParent(node);
                try {
                    dbManager.removeGroup(node.getNntpGroup());
                }
                catch (Exception ex) {
                    log.error("failed removing subscribed group from DB: " + ex.getMessage());
                }
            }
        });
        
        return menuItem;
    }
}
