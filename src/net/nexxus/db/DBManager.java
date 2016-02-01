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
package net.nexxus.db;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import net.nexxus.event.EventListenerInterface;
import net.nexxus.nntp.NntpArticleHeader;
import net.nexxus.nntp.NntpGroup;
import net.nexxus.nntp.NntpServer;

public interface DBManager extends EventListenerInterface {
    
    // ArticleHeader methods ////
    
    public List<NntpArticleHeader> getHeaders(NntpGroup group, Integer cutoff);
    
    public NntpArticleHeader getHeader(NntpGroup group, NntpArticleHeader header) throws Exception;
    
    public void addHeader(NntpArticleHeader header) throws Exception;
    
    public void updateHeader(NntpArticleHeader header);
    
    public void removeHeader(NntpArticleHeader header);
    
    // NntpGroup methods ////

    public void createServerGroups() throws Exception;
    
    public NntpGroup getGroupMinMax(NntpGroup group) throws Exception;
    
    public List<NntpGroup> getGroups() throws Exception;
    
    public void addGroup(NntpGroup group) throws Exception;
    
    public void updateGroup(NntpGroup group) throws Exception;
    
    public void removeGroup(NntpGroup group) throws Exception;
    
    public void createServerGroupList() throws Exception;
    
    public void addServerGroup(NntpGroup group) throws Exception;
    
    public void addServerGroups(List<NntpGroup> groups) throws Exception;
    
    public List<NntpGroup> getServerGroups() throws Exception;

    // NntpServer methods ////
    
    public void createServerTable() throws Exception;
    
    public void addServer(NntpServer server) throws Exception;
    
    public NntpServer getServer() throws Exception;
    
    public void removeServer(NntpServer server) throws Exception;
    
    public void sanityCheck() throws Exception;
    
}
