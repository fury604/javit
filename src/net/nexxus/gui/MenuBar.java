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

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.EventListenerList;

import net.nexxus.event.AddServerDialogEvent;
import net.nexxus.event.AutoUpdatesEvent;
import net.nexxus.event.GUIEvent;
import net.nexxus.event.GUIEventListener;
import net.nexxus.event.SystemExitEvent;
import net.nexxus.util.ApplicationConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class MenuBar extends JMenuBar {

	private static Logger log = LogManager.getLogger(MenuBar.class.getName());
	private static JMenu file = new JMenu("File");

	private static JMenuItem server = new JMenuItem("Add Server");  
	private static JMenuItem update = new JMenuItem("Update Now");
	private static JMenuItem quit = new JMenuItem("Exit");

	private static EventListenerList listenerList = new EventListenerList();

	public MenuBar() {
		super();
		
		// set up fonts
		file.setFont(ApplicationConstants.LUCIDA_FONT);
		server.setFont(ApplicationConstants.LUCIDA_FONT);
		update.setFont(ApplicationConstants.LUCIDA_FONT);
		quit.setFont(ApplicationConstants.LUCIDA_FONT);


		server.addActionListener(new ActionListener(  ) {
			public void actionPerformed(ActionEvent e) { 
				AddServerDialogEvent event = new AddServerDialogEvent(this);
				fireEvent(event);
			}
		});
		update.addActionListener(new ActionListener(  ) {
			public void actionPerformed(ActionEvent e) { 
				AutoUpdatesEvent event = new AutoUpdatesEvent(this);
				fireEvent(event);
			}
		});
		quit.addActionListener(new ActionListener(  ) {
			public void actionPerformed(ActionEvent e) { 
				SystemExitEvent event = new SystemExitEvent("exit");
				fireEvent(event);
			}
		});

		file.add(server);
		file.add(update);
		file.add(quit);
		add(file);
	}

   public void addGUIEventListener(GUIEventListener listener) {
        listenerList.add(GUIEventListener.class, listener);
    }

	protected void fireEvent(GUIEvent event) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==GUIEventListener.class) {
				((GUIEventListener)listeners[i+1]).eventOccurred(event);
			}
		}
	}

}
