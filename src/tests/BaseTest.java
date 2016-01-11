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
package tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

import net.nexxus.db.DBManager;
import net.nexxus.db.DBManagerImpl;
import net.nexxus.nntp.NntpArticleHeader;
import net.nexxus.nntp.NntpGroup;
import net.nexxus.nntp.NntpServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;

public class BaseTest {

    private static Logger log = LogManager.getLogger(BaseTest.class);
            
    protected String testHeadersFile = "tests/resources/xover_output.txt"; 
    protected String downloadDir;
    protected String cacheDir;
    
    protected NntpServer nServer; 
    protected NntpGroup nGroup; 
    protected DBManager dbManager;
    
    
    /*
     * set up the state for out essential test
     * classes using a properties file
     * 
     * this is run before every test invocation
     */
    @Before
    public void setup() {
        log.debug("in before");
        try {
            Properties p = new Properties();
            InputStream conf = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.properties");
            p.load(conf);
            
            String nntpServer = p.getProperty("nntp_server");
            int nntpPort = Integer.valueOf(p.getProperty("nntp_port"));
            String nntpUser = p.getProperty("nntp_user");
            String nntpPass = p.getProperty("nntp_password");
            String nntpGroup = p.getProperty("nntp_group");
            
            this.nServer = new NntpServer(nntpServer, nntpPort, nntpUser, nntpPass);
            this.nGroup = new NntpGroup(nntpServer, nntpPort, nntpGroup, 0);
            
            this.downloadDir = p.getProperty("download_dir");
            this.cacheDir = p.getProperty("cache_dir");
            
            this.dbManager = new DBManagerImpl(p);
            
        }
        catch (Exception e) {
            log.error("failed loading test properties: " + e.getMessage());
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            URL[] urls = ((URLClassLoader)cl).getURLs();
            for (URL url: urls) {
                log.debug(url.getFile());
            }
        }
        
    }
    
    /*
     * use a test file to simulate fetching headers
     * from an NNTP server
     */
    public BufferedReader getTestHeaders() throws Exception {
        URL to_dir = ClassLoader.getSystemResource(testHeadersFile);
        Assert.assertNotNull(to_dir);
        File xover = new File (to_dir.toURI());
        Assert.assertNotNull(xover);
        BufferedReader reader = new BufferedReader(new FileReader(xover));
        
        return reader;
    }

    /*
     * simple routine to validate all important attributes
     * of an NntpArticleHeader
     */
    protected void validateHeader(NntpArticleHeader header) {
        Assert.assertNotNull("header id was null", header.getID());
        Assert.assertNotNull("subject was null", header.getSubject());
        Assert.assertFalse("subject was empty", header.getSubject().isEmpty());
        Assert.assertNotNull("from was null", header.getFrom());
        Assert.assertFalse("from was empty", header.getFrom().isEmpty());
        Assert.assertNotNull("date was null", header.getDate());
        Assert.assertFalse("date was empty", header.getDate().isEmpty());
        Assert.assertNotNull("msgID was null", header.getMsgID());
        Assert.assertFalse("msgID was empty", header.getMsgID().isEmpty());
        Assert.assertNotNull("bytes was null", header.getBytes());
        Assert.assertFalse("bytes was empty", header.getBytes().isEmpty());
        Assert.assertNotNull("status was null", header.getStatus());
        Assert.assertFalse("status was empty", header.getStatus().isEmpty());
        Assert.assertNotNull("total parts was null", header.getTotalParts());
        //if (header.isMultipart() && header.isComplete()) 
        if (header.isMultipart()) {
            //Assert.assertTrue("multipart header was not complete", header.isComplete());
            Assert.assertNotNull("parts as JSON was null", header.getPartsAsJSON());
            Assert.assertFalse("parts as JSON was empty", header.getPartsAsJSON().isEmpty());
        }
        Assert.assertNotNull("server was null", header.getServer());
        Assert.assertFalse("server was empty", header.getServer().isEmpty());
        Assert.assertNotNull("port was null", header.getPort());
        Assert.assertNotNull("group was null", header.getGroup());
        Assert.assertFalse("group was empty", header.getGroup().isEmpty());
    }
    
}
