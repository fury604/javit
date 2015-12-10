/**
 * NntpArticleHeader.java
 *
 * a data structure to contain the 
 * NNTP article headers in the format
 * prescribed by the OVERVIEW.FMT for
 * a given NNTP server and RFC 2980
 *
 * This class is also Serializable for IO streams.
 */
package net.nexxus.nntp;

import java.io.Serializable;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

@SuppressWarnings("rawtypes")
public class NntpArticleHeader implements Serializable, Comparable {

    private static final long serialVersionUID = 3408278499414524516L;
    private static Logger log = LogManager.getLogger(NntpArticleHeader.class);
    private long id;
	private String subject;
	private String from;
	private String date;
	private String bytes;
	private String lines;
	private boolean isread;
	private String msgID;
	private String refs;
	private String xref;
	private String server;
	private int port;
	private String group;
	private boolean multipart = false;
	private int totalParts;
	private NntpArticlePartID[] parts;
	//private long[] ids;
	private int index = 0;
	private String status;

	public static String STATUS_UNREAD = new String("unread");
	public static String STATUS_READ = new String("read");
	public static String STATUS_QUEUED = new String("queued");
	public static String STATUS_DOWNLOADING = new String("downloading");
	public static String STATUS_WAITING_TO_DECODE = new String("waiting to decode");
	public static String STATUS_DECODING = new String("decoding");
	public static String STATUS_ERROR = new String("error");
	
	// default c'tor
	public NntpArticleHeader() { 
		this.status = STATUS_UNREAD; 
	}
  
	// full c'tor
	public NntpArticleHeader(long id, String subject, String from,
			String date, String msgID, String refs, String bytes, String lines,
			String xref) {

		this.id = id;
		this.subject = subject;
		this.from = from;
		this.date = date;
		this.msgID = msgID;
		this.refs = refs;
		this.bytes = bytes;
		this.lines = lines;
		this.xref = xref;

		this.isread = false;
		this.multipart = false;
		this.totalParts = 1;
		this.status = STATUS_UNREAD;
	}
  
    public long getID() { 
        return this.id; 
    }

    public void setID(long id) { 
		this.id = id; 
	}

    public String getSubject() { 
        return this.subject; 
    }

    public void setSubject(String subject) { 
		this.subject = subject; 
	}

    public String getFrom() { 
        return this.from; 
    }

    public void setFrom(String from) { 
		this.from = from; 
	}

    public String getDate() { 
        return this.date; 
    }

    public void setDate(String date) { 
		this.date = date; 
	}

    public String getMsgID() { 
        return this.msgID; 
    }

    public void setMsgID(String msgID) { 
		this.msgID = msgID; 
	}

    public String getRefs() { 
        return this.refs; 
    }

    public void setRefs(String refs) { 
		this.refs = refs;
	}

    public String getBytes() { 
        return this.bytes; 
    }

    public void setBytes(String bytes) { 
		this.bytes = bytes; 
	}

    public String getLines() { 
        return this.lines; 
    }

    public void setLines(String lines) { 
		this.lines = lines;
	}

    public String getXref() { 
        return this.xref; 
    }

    public void setXref(String xref) { 
		this.xref = xref; 
	}

    public String getServer() { 
        return this.server; 
    }

    public void setServer(String server) { 
		this.server = server; 
	}

    public int getPort() { 
        return this.port; 
    }

    public void setPort(int port) { 
		this.port = port; 
	}

    public String getGroup() { 
        return this.group; 
    }

    public void setGroup(String group) { 
		this.group = group; 
	}

    public String getStatus() { 
        return this.status; 
    }

    public void setStatus(String status) { 
		this.status = status; 
	}
  
	public void setAsMultipart(boolean toggle) { 
		this.multipart = toggle; 
	}

	public void setSource(String server, int port, String group) {
		this.server = server;
		this.port = port;
		this.group = group;
	}

	public int getTotalParts() { 
	    return this.totalParts; 
	}

	public void setTotalParts(int total) {
	    this.totalParts = total;
	    this.parts = new NntpArticlePartID[total];
	}
  
    public void markAsRead(boolean isread) { 
        this.isread = isread; 
    }

    public boolean isRead() { 
        return isread; 
    }
    
    public void addMultiPartID(int pos, long id, String msgId) {
        NntpArticlePartID part = new NntpArticlePartID(id, msgId);
        this.parts[pos] = part;
        
        if (this.multipart == false) {
            this.multipart = true;
        }
    }
    
    public boolean isMultipart() { 
        return this.multipart; 
    }
    
    public NntpArticlePartID[] getParts() { 
        return parts; 
    }

    public String getPartsAsJSON() {
        if (multipart) {
            JSONArray partsArray = new JSONArray();
            for (int x=0; x < parts.length; x++) {
                // incomplete multiparts have null elements
                if (parts[x] != null) {
                    JSONArray part = new JSONArray();
                    part.add(parts[x].getId());
                    part.add(parts[x].getMsgID());
                    partsArray.add(part);
                }
            }
            return partsArray.toJSONString();
        }
        return "";
    }
    
