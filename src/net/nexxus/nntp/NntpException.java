
package net.nexxus.nntp;

@SuppressWarnings("serial")
public class NntpException extends Exception {

    private int code = 0;
    
    public NntpException() { 
    	super(); 
    }
    
    public NntpException(String s) { 
    	super(s); 
    }
    
    public void setResponseCode(int code) {
        this.code = code;
    }
    
    public int getResponseCode() {
        return this.code;
    }
    
}
