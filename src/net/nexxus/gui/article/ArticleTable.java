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

import javax.swing.*;
import javax.swing.event.*;

import net.nexxus.event.DownloadArticleEvent;
import net.nexxus.event.GUIEvent;
import net.nexxus.event.GUIEventListener;
import net.nexxus.nntp.NntpArticleHeader;
import net.nexxus.nntp.NntpGroup;
import net.nexxus.util.ApplicationConstants;

import java.awt.event.*;
import java.awt.Color;


public class ArticleTable extends JTable {

	// private members
	public static ArticleTableModel model = new ArticleTableModel();
	private EventListenerList listenerList = new EventListenerList();
	private int bytesWidth = 100;
	private int titleWidth = 900;
	private int dateWidth = 100;
	private int fromWidth = 50;

	public ArticleTable() {
		super();
		setModel(model);
		setBackground(Color.WHITE);
		setDefaultRenderer(NntpArticleHeader.class, new ArticleTableCellRenderer());

		// setup columns
		getColumnModel().getColumn(0).setPreferredWidth(bytesWidth);  // bytes
		getColumnModel().getColumn(1).setPreferredWidth(titleWidth); // title
		getColumnModel().getColumn(2).setPreferredWidth(dateWidth); // date
		getColumnModel().getColumn(3).setPreferredWidth(fromWidth);  // from

		// addMouseListener for right click context
		addMouseListener( new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if ((e.getModifiers() & java.awt.event.InputEvent.BUTTON3_MASK) != 0) {
					JPopupMenu popup = getHeadersPopupMenu();
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});

		// addKeyListener for keyboard shortcuts
		addKeyListener( new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if ( e.getKeyCode() == e.VK_SPACE ) {
					int[] rows = getSelectedRows();
					if (rows.length == 1) {
					    NntpArticleHeader article = (NntpArticleHeader)model.getRow(getSelectedRow());
						DownloadArticleEvent event = new DownloadArticleEvent(article);
						fireEvent(event);
						clearSelection();
					} 
					else {  // multiple articles selected
						for (int x=0; x < (rows.length); x++) {
							DownloadArticleEvent event = new DownloadArticleEvent((NntpArticleHeader)model.getRow(rows[x]));
							fireEvent(event);
						}
						clearSelection();
					}
				}
			}
		});
	}

	/**
	 * getHeadersPopupMenu()
	 *
	 * provides context sensitive menu for Headers Window
	 */
	public JPopupMenu getHeadersPopupMenu() {

	    JMenuItem menuitem = new JMenuItem("download");
		menuitem.setFont(ApplicationConstants.LUCIDA_FONT);

		// need an action listener for each  menu item
		menuitem.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int[] rows = getSelectedRows();
				if (rows.length == 1) {
				    NntpArticleHeader article = (NntpArticleHeader)model.getRow(getSelectedRow());
					DownloadArticleEvent event = new DownloadArticleEvent(article);
					fireEvent(event);
					clearSelection();
				} 
				else {  // multiple articles selected
					for (int x=0; x < (rows.length); x++) {
						DownloadArticleEvent event = 
						        new DownloadArticleEvent((NntpArticleHeader)model.getRow(rows[x]));
						fireEvent(event);
					}
					clearSelection();
				}
			}
		});

		// now create the popup window object
		JPopupMenu popup = new JPopupMenu("download articles");
		popup.add(menuitem);
		return popup;

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
