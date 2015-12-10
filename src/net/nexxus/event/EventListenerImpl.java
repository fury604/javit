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
