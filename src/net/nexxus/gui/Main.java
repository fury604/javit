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

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import net.nexxus.event.AutoUpdatesEvent;
//import net.nexxus.cache.CacheManager;
//import net.nexxus.event.AutoUpdatesEvent;
import net.nexxus.event.GUIEvent;
import net.nexxus.event.GUIEventListener;
import net.nexxus.event.SystemExitEvent;
//import net.nexxus.event.GroupsUpdatedEvent;
import net.nexxus.event.HeadersUpdatedEvent;
import net.nexxus.gui.groups.GroupTreeGUIEventListener;
//import net.nexxus.event.SystemExitEvent;
//import net.nexxus.event.UpdateHeadersEvent;
//import net.nexxus.event.UpdateGroupsEvent;
//import net.nexxus.event.GroupSelectedEvent;
//import net.nexxus.event.ServerSelectedEvent;
import net.nexxus.event.AddServerDialogEvent;
//import net.nexxus.event.DownloadArticleEvent;
//import net.nexxus.gui.article.ArticleTableModel;
//import net.nexxus.gui.groups.GroupsListTableModel;
//import net.nexxus.gui.tree.ServerNode;
//import net.nexxus.gui.tree.GroupNode;
import net.nexxus.nntp.NntpArticleHeader;
import net.nexxus.nntp.NntpGroup;
import net.nexxus.nntp.NntpServer;
//import net.nexxus.task.DownloadArticleTask;
//import net.nexxus.task.TaskManager;
import net.nexxus.task.UpdateHeadersTask;
//import net.nexxus.util.ComponentManager;
import net.nexxus.util.ApplicationConstants;

public class Main {

	private static Logger log = LogManager.getLogger(Main.class.getName());
	//private static Properties properties = ComponentManager.getProperties();
	private static JPanel panel;
	private static JFrame frame;
	//private static TaskManager taskManager;
	private static PanelFactory panelFactory = new PanelFactory();
	private static MenuBar menuBar;	

	/**
	 * start Graphical interface
	 */
	public static void main(String[] args) {
	    SwingUtilities.invokeLater(
	        new Runnable() {
	            public void run() { 
	                createComponents(); 
	            }
	        }
	    );
	}

