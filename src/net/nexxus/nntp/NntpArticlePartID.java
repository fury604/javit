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
