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

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.nexxus.event.AddServerDialogEvent;
import net.nexxus.event.GUIEvent;
import net.nexxus.event.GUIEventListener;
import net.nexxus.event.ServerSelectedEvent;
//import net.nexxus.event.UpdateHeadersEvent;
//import net.nexxus.event.UpdateGroupsEvent;
import net.nexxus.event.GroupSelectedEvent;
import net.nexxus.gui.PanelFactory;
import net.nexxus.gui.article.ArticleTable;
import net.nexxus.gui.article.ArticleTableModel;
import net.nexxus.nntp.NntpArticleHeader;
import net.nexxus.nntp.NntpGroup;
import net.nexxus.util.ApplicationConstants;
import net.nexxus.util.ComponentManager;

public class GroupTreeGUIEventListener implements GUIEventListener {

    private JPanel panel;
    private PanelFactory panelFactory;
    private JFrame frame;
    private ComponentManager componentManager = new ComponentManager();
    private static Logger log = LogManager.getLogger(GroupTreeGUIEventListener.class.getName());
            
    public GroupTreeGUIEventListener(JPanel panel, PanelFactory panelFactory, JFrame frame) {
        this.panel = panel;
        this.panelFactory = panelFactory;
        this.frame = frame;
    }
    
    public void eventOccurred(GUIEvent event) {
        // this event we need to load up the ArticleTableModel
        if (event instanceof GroupSelectedEvent) {
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            GroupNode groupNode = (GroupNode)event.getSource();
            //System.gc();  // being bad

            final NntpGroup nntpGroup = groupNode.getNntpGroup();
            final JPanel pref = panel;
            
            // start this task in separate thread
            Thread p = new Thread(
                    new Runnable() {
                        public void run() {
                            log.debug("retrieving headers from DB");
                            log.debug("CUTOFF is " + ApplicationConstants.CUTOFF);
                            Integer cutoff = new Integer(ApplicationConstants.CUTOFF);
                            List<NntpArticleHeader> headers = 
                                    componentManager.getDBManager().getHeaders(nntpGroup, cutoff);
                            ArticleTable.model.fill(headers);
                            log.debug("finished reading cached headers");
                            pref.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        }
                    });
            p.setName("cachedHeaderThread");
            p.start();
        }

        // load GroupTable model with server groups
        if (event instanceof ServerSelectedEvent) {
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            ServerNode serverNode = (ServerNode)event.getSource();
            try {
                List<NntpGroup> groups = componentManager.getDBManager().getServerGroups();
                GroupsListPanel.groupsModel.fill(groups);
            }
            catch (Exception e) {
                log.error("failed retrieving and filling groupsModel with groups: " + e.getMessage());
            }
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
        
        // our old friend
        if (event instanceof AddServerDialogEvent) {
            panelFactory.getAddServerDialog(frame);
        }
    }
}
