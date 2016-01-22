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
package net.nexxus.gui.task;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.event.EventListenerList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.nexxus.event.GUIEvent;
import net.nexxus.event.GUIEventListener;
//import net.nexxus.event.ArticleDownloadedEvent;
import net.nexxus.event.HeadersUpdatedEvent;
import net.nexxus.event.HeadersUpdateErrorEvent;
//import net.nexxus.event.GroupsUpdatedEvent;
import net.nexxus.event.ArticleDownloadErrorEvent;
//import net.nexxus.event.GroupsUpdateErrorEvent;
//import net.nexxus.event.DownloadCanceledEvent;
import net.nexxus.event.UpdateHeadersEvent;
//import net.nexxus.event.CancelArticleDownloadEvent;

import net.nexxus.task.Task;
//import net.nexxus.task.TaskManager;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;


public class TaskTable extends JTable {

	// private members
	private static TaskTableModel taskModel = new TaskTableModel(); 
	private static EventListenerList listenerList = new EventListenerList();
	//private static TaskManager tm = TaskManager.getInstance();
	private static Logger log = LogManager.getLogger(TaskTable.class.getName());
	private Font defaultFont = new Font("Lucida",Font.PLAIN,11);

	// c'tor
	public TaskTable() {
		super();
		setModel(taskModel);
		setBackground( Color.WHITE );

		/*
		tm.addGUIEventListener( new GUIEventListener() {
			public void eventOccurred(GUIEvent event) {
				if (event instanceof ArticleDownloadedEvent) {
					taskModel.fill(tm.getTaskList());
				}

				if (event instanceof HeadersUpdatedEvent) {
					taskModel.fill(tm.getTaskList());
				}

				if (event instanceof HeadersUpdateErrorEvent) {
					log.error("got HeadersUpdateErrorEvent");
					taskModel.fill(tm.getTaskList());
				}

				if (event instanceof GroupsUpdatedEvent) {
					taskModel.fill(tm.getTaskList());
				}

				if (event instanceof ArticleDownloadErrorEvent) {
					log.info("got ArticleDownloadErrorEvent");
					taskModel.fill(tm.getTaskList());
				}

				if (event instanceof GroupsUpdateErrorEvent) {
					log.info("got GroupsUpdateErrorEvent");
				}

				if (event instanceof DownloadCanceledEvent) {
					log.info("got DownloadCanceledEvent");
					taskModel.fill(tm.getTaskList());
				}

				if (event instanceof UpdateHeadersEvent) {
					taskModel.fill(tm.getTaskList());
				}

				if (event instanceof CancelArticleDownloadEvent) {
					log.info("got CancelArticleDownloadEvent");
					taskModel.fill(tm.getTaskList());
				}
			}
		});
        */
		
		// renderer
		setDefaultRenderer(String.class, new TaskTableCellRenderer());

		// setup Columns
		getColumnModel().getColumn(0).setPreferredWidth(50);
		getColumnModel().getColumn(1).setPreferredWidth(50);
		getColumnModel().getColumn(2).setPreferredWidth(70);
		getColumnModel().getColumn(3).setPreferredWidth(600);

		// addMouseListener for right click context
		addMouseListener( new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if ((e.getModifiers() & java.awt.event.InputEvent.BUTTON3_MASK) != 0) {
					JPopupMenu pop = getTasksPopupMenu();
					pop.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});

		// add KeyListener for keyboard shortcuts
		addKeyListener( new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if ( e.getKeyCode() == e.VK_DELETE ) {  // cancel selected shortcut
					int[] rows = getSelectedRows();
					if ( rows.length == 1 ) {
						Task t = (Task)taskModel.getRow(getSelectedRow());
						//tm.cancel(t);
					} 
					else {
						ArrayList tasks = new ArrayList(); 
						int[] r = getSelectedRows();
						for (int x=0; x <= r.length; x++) {
							Task t = taskModel.getRow(r[x]);
							tasks.add(t);
						}
						//tm.cancel(tasks);
					}
				}

				// clear errors shortcut
				if ( e.getKeyCode() == e.VK_C ) {
					//tm.clearErrors(); 
				}
			}
		});  // end KeyListener

		doLayout();
	} // end c'tor

	/**
	 * getTasksPopupMenu()
	 * 
	 * @return
	 */
	public JPopupMenu getTasksPopupMenu() {
		// CLEAR ERRORS
		JMenuItem itemClear = new JMenuItem("clear errors");
		itemClear.setFont(defaultFont);
		// need an action listener for each  menu item
		itemClear.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) { 
				//tm.clearErrors(); 
			}
		});

		// CANCEL SELECTED
		JMenuItem itemCancel = new JMenuItem("cancel selected");
		itemCancel.setFont(defaultFont);
		
		// need an action listener for each  menu item
		itemCancel.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int[] rows = getSelectedRows();
				if ( rows.length == 1 ) {
					Task t = (Task)taskModel.getRow(getSelectedRow());
					//TaskManager.getInstance().cancel(t);
				} 
				else {
					ArrayList tasks = new ArrayList(); 
					int[] r = getSelectedRows();
					for (int x=0; x < (r.length); x++) {
						Task t = taskModel.getRow(r[x]);
						log.debug("from model: " + t.getTaskID());
						tasks.add(t);
					}
					//TaskManager.getInstance().cancel(tasks);
				}
			}
		});
		JPopupMenu pop = new JPopupMenu("task list");
		pop.add(itemClear);
		pop.add(itemCancel);
		return pop;
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