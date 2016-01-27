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

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.nexxus.event.EventListenerImpl;
import net.nexxus.nntp.NntpArticleHeader;
import net.nexxus.nntp.NntpGroup;
import net.nexxus.nntp.NntpServer;

public class DBManagerImpl extends EventListenerImpl implements DBManager {
    
    private static Logger log = LogManager.getLogger(DBManagerImpl.class);
    private String ormConfig = "mybatis-config.xml";
    private SqlSessionFactory sqlFactory;
    private String groupsTable = "groups_subscribed";
    private String serverTable = "servers";
    
    public DBManagerImpl(Properties p) {
        // get setup here
        try {
            InputStream is = Resources.getResourceAsStream(ormConfig);
            sqlFactory = new SqlSessionFactoryBuilder().build(is,p);
        }
        catch (Exception e) {
            log.error("could not load myBatis config: " + e.getMessage());
        }
    }
    
    // ArticleHeader methods ////
    
    /**
     * return a List of headers using an optional cutoff which
     * can be null
     */
    public List<NntpArticleHeader> getHeaders(NntpGroup group, Integer cutoff) {
        List resultSet = null;
        try {
            String cutoffPoint;
            HashMap map = new HashMap();
            String table = DBUtils.convertGroup(group.getName());
            map.put("table", table);
            SqlSession session = sqlFactory.openSession();

            if (cutoff == null || cutoff.equals(Integer.valueOf(0))) {
                log.debug("not using cutoff selecting headers");
                resultSet = session.selectList("getHeadersLite", map);
            }
            else {
                cutoffPoint = this.calculateCutoff(cutoff);
                log.debug("using " + cutoffPoint + " as cutoff for selecting headers");
                map.put("cutoff", cutoffPoint);
                resultSet = session.selectList("getHeadersRangeLite", map);
            }
            
            session.close();
        }
        catch (Exception e) {
            log.error("failed loading headers from DB: " + e.getMessage());
            e.printStackTrace();
        }
        
        return resultSet;
    }
    
