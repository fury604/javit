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
package tests.db;

import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import net.nexxus.db.DBManager;
import net.nexxus.db.DBManagerImpl;
import net.nexxus.nntp.NntpArticleHeader;
import net.nexxus.nntp.NntpGroup;
import net.nexxus.nntp.NntpServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tests.BaseTest;

@RunWith(JUnit4.class)
public class DBManagerImplTest extends BaseTest {
    
    private static Logger log = LogManager.getLogger(DBManagerImplTest.class);
    
    /*
     * fetch all headers from the DB then
     * validate them
     */
    @Test
    @Ignore
    public void testGetHeaders() {
        List<NntpArticleHeader> headers = getHeaders();
        Iterator<NntpArticleHeader> iter = headers.iterator();
        while (iter.hasNext()) {
            NntpArticleHeader header = iter.next();
            try {
                header = getHeader(header);
                this.validateHeader(header);
            }
            catch (Exception e) {
                log.error("failed retrieving header: " + e.getMessage());
                Assert.fail("failed retrieving header from DB: " + e.getMessage());
            }
        }
    }
    
    @Test
    @Ignore
    public void testUpdateHeaders() {
        List<NntpArticleHeader> headers = getHeaders();
        
        Iterator<NntpArticleHeader> iter = headers.iterator();
        while (iter.hasNext()) {
            NntpArticleHeader header = iter.next();
            header.setStatus(NntpArticleHeader.STATUS_READ);
            dbManager.updateHeader(header);
        }
    }
    
    /*
     * get the minimum and maximum article ID values
     * for a given group
     * 
     * this is useful to determine the article range
     * to present with an XOVER command
     */
    @Test
    public void testMinMax() {
        try { 
            nGroup = dbManager.getGroupMinMax(nGroup);
            Assert.assertTrue("low ID is zero", nGroup.getLowID() != 0);
            Assert.assertTrue("high ID is zero", nGroup.getHighID() != 0);
            
        }
        catch (Exception e) {
            log.error("failed fetching man max values: " + e.getMessage());
            Assert.fail("failed fetching man max values: " + e.getMessage());
        }
    }
    
    // Server Group Tests //
    
    @Test
    public void testCreateServerGroups() {
        try {
            dbManager.createServerGroups();
        }
        catch (Exception e) {
            Assert.fail("failed creating server groups: " + e.getMessage());
        }
    }
    
    @Test
    public void testAddGroup() {
        try {
            dbManager.addGroup(nGroup);
        }
        catch (Exception e) {
            Assert.fail("failed adding Group: " + e.getMessage());
        }
    }
    
    @Test
    public void testUpdateGroup() {
        try {
            nGroup.setAutoUpdate(true);
            dbManager.updateGroup(nGroup);
        }
        catch (Exception e) {
            Assert.fail("failed updating Group: " + e.getMessage());
        }
    }

    @Test
    public void testGetGroups() {
        try {
            List<NntpGroup> groups = dbManager.getGroups();
            Assert.assertTrue("groups list was empty", groups.size() > 0);
        }
        catch (Exception e) {
            Assert.fail("failed retreiving groups: " + e.getMessage());
        }
    }
    
    @Test
    public void testRemoveGroup() {
        try {
            dbManager.removeGroup(nGroup);
        }
        catch (Exception e) {
            Assert.fail("failed removing Group: " + e.getMessage());
        }
    }
    
    @Test
    public void testCreateServerGroupList() {
        try {
            dbManager.createServerGroupList();
        }
        catch (Exception e) {
            Assert.fail("failed creating server_groups table: " + e.getMessage());
        }
    }
    
    @Test
    @Ignore
    public void testAddGroupToGroupList() {
        try {
            dbManager.addServerGroup(nGroup);
        }
        catch (Exception e) {
            Assert.fail("failed adding group to server_groups table: " + e.getMessage());
        }
    }
    
    @Test
    @Ignore
    public void testGetServerGroupList() {
        try {
            List<NntpGroup> groups = dbManager.getServerGroups();
            Assert.assertNotNull(groups);
        }
        catch (Exception e) {
            Assert.fail("failed retrieving groups from server_groups table: " + e.getMessage());
        }
    }
    
    
    // Server Table tests //
    
    @Test
    public void testCreateServerTable() {
        try {
            dbManager.createServerTable();
        }
        catch (Exception e) {
            Assert.fail("failed creating server table: " + e.getMessage());
        }
    }

    @Test
    @Ignore
    public void testAddServer() {
        try {
            dbManager.addServer(nServer);
        }
        catch (Exception e) {
            Assert.fail("failed adding Server: " + e.getMessage());
        }
    }
    
    @Test
    @Ignore
    public void testGetServer() {
        try {
            NntpServer server = dbManager.getServer();
            Assert.assertNotNull("server was null", server);
        }
        catch (Exception e) {
            Assert.fail("failed retreiving server: " + e.getMessage());
        }
    }
    
    @Test
    @Ignore
    public void testRemoveServer() {
        try {
            dbManager.removeServer(nServer);
        }
        catch (Exception e) {
            Assert.fail("failed removing Server: " + e.getMessage());
        }
    }
    
    
    // utility methods
    
    // get all headers
    private List<NntpArticleHeader> getHeaders() {
        List<NntpArticleHeader> headers = dbManager.getHeaders(nGroup, null);
        Assert.assertNotNull("headers array was null", headers);
        Assert.assertFalse("headers had zero elements", headers.size() == 0);

        return headers;
    }
    
    // get a single header
    private NntpArticleHeader getHeader(NntpArticleHeader header) 
    throws Exception {
        header = dbManager.getHeader(nGroup, header);
        return header;
    }

}
