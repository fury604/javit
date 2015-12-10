/**
 * HeadersUpdatedEvent.java
 *
 * an implementation of an Event 
 * used to request a header update
 */

package net.nexxus.event;

public class HeadersUpdatedEvent extends GUIEvent {

    public HeadersUpdatedEvent(Object source) {
    	super(source);
    }

}
