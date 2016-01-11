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
