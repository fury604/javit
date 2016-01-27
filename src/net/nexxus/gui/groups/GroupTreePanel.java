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

/*
 * Created on Jun 5, 2004
 *
 */
package net.nexxus.gui.groups;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.EventListenerList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.event.*;
import java.awt.Font;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimerTask;
import java.util.Timer;

import net.nexxus.db.DBManager;
import net.nexxus.db.DBManagerImpl;
//import net.nexxus.cache.CacheManager;
import net.nexxus.event.*;
//import net.nexxus.gui.article.ArticleTable;
import net.nexxus.gui.PanelFactory;
import net.nexxus.task.*;
import net.nexxus.util.ComponentManager;
import net.nexxus.nntp.*;


public class GroupTreePanel extends JTree {

	public static long AUTO_INTERVAL = 3600000; 
	public static int AUTO_INTERVAL_MULTIPLIER = 3600000;

	private static DefaultTreeModel treeModel;
	private static RootNode rootNode = new RootNode();
	private static JPopupMenu popup;
	private static EventListenerList listenerList = new EventListenerList();
	private static Timer autoUpdateTimer = new Timer();

	private ComponentManager componentManager = new ComponentManager();

	private static Logger log = LogManager.getLogger(GroupTreePanel.class.getName());

	// default c'tor
	public GroupTreePanel() {
		super(new RootNode());

		/*
		// determine AUTO_INTERVAL based on properties settings
		Properties p = ComponentManager.getProperties();
		if ( p.getProperty("update_interval") != null ) {
			String value = p.getProperty("update_interval");
			int tmp = Integer.parseInt(value);
			// assume interval expressed in seconds
			log.debug("setting AUTO_INTERVAL using: " + value);
			// use 60000 for 1 minute and use 3600000 for 1 hour
			AUTO_INTERVAL = (Integer.valueOf(tmp * AUTO_INTERVAL_MULTIPLIER)).longValue();
		}
        */
		
		this.treeModel = new DefaultTreeModel(rootNode);
		
		// try populating the treeModel
		try {
		    DBManager dbManager = componentManager.getDBManager();
		    NntpServer server = dbManager.getServer();
		    if (server == null) {
		        log.debug("server was null, no server in the DB");
		    }
		    else {
		        ServerNode serverNode = new ServerNode(server);
		        treeModel.insertNodeInto(serverNode, rootNode, 0);
		        // next try grabbing subscribed groups
		        List<NntpGroup> groups = dbManager.getGroups();
		        if (groups == null || groups.size() == 0) {
		            log.debug("there were no subscribed groups");
		        }
		        else {
		            Iterator<NntpGroup> iter = groups.iterator();
		            while (iter.hasNext()) {
		                NntpGroup group = iter.next();
		                GroupNode node = new GroupNode(group);
		                treeModel.insertNodeInto(node, serverNode, 0);
		            }
		        }
		    }
		} catch (Exception e) {
			log.error("caught exception loading model from cache " + e.toString());
		}

		setCellRenderer(new GroupTreePanelCellRenderer());
		setModel(this.treeModel);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		// addMouseListener here for right click
		// context handling
		addMouseListener( new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				// right click
				if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
					// figure out what is selected
					Object node = (Object) getLastSelectedPathComponent();
					if (node == null) {
						return;
					}
					if ( node instanceof GroupNode) {
						popup = getGroupNodePopupMenu();
						popup.show(e.getComponent(), e.getX(), e.getY());
					} 
					else if (node instanceof RootNode) {
						popup = getRootNodePopupMenu();
						popup.show(e.getComponent(), e.getX(), e.getY());
					} 
					else {
						popup = getServerNodePopupMenu();
						popup.show(e.getComponent(), e.getX(), e.getY());
					}
				}
			}
		});

		// setup keyboard shortcuts
		addKeyListener( new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				// keyboard shortcut for incremental update
				if ( e.getKeyCode() == KeyEvent.VK_I ) {
					Object node = (Object) getLastSelectedPathComponent();
					if (node == null) {
						return;
					}
					if (node instanceof GroupNode) {
						GroupNode groupNode = (GroupNode)node;
						ServerNode serverNode = (ServerNode)groupNode.getParent();
						//TaskManager.getInstance().add(new UpdateHeadersTask(gnode.getNntpGroup(),snode.getServer()));
					}
				}
				// keyboard shortcut for full update
				if ( e.getKeyCode() == e.VK_U ) {
					Object node = (Object) getLastSelectedPathComponent();
					if (node == null) {
						return;
					}
					if (node instanceof GroupNode) {
						GroupNode groupNode = (GroupNode)node;
						groupNode.getNntpGroup().setHighID(0);
						ServerNode snode = (ServerNode)groupNode.getParent();
						//TaskManager.getInstance().add(new UpdateHeadersTask(gnode.getNntpGroup(),snode.getServer()));
					}
					if (node instanceof ServerNode) {
						ServerNode serverNode = (ServerNode)node;
						//TaskManager.getInstance().add(new UpdateGroupsTask(server.getServer()));

					}
				}
				// toggle auto-update
				if ( e.getKeyCode() == e.VK_T ) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) getLastSelectedPathComponent();
					if (node == null) {
						return;
					}
					if (node instanceof GroupNode) {
						GroupNode groupNode = (GroupNode)node;
						if ( groupNode.getNntpGroup().isAutoUpdate() ) {
						    groupNode.getNntpGroup().setAutoUpdate(false);
						}
						else {
						    groupNode.getNntpGroup().setAutoUpdate(true);
						}
					}
				}
				// remove group
				if ( e.getKeyCode() == e.VK_DELETE ) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) getLastSelectedPathComponent();
					if (node == null) {
						return;
					}
					if (node instanceof GroupNode) {
						treeModel.removeNodeFromParent(node);
						//ComponentManager.getCacheManager().removeGroup( ((GroupNode)node).getNntpGroup() );
					}
					if (node instanceof ServerNode) {
						treeModel.removeNodeFromParent(node);
					}
				}
			}
		});

		// add selection listener here
		addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				Object node = getLastSelectedPathComponent();
				if (node == null) {
					return;   // case ambiguous
				}
				if (node instanceof GroupNode) {
					GroupNode groupNode = (GroupNode)node;
					GroupSelectedEvent event = new GroupSelectedEvent(groupNode);
					log.debug("group has count: " + groupNode.getNntpGroup().getCount());
					fireEvent(event);
				} 
				else if (node instanceof RootNode) {
				} 
				else {
					ServerNode serverNode = (ServerNode)node;
					ServerSelectedEvent event = new ServerSelectedEvent(serverNode);
					fireEvent(event);
				}
			}
		});

		AutoUpdateTimer autoupdate = new AutoUpdateTimer();
		// schedule every 180 minutes ( 10800000 in millis )
		log.debug("autoupdate is set to " + AUTO_INTERVAL + " current time is " + System.currentTimeMillis());
		autoUpdateTimer.scheduleAtFixedRate(autoupdate, AUTO_INTERVAL , AUTO_INTERVAL);

	} 

	/**
	 * getGroupNodePopupMenu()
	 *
	 * provides right click menu for
	 * a JTree GroupNode
	 *
	 */
	private GroupNodePopupMenu getGroupNodePopupMenu() {
		return new GroupNodePopupMenu("groups for server", this, treeModel);
	}

	/**
	 * getServerNodePopupMenu()
	 *
	 * provides right click menu for GroupTree ServerNode
	 *
	 */
	public ServerNodePopupMenu getServerNodePopupMenu() {
	    return new ServerNodePopupMenu("server name", this, treeModel, componentManager.getDBManager());
	}


	/**
	 * getRootNodePopupMenu()
	 *
	 * provides right click menu for GroupTree RootNode
	 *
	 */
	public JPopupMenu getRootNodePopupMenu() {
	    // need to add my eventlistener to the c'tor
	    // so that this class can fire an event into the
	    // top level JFrame
	    return new RootNodePopupMenu("server name", listenerList);
	}

	//  public method to update TreeNodes

	/**
	 * updateGroup
	 *
	 * update a group
	 */
	public void updateGroup(NntpGroup group) {
		int childCount = treeModel.getChildCount(treeModel.getRoot());
		for (int x=0; x<= (childCount-1); x++) {
			ServerNode server = (ServerNode)treeModel.getChild(treeModel.getRoot(),x);
			// is this our server?
			if (server.getName().equals(group.getServer())) {
				int thisChildrenCount = treeModel.getChildCount(server);
				// look for our group
				for (int y=0; y <= (thisChildrenCount-1); y++) {
					GroupNode groupNode = (GroupNode)treeModel.getChild(server,y);
					if ( groupNode.getGroup().equals(group.getName()) ) {
						//log.debug("our group is: " + groupNode.getGroup());
						TreeNode[] pathTo = treeModel.getPathToRoot(groupNode);
						TreePath path = new TreePath(pathTo);
						groupNode.setLastUpdate(System.currentTimeMillis());
						treeModel.valueForPathChanged(path, groupNode);
						return;
					}
				}
			}
		}
	}

	/**
	 * updateAllAuto
	 *
	 * update all auto update groups with an incremental update
	 */
	public void updateAllAuto() {
		int childCount = treeModel.getChildCount(treeModel.getRoot());
		for (int x=0; x<= (childCount-1); x++) {
			ServerNode server = (ServerNode)treeModel.getChild(treeModel.getRoot(),x);
			int thisChildrenCount = treeModel.getChildCount(server);

			// determine if group is AutoUpdate
			for (int y=0; y <= (thisChildrenCount-1); y++) {
				GroupNode groupNode = (GroupNode)treeModel.getChild(server,y);
				if ( groupNode.getNntpGroup().isAutoUpdate() ) {
					//TaskManager.getInstance().add(new UpdateHeadersTask(groupNode.getNntpGroup(),server.getServer()));
				}
			}
		}
	}

	/**
	 * storeModel()
	 *
	 * @param listener
	 */
	public void storeModel() {
		//ComponentManager.getCacheManager().storeGroupTreeModel(treeModel);
	}

	public void addGUIEventListener(GUIEventListener listener) {
		listenerList.add(GUIEventListener.class, listener);
	}

	public void removeGUIEventListener(GUIEventListener listener) {
		listenerList.remove(GUIEventListener.class, listener);
	}

	protected void fireEvent(GUIEvent event) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==GUIEventListener.class) {
				((GUIEventListener)listeners[i+1]).eventOccurred(event);
			}
		}
	}

	/**
	 * private class to handle the triggering of 
	 * automatic incremental header updates for Groups
	 * configured to have them
	 * 
	 */
	private class AutoUpdateTimer extends TimerTask {

		public AutoUpdateTimer() { 
			super();
			log.debug("auto update time created");
		}
		
		public void run() {
			rootNode = (RootNode)treeModel.getRoot();
			int servers = treeModel.getChildCount(rootNode);
			log.debug("in auti update found " + servers + " nodes in tree");
			for (int x=0; x < servers; x++) {
				ServerNode snode = (ServerNode)treeModel.getChild(rootNode, x);
				int groups = treeModel.getChildCount(snode);
				for (int y=0; y < groups; y++) {
					GroupNode g = (GroupNode)treeModel.getChild(snode, y);
					long elapsed = System.currentTimeMillis() - g.getLastUpdate();
					log.debug("auto update time elapsed for " + g.getGroup() + " is " + elapsed);
					if (g.getNntpGroup().isAutoUpdate() && elapsed > AUTO_INTERVAL) {
						//TaskManager.getInstance().add(new UpdateHeadersTask(g.getNntpGroup(),snode.getServer()));
					}
				}
			}
		}
	}
}
