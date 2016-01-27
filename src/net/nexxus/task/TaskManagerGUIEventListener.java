package net.nexxus.task;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.nexxus.db.DBManager;
import net.nexxus.event.GUIEvent;
import net.nexxus.event.GUIEventListener;
import net.nexxus.event.GroupsUpdatedEvent;
import net.nexxus.event.HeadersUpdatedEvent;
import net.nexxus.gui.article.ArticleTable;
import net.nexxus.gui.groups.GroupNode;
import net.nexxus.gui.groups.GroupTreeGUIEventListener;
import net.nexxus.gui.groups.GroupTreePanel;
import net.nexxus.gui.groups.GroupsListPanel;
import net.nexxus.gui.groups.ServerNode;
import net.nexxus.nntp.NntpArticleHeader;
import net.nexxus.nntp.NntpGroup;
import net.nexxus.nntp.NntpServer;
import net.nexxus.util.ApplicationConstants;
import net.nexxus.util.ComponentManager;

public class TaskManagerGUIEventListener implements GUIEventListener  {
    
    private GroupTreePanel groupTreePanel;
    private DBManager dbManager;
    private static Logger log = LogManager.getLogger(TaskManagerGUIEventListener.class);
    
    public TaskManagerGUIEventListener(GroupTreePanel groupTreePanel, DBManager dbManager) {
        this.groupTreePanel = groupTreePanel;
        this.dbManager = dbManager;
    }

    public void eventOccurred(GUIEvent e) {
        // GroupsUpdatedEvent, reload the group
        if (e instanceof GroupsUpdatedEvent) {
            NntpServer s = (NntpServer)e.getSource();
            Object selected = groupTreePanel.getLastSelectedPathComponent();
            if (selected instanceof ServerNode) {
                ServerNode snode = (ServerNode)selected;
                if (snode.getName().equals(s.getServer())) {
                    try {
                        List<NntpGroup> groups = dbManager.getServerGroups();
                        GroupsListPanel.groupsModel.fill(groups);
                    }
                    catch (Exception ex) {
                        log.error("could not fill groupsListPanel model with server groups from DB: "
                                + ex.getMessage());
                    }
                }
            }
        }

        // HeadersUpdatedEvent
        if (e instanceof HeadersUpdatedEvent) {
            UpdateHeadersTask t= (UpdateHeadersTask)e.getSource();
            NntpGroup g = (NntpGroup)t.getSource();
            Object selected = groupTreePanel.getLastSelectedPathComponent();
            if (selected instanceof GroupNode) {
                GroupNode gnode = (GroupNode)selected;
                if (gnode.getGroup().equals(g.getName()) &&
                        gnode.getServer().equals(g.getServer()) ) 
                {
                    log.debug("calling get cached headers with: " + ApplicationConstants.CUTOFF);
                    NntpGroup group = gnode.getNntpGroup();
                    List<NntpArticleHeader> headers = dbManager.getHeaders(group, ApplicationConstants.CUTOFF); 
                    ArticleTable.model.fill(headers);
                }
            }
        }
    }
}
