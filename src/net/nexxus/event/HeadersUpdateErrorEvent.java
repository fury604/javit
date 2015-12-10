/**
 * HeadersUpdateErrorEvent.java
 *
 * an implementation of an Event 
 * used to indicate and error 
 * while updating headers
 */

package net.nexxus.event;

public class HeadersUpdateErrorEvent extends GUIEvent {

    public HeadersUpdateErrorEvent(Object source) {
    	super(source);
    }

}
