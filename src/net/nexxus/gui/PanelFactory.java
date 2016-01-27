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
package net.nexxus.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.EventListenerList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.nexxus.db.DBManager;
import net.nexxus.event.AddServerEvent;
//import net.nexxus.event.AddServerEvent;
import net.nexxus.event.GUIEvent;
import net.nexxus.event.GUIEventListener;
import net.nexxus.event.SubscribeGroupEvent;
import net.nexxus.gui.article.ArticlePanel;
import net.nexxus.gui.article.ArticleTable;
import net.nexxus.gui.dialog.AddServerDialog;
import net.nexxus.gui.groups.GroupNode;
import net.nexxus.gui.groups.GroupTreePanel;
import net.nexxus.gui.groups.GroupsListPanel;
import net.nexxus.gui.groups.RootNode;
import net.nexxus.gui.groups.ServerNode;
import net.nexxus.gui.task.TaskPanel;
//import net.nexxus.event.SubscribeGroupEvent;
//import net.nexxus.gui.groups.GroupsListPanel;
//import net.nexxus.gui.article.ArticlePanel;
//import net.nexxus.gui.article.ArticleTable;
//import net.nexxus.gui.dialog.AddServerDialog;
//import net.nexxus.gui.tree.RootNode;
//import net.nexxus.gui.tree.ServerNode;
//import net.nexxus.gui.tree.GroupNode;
//import net.nexxus.gui.task.TaskPanel;
import net.nexxus.nntp.NntpGroup;
import net.nexxus.util.ApplicationConstants;
import net.nexxus.util.ComponentManager;

/**
 * The PanelFactory gives us an easy way to fetch the
 * needed graphical components that make up the main 
 * UI for Javit
 */

public class PanelFactory {

	public static GroupTreePanel groupTreePanel = new GroupTreePanel();
	
	private static TaskPanel taskPanel;
	private static GroupsListPanel groupListPanel;
	private static JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	private static EventListenerList listenerList = new EventListenerList();
	private static AddServerDialog addServerDialog;
	//private static ProgressBar progressBar = new ProgressBar();
	private static ArticlePanel articlePanel = new ArticlePanel();
	private ComponentManager componentManager = new ComponentManager();
	private static Logger log = LogManager.getLogger(PanelFactory.class.getName());

	// default c'tor
	public PanelFactory() { 
	    // create and start a thread for our progressBar
		//Thread pb = new Thread(progressBar);
		//pb.start();
	    
	    // setup the splitpane
		splitPane.add(getLeftSide());
		splitPane.add(getRightSide());
	}

	/**
	 * build up the JPanel that will be the GroupView and
	 * the InfoWindow Panels
	 *
     */
	public JComponent getLeftSide() {
		JScrollPane treeScrollPane = new JScrollPane(groupTreePanel);
		JSplitPane left = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		left.setDividerSize(2);
		treeScrollPane.setPreferredSize( new Dimension(200, 450) );
		left.setTopComponent(treeScrollPane);

		// build panel for ProgressBars
		InfoPanel infoPanel = new InfoPanel();
		infoPanel.listenTo(groupTreePanel);

		left.setBottomComponent(infoPanel);
		left.resetToPreferredSizes();
		left.setResizeWeight(1.0);
		return left;
	}

	/**
	 * get the right side GUI components,
	 * TabbedPane with article, task and grouplist 
	 * tables
	 * 
	 * @return
	 */
	public JTabbedPane getRightSide() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(ApplicationConstants.LUCIDA_FONT);

        // Article tab
        tabbedPane.add("articles", articlePanel);

        // Tasks tab
        tabbedPane.add("tasks", getTaskPanel());

        // Groups tab
        JScrollPane groupScrollPane = new JScrollPane(getGroupsListPanel());
        tabbedPane.add("groups",groupScrollPane);
        
