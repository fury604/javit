package net.nexxus.db;

import java.util.List;

import net.nexxus.event.EventListenerInterface;
import net.nexxus.nntp.NntpArticleHeader;
import net.nexxus.nntp.NntpGroup;
import net.nexxus.nntp.NntpServer;

public interface DBManager extends EventListenerInterface {
    
    public List<NntpArticleHeader> getHeaders(NntpGroup group, Integer cutoff);
    
    public NntpArticleHeader getHeader(NntpGroup group, NntpArticleHeader header) throws Exception;
    
    public void addHeader(NntpArticleHeader header) throws Exception;
    
    public void updateHeader(NntpArticleHeader header);
    
    public void removeHeader(NntpArticleHeader header);
    
    public NntpGroup getGroupMinMax(NntpGroup group) throws Exception;
    
    public void createServerGroups() throws Exception;
    
    public List<NntpGroup> getGroups() throws Exception;
    
    public void addGroup(NntpGroup group) throws Exception;
    
    public void removeGroup(NntpGroup group) throws Exception;
    
    public void createServerTable() throws Exception;
    
    public void addServer(NntpServer server) throws Exception;
    
    public NntpServer getServer() throws Exception;
    
    public void removeServer(NntpServer server) throws Exception;
    
}