	/**
	 * create the Swing components we need
	 */
	private static void createComponents() {

		// ensure folders are present
		try { 
			checkPaths(); 
		} 
		catch (Exception e) {
			System.err.println("failed creating needed directories!");
			//System.err.println(properties.getProperty("header_dir"));
			System.exit(1);
		}

		//set up TaskManager Thread
		//TaskManager taskManager = TaskManager.getInstance();
		ThreadGroup taskGroup = new ThreadGroup("taskmanager");
		taskGroup.setMaxPriority(5);
		/**
		Thread tasksThread = new Thread(taskGroup, taskManager, taskGroup.getName());
		try {
			tasksThread.setPriority(2);
			tasksThread.start();
		} 
		catch (IllegalThreadStateException ite) {
			System.out.println("could not start taskmanager thread " + ite.toString());
		}
		*/

		// create a window
		frame = new JFrame("Javit News Client");
		// give us a panel
		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setPreferredSize(new java.awt.Dimension(1000,500));

		// set up GUI components
		panel.add(panelFactory.getSplitPane());

		////////////////////////////////////////
		// Add EventListeners to Panel Objects
		////////////////////////////////////////
		GroupTreeGUIEventListener groupTreeListener = new GroupTreeGUIEventListener(panel, panelFactory, frame);
		panelFactory.getGroupTree().addGUIEventListener(groupTreeListener);
		

		/**
		 * listen for user events in the main ArtcileTable panel
		 *
		 * we watch for 
		 * Download events, add them to the TaskManager queue
		 * ArrayList events, groups of headers, add them to the TaskManager queue
		 */
		/*
		poof.getArticleTable().addGUIEventListener(new GUIEventListener() {
			public void eventOccurred(GUIEvent event) {
				if (event instanceof DownloadArticleEvent) {
					Object source = event.getSource();
					// single article
					if ( source instanceof NntpArticleHeader ) {
						Object selected = poof.getGroupTree().getLastSelectedPathComponent();
						NntpArticleHeader article = (NntpArticleHeader)source;
						if ( selected instanceof GroupNode ) {
							GroupNode node = (GroupNode)selected;
							ServerNode parent = (ServerNode)node.getParent();
							DownloadArticleTask task = 
								new DownloadArticleTask((NntpArticleHeader)article, parent.getServer());
							//
							if (properties.getProperty("cache_dir") != null) {
								log.debug("setting cache_dir to: " + properties.getProperty("cache_dir"));
								task.setCacheDir(properties.getProperty("cache_dir"));
							}
							TaskManager.getInstance().add(task);
						}
					}
					// Collection of articles
					if ( source instanceof ArrayList ) {
						ArrayList selection = (ArrayList)source;
						Object selected = poof.getGroupTree().getLastSelectedPathComponent();
						for ( int x = 0; x < selection.size(); x++ ) {
							NntpArticleHeader article = (NntpArticleHeader)selection.get(x);
							if ( selected instanceof GroupNode ) {
								GroupNode node = (GroupNode)selected;
								ServerNode parent = (ServerNode)node.getParent();
								DownloadArticleTask task = 
									new DownloadArticleTask((NntpArticleHeader)article, parent.getServer());
								TaskManager.getInstance().add(task);
							}
						}
					}
				}
			}
		});
        */

		/**
		 * registering event listeners with the TaskManager
		 * supplies behaviour for 
		 *
		 * reloading the server groupsList on update
		 * reloading headers in the ArticleTable after update
		 */   
		
		/*
		taskManager.addGUIEventListener(new GUIEventListener() {

			// add Event Listeners
			public void eventOccurred(GUIEvent e) {

				// GroupsUpdatedEvent, reload the group
				if (e instanceof GroupsUpdatedEvent) {
					NntpServer s = (NntpServer)e.getSource();
					Object selected = poof.getGroupTree().getLastSelectedPathComponent();
					if (selected instanceof ServerNode) {
						ServerNode snode = (ServerNode)selected;
						if (snode.getName().equals(s.getServer())) {
							((GroupsListTableModel)
									(poof.getGroupListPanel().getModel())).fill(ComponentManager.getCacheManager().getGroupList(s));
						}
					}
				}

				// HeadersUpdatedEvent
				if (e instanceof HeadersUpdatedEvent) {
					poof.getGroupTree().storeModel();
					UpdateHeadersTask t= (UpdateHeadersTask)e.getSource();
					NntpGroup g = (NntpGroup)t.getSource();
					poof.getGroupTree().updateGroup(g);
					Object selected = poof.getGroupTree().getLastSelectedPathComponent();
					if (selected instanceof GroupNode) {
						GroupNode gnode = (GroupNode)selected;
						if (gnode.getGroup().equals(g.getName()) &&
								gnode.getServer().equals(g.getServer()) ) 
						{
							log.debug("calling get cached headers with: " + ComponentManager.CUTOFF_VAL);
							ArrayList tmp = 
								ComponentManager.getCacheManager().getCachedHeaders(
										gnode.getNntpGroup(), ComponentManager.CUTOFF_VAL);

							((ArticleTableModel)(poof.getArticleTable().getModel())).fill(tmp);
							//CacheManager.getInstance().getCachedHeaders(gnode.getNntpGroup()));
							//ComponentManager.getCacheManager().expireHeaders(gnode.getNntpGroup());
						}
					}
				}


			}
		});
        */



		// make frame visible
		frame.setContentPane(panel);
		
		// add a shutdown hook
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) { 
				//poof.getGroupTree().storeModel();
				//ComponentManager.getCacheManager().closeAll();
				System.exit(0); 
			}
		});

		menuBar = panelFactory.getMenuBar();
		menuBar.addGUIEventListener(new GUIEventListener() {
			public void eventOccurred(final GUIEvent event) {
				// System Exit
				if (event instanceof SystemExitEvent) {
					//poof.getGroupTree().storeModel();
					//ComponentManager.getCacheManager().closeAll();
					clearCache();
					System.exit(0);
				}
				// Add Server Dialog
				if (event instanceof AddServerDialogEvent) {
					panelFactory.getAddServerDialog(frame);
				}
				if (event instanceof AutoUpdatesEvent) {
					panelFactory.getGroupTree().updateAllAuto();
				}
			}
		});

		frame.setJMenuBar(menuBar);
		frame.pack();
		frame.setResizable(true);
		frame.setVisible(true);
		
	}


	/**
	 * checkPaths()
	 * 
	 * confirm our directory setup
	 * @throws Exception
	 */
	private static void checkPaths() throws Exception {
		File headers = new File(ApplicationConstants.HEADERS_DIR);
		File cache = new File(ApplicationConstants.CACHE_DIR);
		File downloads = new File(ApplicationConstants.DOWNLOAD_DIR);
		if (! headers.exists()) { 
			headers.mkdir();
		}
		
		if (! cache.exists()) { 
			cache.mkdir();
		}
		
		if (! downloads.exists()) { 
			downloads.mkdir();
		}
	}

	/**
	 * clearCache()
	 *
	 * a function to delete all tmp files
	 */
	private static void clearCache() {
		File cachedir = new File(ApplicationConstants.CACHE_DIR);
		File[] files = cachedir.listFiles();
		for (int x=0; x < files.length; x++) {
			files[x].delete();
		}
	}

}
