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
package net.nexxus.gui.article;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.nexxus.event.DownloadArticleEvent;
import net.nexxus.event.GUIEvent;
import net.nexxus.event.GUIEventListener;
import net.nexxus.gui.groups.GroupNode;
import net.nexxus.gui.groups.GroupTreePanel;
import net.nexxus.gui.groups.ServerNode;
import net.nexxus.nntp.NntpArticleHeader;
import net.nexxus.task.DownloadArticleTask;
import net.nexxus.task.TaskManager;

public class ArticleTableGUIEventListener implements GUIEventListener {

    private static GroupTreePanel groupTreePanel;
    private static Logger log = LogManager.getLogger(ArticleTableGUIEventListener.class);
    
    public ArticleTableGUIEventListener(GroupTreePanel groupTreePanel) {
        this.groupTreePanel = groupTreePanel;
    }
    
    public void eventOccurred(GUIEvent event) {
        if (event instanceof DownloadArticleEvent) {
            Object source = event.getSource();
            
            // single article
            if (source instanceof NntpArticleHeader) {
                Object selected = groupTreePanel.getLastSelectedPathComponent();
                NntpArticleHeader article = (NntpArticleHeader)source;
                if (selected instanceof GroupNode) {
                    GroupNode node = (GroupNode)selected;
                    // article doesn't have NntpGroup, fix that
                    article.setGroup(node.getGroup());
                    ServerNode parent = (ServerNode)node.getParent();
                    DownloadArticleTask task = 
                        new DownloadArticleTask((NntpArticleHeader)article, parent.getServer());
                    TaskManager.getInstance().add(task);
                }
            }
            
            // Collection of articles
            if (source instanceof ArrayList) {
                ArrayList selection = (ArrayList)source;
                Object selected = groupTreePanel.getLastSelectedPathComponent();
                for (int x = 0; x < selection.size(); x++) {
                    NntpArticleHeader article = (NntpArticleHeader)selection.get(x);
                    if (selected instanceof GroupNode) {
                        GroupNode node = (GroupNode)selected;
                        // article doesn't have NntpGroup, fix that
                        article.setGroup(node.getGroup());
                        ServerNode parent = (ServerNode)node.getParent();
                        DownloadArticleTask task = 
                            new DownloadArticleTask((NntpArticleHeader)article, parent.getServer());
                        TaskManager.getInstance().add(task);
                    }
                }
            }
        }
    }
}
