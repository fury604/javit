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

import javax.swing.*;
//import javax.swing.table.*;
import javax.swing.event.EventListenerList;
import java.awt.event.*;
import java.awt.*;
//import java.util.ArrayList;

import net.nexxus.event.GUIEvent;
import net.nexxus.event.GUIEventListener;
import net.nexxus.event.SubscribeGroupEvent;
import net.nexxus.nntp.NntpGroup;
import net.nexxus.util.ApplicationConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class GroupsListPanel extends JTable {

	private static Logger log = LogManager.getLogger(GroupsListPanel.class.getName());
	private static GroupsListTableModel groupsModel = new GroupsListTableModel();
	private static EventListenerList listenerList = new EventListenerList();
	private static int DEFAULT_WIDTH_NAME = 400;
	private static int DEFAULT_WIDTH_COUNT = 40;

	/**
	 */
	public GroupsListPanel() {

		super();
		setModel(groupsModel);
		setDefaultRenderer(String.class, new GroupsListTableCellRenderer());
		getColumnModel().getColumn(0).setPreferredWidth(DEFAULT_WIDTH_NAME); 
		getColumnModel().getColumn(1).setPreferredWidth(DEFAULT_WIDTH_COUNT);

		// addMouseListener for right click context
		addMouseListener( new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if ((e.getModifiers() & java.awt.event.InputEvent.BUTTON3_MASK) != 0) {
				    JPopupMenu popup = getGroupListPopupMenu();
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});

		addKeyListener( new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if ( e.getKeyCode() == KeyEvent.VK_SPACE ) {
				    NntpGroup group = (NntpGroup)groupsModel.getRow(getSelectedRow());
					SubscribeGroupEvent event = new SubscribeGroupEvent(group);
					fireEvent(event);
				}
			}
		});

		// watch for actions on the table columns
		getTableHeader().addMouseListener( new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int colIdx = getColumnModel().getColumnIndexAtX(e.getX());
				// article count selected
				if ( colIdx == 0 ) {
					groupsModel.sortByGroup();
				}
				if ( colIdx == 1 ) {
					groupsModel.sortByCount();
				}
			}
		});
	}

	/**
	 * getGroupListPopupMenu()
	 *
	 * provides context sensitive menu for Groups Window
	 */
	private JPopupMenu getGroupListPopupMenu() {
	    JMenuItem menuitem = new JMenuItem("subscribe");
		menuitem.setFont(ApplicationConstants.LUCIDA_FONT);

		// need an action listener for each  menu item
		menuitem.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
			    NntpGroup group = (NntpGroup)groupsModel.getRow(getSelectedRow());
				// now create event
				SubscribeGroupEvent event = new SubscribeGroupEvent(group);
				fireEvent(event);
			}
		});

		// now create the popup window object
		JPopupMenu popup = new JPopupMenu("subscribe to group");
		popup.add(menuitem);
		return popup;
	}

	public GroupsListTableModel getTableModel() { 
		return groupsModel; 
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
