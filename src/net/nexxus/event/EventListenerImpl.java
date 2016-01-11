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
package net.nexxus.event;

import javax.swing.event.EventListenerList;

public class EventListenerImpl implements EventListenerInterface {

    private EventListenerList listenerList = new EventListenerList();

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
