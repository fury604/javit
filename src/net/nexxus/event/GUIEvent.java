/**
 * GUIevent.java
 *
 * subclass of java.util.EventObject to
 * propogate GUI events
 */

package net.nexxus.event;

import java.util.EventObject;

@SuppressWarnings("serial")
public class GUIEvent extends EventObject {

    public GUIEvent(Object source) { super(source); }

} 