    public void setPartsFromJSON(String json) {
        if (json == null || json.isEmpty()) {
            return;
        }

        JSONParser parser = new JSONParser();
        try {
            Object o = parser.parse(json);
            JSONArray array = (JSONArray)o;
            
            this.parts = new NntpArticlePartID[totalParts];
            
            Iterator iter = array.iterator();
            int x = 0;
            while (iter.hasNext()) {
                JSONArray part = (JSONArray)iter.next();
                long partID = ((Long)part.get(0)).longValue();
                String partMsgID = (String)part.get(1);
                NntpArticlePartID articlePart = new NntpArticlePartID(partID, partMsgID);
                this.parts[x++] = articlePart;
            }
            this.multipart = true;
        }
        catch (Exception e) {
            log.error("failed setting parts from JSON: " + e.getMessage());
            log.debug("ID: " + this.id);
        }
    }

    public int totalMultiparts() {
        if ( multipart ) return parts.length;
        return 0;
    }

    public boolean isComplete() {
        if ( multipart ) {
            for (int x=0; x < parts.length; x++) {
                if (parts[x] == null) return false;
            }
            return true;
        }
        return false;
    }

  
    /**
     * getBytes()
     *
     * provide a byte array representation of this header
    public byte[] getAsBytes() {

        String output = new String();

        //output = output.concat(new String(this.id));
        //output = output.concat("\u0009");

        //output = output.concat(new String(this.subject));
        //output = output.concat("\u0009");

        //output = output.concat(new String(this.from));
        //output = output.concat("\u0009");

        //output = output.concat(new String(this.date));
        //output = output.concat("\u0009");

        output = output.concat(new String(this.bytes));
        output = output.concat("\u0009");

        output = output.concat(new String(this.lines));
        output = output.concat("\u0009");

        output = output.concat(String.valueOf(this.isread));
        output = output.concat("\u0009");

        if ( this.msgID != null ) {
            output = output.concat(new String(this.msgID));
            output = output.concat("\u0009");
        } else {
            output = output.concat("0\u0009");
        }

        if ( this.refs != null ) {
            output = output.concat(new String(this.refs));
            output = output.concat("\u0009");
        } else {
            output = output.concat("0\u0009");
        }

        if ( this.xref != null ) {
            output = output.concat(new String(this.xref));
            output = output.concat("\u0009");
        } else {
            output = output.concat("0\u0009");
        }

        output = output.concat(new String(this.server));
        output = output.concat("\u0009");

        output = output.concat(String.valueOf(this.port));
        output = output.concat("\u0009");

        output = output.concat(new String(this.group));
        output = output.concat("\u0009");

        output = output.concat(String.valueOf(this.multipart));
        output = output.concat("\u0009");

        output = output.concat(String.valueOf(this.totalParts));
        output = output.concat("\u0009");

        
        // new way of doing things.
        if ( this.multipart ) {
            String parts = new String();
            for (int n=0; n < this.totalParts; n++) {
                NntpArticlePartID part = totalPartsTest[n];
                parts = parts.concat(String.valueOf(part.getId()));
                parts = parts.concat("-");
                parts = parts.concat(part.getMsgID());
                parts = parts.concat(":");
            }
            output = output.concat(parts);
            output = output.concat("\u0009");
        }
        else {
            output = output.concat("0\u0009");
        }
        

        output = output.concat(String.valueOf(this.index));
        output = output.concat("\u0009");

        output = output.concat(new String(this.status));

        return output.getBytes();

    }
*/
/*
    public void reconstitute(byte[] bytes) throws Exception {

        String body = new String(bytes);
        String[] parts = body.split("\t");
        try {
            //this.id = parts[0].getBytes();
            //this.subject = parts[1].getBytes();
            //this.from = parts[2].getBytes();
            //this.date = parts[3].getBytes();
            this.bytes = parts[4].getBytes();
            this.lines = parts[5].getBytes();
            this.isread = Boolean.valueOf(parts[6]).booleanValue();
            this.msgID = parts[7].getBytes(); //
            this.refs = parts[8].getBytes();
            this.xref = parts[9].getBytes();
            this.server = parts[10].getBytes();
            this.port = Integer.parseInt(parts[11]);
            this.group = parts[12].getBytes();
            this.multipart = Boolean.valueOf(parts[13]).booleanValue();
            this.totalParts = Integer.parseInt(parts[14]);
            if ( this.multipart ) {
                
                // new way
                String[] ids = parts[15].split(":"); // get string tuples
                int size = ids.length;
                this.totalPartsTest = new NntpArticlePartID[size];
                for ( int x=0; x < size; x++ ) {
                    String myParts[] = ids[x].split("-");
                    long myId = Long.valueOf(myParts[0]);
                    String myMsgId = myParts[1];
                    NntpArticlePartID partID = new NntpArticlePartID(myId, myMsgId);
                    this.totalPartsTest[x] = partID;
                }
                
            }

            this.index = Integer.parseInt(parts[16]);
            this.status = parts[17].getBytes();
        }
        catch (java.lang.ArrayIndexOutOfBoundsException e) {
            System.out.println("OUCH: " + parts.length);
            //e.printStackTrace();
            System.out.println("got out of bounds: " + new String(bytes));
            throw e;
        }
    }
*/
  
    /*
     * compareTo(Object o)
     *
     * implements the Comparable interface
     *
     * override the default CompareTo method from Object
     */
    public int compareTo(Object o) {
        NntpArticleHeader header = (NntpArticleHeader)o; 
        return getSubject().compareTo(header.getSubject()); 
    }

    public boolean equals(Object o) {
        NntpArticleHeader header = (NntpArticleHeader)o; 
        return this.id == header.getID();
    }

}