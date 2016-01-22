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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.EventListenerList;

import net.nexxus.event.AddServerDialogEvent;
import net.nexxus.event.GUIEvent;
import net.nexxus.event.GUIEventListener;
import net.nexxus.util.ApplicationConstants;

public class RootNodePopupMenu extends JPopupMenu {

    private static EventListenerList listenerList = new EventListenerList();
    
    public RootNodePopupMenu(String label, EventListenerList listenerList) {
        super(label);
        this.listenerList = listenerList;
        this.add(getAddServerMenuItem());
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
    
    private JMenuItem getAddServerMenuItem() {
        JMenuItem menuItem = new JMenuItem("add server");
        menuItem.setFont(ApplicationConstants.LUCIDA_FONT);
        menuItem.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                AddServerDialogEvent event = new AddServerDialogEvent("add");
                fireEvent(event);
            }
        });
        
        return menuItem;
    }
}
