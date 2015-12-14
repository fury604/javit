package net.nexxus.db;

import java.util.List;

import net.nexxus.event.EventListenerInterface;
import net.nexxus.nntp.NntpArticleHeader;
import net.nexxus.nntp.NntpGroup;

public interface DBManager extends EventListenerInterface {
    
    public List<NntpArticleHeader> getHeaders(NntpGroup group, Integer cutoff);
    
    public NntpArticleHeader getHeader(NntpGroup group, NntpArticleHeader header) throws Exception;
    
    public void addHeader(NntpArticleHeader header) throws Exception;
    
    public void updateHeader(NntpArticleHeader header);
    
    public void removeHeader(NntpArticleHeader header);
    
    public NntpGroup getGroupMinMax(NntpGroup group) throws Exception;
    
    public void createServerGroups() throws Exception;
    
    public void addGroup(NntpGroup group) throws Exception;
    
    public void removeGroup(NntpGroup group) throws Exception;
    
}