        return tabbedPane;
	}
	
	public TaskPanel getTaskPanel() {
       this.taskPanel = new TaskPanel();
       return this.taskPanel;
	}
	
	public GroupTreePanel getGroupTree() { 
		return groupTreePanel; 
	}

	public GroupsListPanel getGroupsListPanel() { 
	    this.groupListPanel = new GroupsListPanel();
	    this.groupListPanel.addGUIEventListener(new GUIEventListener() {
	        public void eventOccurred(final GUIEvent event) {
	            // SubscribeGroup Event
	            if (event instanceof SubscribeGroupEvent) {
	                NntpGroup group = (NntpGroup)event.getSource();
	                RootNode root = (RootNode)groupTreePanel.getModel().getRoot();
	                int childCount = groupTreePanel.getModel().getChildCount(root);
	                GroupNode node = new GroupNode(group);
	                // iterate over server nodes
	                for (int x=0; x <= (childCount-1); x++) {
	                    ServerNode server = 
	                            (ServerNode)groupTreePanel.getModel().getChild(root,x);
	                    // now compare
	                    //if ( server.getName().equals(group.getServer()) ) {
                        //}

	                    ((DefaultTreeModel)groupTreePanel.getModel()).insertNodeInto(node, server, 0);
                        // store this subscription in the DB
                        try {
                            DBManager dbManager = componentManager.getDBManager();
                            group.setServer(server.getName()); // correct for ambiguity
                            dbManager.addGroup(group);
                        }
                        catch (Exception e) {
                            log.error("failed saving group subscription to DB: " + e.getMessage());
                        }
                        
	                }
	            } // end SubscribeGroupEvent
	        }
	    });

		return groupListPanel; 
	}
	
	public JSplitPane getSplitPane() { 
		return splitPane; 
	}
	
	public ArticleTable getArticleTable() { 
		return articlePanel.getArticleTable(); 
	}

	/*
	 * getAddServerDialog()
	 * 
	 * return an instance of the AddServerDialog panel
	 * ensure its c'tor is only called once. 
     */
	public void getAddServerDialog(Frame frame) {
		if (addServerDialog == null) {
		    addServerDialog = new AddServerDialog(frame);
		    addServerDialog.addGUIEventListener( new GUIEventListener() {
				public void eventOccurred(GUIEvent event) {
					if (event instanceof AddServerEvent) {
						RootNode rnode = (RootNode)groupTreePanel.getModel().getRoot();
						ServerNode snode = (ServerNode)event.getSource();
						// insert the Server into the DB?
						DBManager dbManager = componentManager.getDBManager();
						try {
						    dbManager.addServer(snode.getServer());
						}
						catch (Exception e) {
						    log.error("failed saving NntpServer to DB: " + e.getMessage());
						}
						((DefaultTreeModel)groupTreePanel.getModel()).insertNodeInto(snode,rnode,
								(groupTreePanel.getModel().getChildCount(rnode)));
					}
				}
			});
		}
		else {
		    log.debug("addServerDialog not null");
		}
		addServerDialog.setVisible(true);
	}


	/**
	 * showProgressBar(Object o) 
	 *
	 * make visible a progess bar that
	 * continually listens to progress events
	 * for a given Task
	public void showProgressBar(boolean showing) {
		progressBar.setVisible(showing);
	}

	public void setProgressBarLocation(Rectangle r) {
		progressBar.setLocation(r);
	}
     */

	/**
	 * getMenuBar()
	 *
	 * this method provides a menubar 
	 */
	public MenuBar getMenuBar() { 
		return new MenuBar();
	}

	/////////////////////////////
	// provide Listener methods
	// for event system
	/////////////////////////////

	// add Listener to GUIevents
	public void addGUIEventListener(GUIEventListener listener) {
		listenerList.add(GUIEventListener.class, listener);
	}
	// remove Listener to GUIevents
	public void removeGUIEventListener(GUIEventListener listener) {
		listenerList.remove(GUIEventListener.class, listener);
	}
	// tell all the Listeners about the Event
	protected void fireEvent(GUIEvent event) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==GUIEventListener.class) {
				((GUIEventListener)listeners[i+1]).eventOccurred(event);
			}
		}
	}

}
