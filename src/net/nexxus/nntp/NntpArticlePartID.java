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
 * Simple class to hold both the article ID and the MsgID
 * for a given NntpArticleHeader
 */
package net.nexxus.nntp;

public class NntpArticlePartID {
    
    private long id;
    private String msgID;
    
    //
    public NntpArticlePartID() {
    }
    
    public NntpArticlePartID(Long id, String msgID) {
        this.id = id;
        this.msgID = msgID;
    }

    public long getId() {
        return id;
    }

    public String getIdAsString() {
        return String.valueOf(id);
    }
    
    public void setId(long id) {
        this.id = id;
    }

    public String getMsgID() {
        return msgID;
    }

    public void setMsgID(String msgID) {
        this.msgID = msgID;
    }

}
