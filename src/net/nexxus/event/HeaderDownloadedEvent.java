/**
 * HeaderDownloadedEvent.java
 *
 * an implementation of an Event 
 * used to signal a header has been downloaded.
 *
 * This event is used when updating headers from the server
 * and is used by the UpdateHeadersTask for brokering status
 * between the NntpClient and the TaskInfoPanel.
 */

package net.nexxus.event;

public class HeaderDownloadedEvent extends GUIEvent {
  
    private String group = new String();
    private int num = 0;
    private long total = 0;

    public HeaderDownloadedEvent(String group, int num, long total) {
        super(new Object());
        this.group = group;
        this.num = num;
        this.total = total;
    }

    public int getNumber() { return this.num; }
    public long getTotal() { return this.total; }

}