    /**
     * retrieve a header from the DB
     */
    public NntpArticleHeader getHeader(NntpGroup group, NntpArticleHeader header) throws Exception {
        try {
            String table = DBUtils.convertGroup(group.getName());
            HashMap map = new HashMap();
            map.put("table", table);
            map.put("id", header.getID());
            SqlSession session = sqlFactory.openSession();
            header = session.selectOne("getHeader", map);
            session.close();
        }
        catch (Exception e) {
            log.error("failed retrieving header from DB: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        
        return header;
    }
    
    public void addHeader(NntpArticleHeader header) throws Exception {
        try {
            SqlSession session = sqlFactory.openSession(true);
            HashMap map = DBUtils.mapHeader(header);
            session.insert("insertHeader", map);
            session.close();
        }
        catch (Exception e) {
            log.error("could not add header to database: " + e.getMessage());
            //e.printStackTrace();
            throw e;
        }
    }
    
    public void updateHeader(NntpArticleHeader header) {
        try {
            SqlSession session = sqlFactory.openSession();
            HashMap map = DBUtils.mapHeaderForUpdate(header);
            session.update("updateStatus", map);
            session.commit();
            session.close();
        }
        catch (Exception e) {
            log.error("could not update header in database: " + e.getMessage());
        }
        
    }
    
    public void removeHeader(NntpArticleHeader header) {
        
    }
    
    // NntpGroup methods ////
    
    /**
     * Methods that handle the internal Groups table
     * of subscribed groups
     */
    
    /**
     * create the NntpGroup List table to keep track of groups
     * that are being used
     */
    public void createServerGroups() throws Exception {
        SqlSession session = sqlFactory.openSession();
        session.insert("createGroupsTable", groupsTable);
        session.commit();
        session.close();
    }
    
    /** 
     * update the given NntpGroup to have its min and max article id's
     * set
     */
    public NntpGroup getGroupMinMax(NntpGroup group) throws Exception {
        SqlSession session = sqlFactory.openSession();
        HashMap<Long,Long> map = session.selectOne("getMinMax", DBUtils.convertGroup(group.getName()));
        if (map == null || map.isEmpty()) {
            group.setLowID(0L);
            group.setHighID(0L);
        }
        else {
            Long min = map.get("min");
            Long max = map.get("max");
            group.setLowID(min.longValue());
            group.setHighID(max.longValue());
        }
        
        return group;
    }
    
    /**
     * add an NntpGroup to our groups list
     */
    public void addGroup(NntpGroup group) throws Exception {
        HashMap<String,String> map = new HashMap<String,String>();
        map.put("table", groupsTable);
        map.put("server", group.getServer());
        map.put("name", group.getName());
        
        SqlSession session = sqlFactory.openSession();
        session.insert("addGroup", map);
        
        // now ensure there is an ArticleHeader table for it
        session.insert("createHeaderTable", DBUtils.convertGroup(group.getName()));
        
        session.commit();
        session.close();
    }
    
    /**
     * remove an NntpGroup from our groups list
     */
    public void removeGroup(NntpGroup group) throws Exception {
        HashMap<String,String> map = new HashMap<String,String>();
        map.put("table", groupsTable);
        map.put("name", group.getName());
        
        SqlSession session = sqlFactory.openSession();
        session.insert("removeGroup", map);
        session.commit();
        session.close();
    }
    
    /**
     * return a List of NntpGroups we are subscribed to
     */
    public List<NntpGroup> getGroups() throws Exception {
        SqlSession session = sqlFactory.openSession();
        List resultSet = session.selectList("getGroups", this.groupsTable);
        session.close();
        
        return resultSet;
    }

     // Methods that handle NntpServer group lists
    
    /**
     * create Server group list table
     * @throws Exception
     */
    public void createServerGroupList() throws Exception {
        SqlSession session = sqlFactory.openSession();
        session.insert("createGroupListTable", groupsTable);
        session.commit();
        session.close();
    }
    
    /**
     * add an NntpGroup to an NntpServers group listing
     * @param group
     * @throws Exception
     */
    public void addServerGroup(NntpGroup group) throws Exception {
        HashMap map = new HashMap();
        map.put("name", group.getName());
        map.put("hi", group.getHighID());
        map.put("low", group.getLowID());
        
        SqlSession session = sqlFactory.openSession();
        session.insert("addGroupToGroupList", map);
        session.commit();
        session.close();
    }
    
    public void addServerGroups(List<NntpGroup> groups) throws Exception {
        HashMap map = new HashMap();
        SqlSession session = sqlFactory.openSession();
        Iterator<NntpGroup> iter = groups.iterator();
        while (iter.hasNext()) {
            NntpGroup group = iter.next();
            map.put("name", group.getName());
            map.put("hi", group.getHighID());
            map.put("low", group.getLowID());
            session.insert("addGroupToGroupList", map);
        }
        session.commit();
        session.close();
    }
    
    /**
     * return a List of NntpGroups belonging to an NntpServer
     * @return
     * @throws Exception
     */
    public List<NntpGroup> getServerGroups() throws Exception {
        SqlSession session = sqlFactory.openSession();
        List resultSet = session.selectList("getGroupList");
        session.close();
        
        return resultSet;
    }
    
    // NntpServer methods ////
    
    /**
     * create the needed DB table to hold our 
     * NntpServer entities 
     */
    public void createServerTable() throws Exception {
        SqlSession session = sqlFactory.openSession();
        session.insert("createServerTable", serverTable);
        session.commit();
        session.close();
    }
    
    /**
     * add an NntpGroup to our groups list
     */
    public void addServer(NntpServer server) throws Exception {
        HashMap map = new HashMap();
        map.put("table", serverTable);
        map.put("server", server.getServer());
        map.put("port", server.getPort());
        map.put("username", server.getUsername());
        map.put("password",  server.getPassword());
        
        SqlSession session = sqlFactory.openSession();
        session.insert("addServer", map);
        session.commit();
        session.close();
    }

    public NntpServer getServer() throws Exception {
        SqlSession session = sqlFactory.openSession();
        NntpServer server = (NntpServer)session.selectOne("getServer", this.serverTable);
        session.close();
        
        return server;
    }
    
    /**
     * remove an NntpServer from our groups list
     */
    public void removeServer(NntpServer server) throws Exception {
        HashMap<String,String> map = new HashMap<String,String>();
        map.put("table", serverTable);
        map.put("server", server.getServer());
        
        SqlSession session = sqlFactory.openSession();
        session.insert("removeServer", map);
        session.commit();
        session.close();
    }

    // Utility methods follow ////

    private String calculateCutoff(Integer cutoff) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, -(cutoff.intValue() * 24));
        Date cutoffPoint = cal.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formatted = dateFormat.format(cutoffPoint);
        return formatted;
    }

    private ArrayList<NntpArticleHeader> parseResultSet(List resultSet) {
        ArrayList<NntpArticleHeader> headers = new ArrayList<NntpArticleHeader>();
        try {
            Iterator iter = resultSet.iterator();
            while (iter.hasNext()) {
                Map entry = (Map)iter.next();
                NntpArticleHeader header = new NntpArticleHeader();
                header.setID((long)entry.get("id"));
            }
        }
        catch (Exception e) {
            log.error("failed parsing resultSet from DB: " + e.getMessage());
        }
        
        return headers;
    }
}
