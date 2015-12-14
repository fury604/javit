package tests.nntp;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.nexxus.db.DBManager;
import net.nexxus.db.DBManagerImpl;
import net.nexxus.db.DBUtils;
import net.nexxus.event.HeaderDownloadedEvent;
//import net.nexxus.cache.CacheManager;
import net.nexxus.nntp.NntpArticleHeader;
import net.nexxus.nntp.NntpArticlePartID;
import net.nexxus.nntp.NntpClientV2;
import net.nexxus.nntp.NntpGroup;
import net.nexxus.nntp.NntpServer;
import net.nexxus.util.DateHelper;
//import net.nexxus.util.ComponentManager;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tests.BaseTest;

@RunWith(JUnit4.class)
public class NntpClientV2Test extends BaseTest {

	private static Logger log = LogManager.getLogger(NntpClientV2Test.class.getName());
	private NntpClientV2 client;
	
	@Test
	public void testConnect() {
	    client = new NntpClientV2(nServer, dbManager);
	    try {
	        client.connect();
	        client.disconnect();
	    }
	    catch (Exception e) {
	        Assert.fail("failed connecting to server: " + e.getMessage());
	    }
	    
	}
	
    @Test
    @Ignore
    public void testGetHeaders() {
        client = new NntpClientV2(nServer, dbManager);
        try {
        	//nGroup.setLowID(12820948L);
        	//nGroup.setHighID(8528471442L);
            client.connect();
            client.getHeaders( nGroup );
            client.disconnect();
        }
        catch (Exception e) {
            log.info("test failed: " + e.getMessage());
            e.printStackTrace();
            Assert.assertTrue(false);
        }
    }

    @Test
    @Ignore
    public void testGetArticle() {
        client = new NntpClientV2(nServer, dbManager);
        try {
            List<NntpArticleHeader> headers = dbManager.getHeaders( nGroup, null );
            
            // choose a header to download
            Iterator<NntpArticleHeader> iter = headers.iterator();
            while(iter.hasNext()) {
                NntpArticleHeader header = iter.next();
                // load up the full header
                header = dbManager.getHeader(nGroup, header);
                // look for a suitable article
                if (header.isMultipart() && header.isComplete() && 
                    header.getTotalParts() < 3 && header.getTotalParts() > 0) {
                    NntpArticlePartID[] ids = header.getParts();
                    try {
                        log.info("downloading article: " + header.getID());
                        log.info("article subject: " + header.getSubject());
                        for (int x=0; x < ids.length; x++) {
                            File cache = new File("/tmp/foo-" + x + ".test");
                            client.connect();
                            client.setGroup(nGroup);
                            client.getArticleBody(ids[x], cache );
                            client.disconnect();
                        }
                        break;
                    }
                    catch (Exception e) {
                        log.error("failed retreiving article bodies " + e.getMessage());
                        Assert.assertTrue(false);
                    }
                }
                /*
                else {
                    try {
                        File cache = new File("/tmp/foo.test");
                        client.getArticleBody(Long.parseLong(header.getID()), cache );
                    }
                    catch (Exception e) {
                        Assert.assertTrue(false);
                    }
                }
                */
            }        
            
        }
        catch (Exception e) {
            Assert.fail("blew up somehow: " + e.getMessage());
        }
    }
    
    @Test
    @Ignore
    public void testGetSpecificArticle() {
        client = new NntpClientV2(nServer, dbManager);
        
        NntpArticleHeader header = new NntpArticleHeader();
        header.setID(61650781L);
        try {
            // load up the full header
            header = dbManager.getHeader(nGroup, header);
            // look for a suitable article
            if (header.isMultipart() && header.isComplete()) {
                NntpArticlePartID[] ids = header.getParts();
                try {
                    log.info("downloading article: " + header.getID());
                    log.info("article subject: " + header.getSubject());
                    for (int x=0; x < ids.length; x++) {
                        client.connect();
                        client.setGroup(nGroup);
                        try {
                            File cache = new File("/tmp/foo-" + x + ".test");
                            client.getArticleBody(ids[x], cache );
                        }
                        catch (Exception ne) {
                            log.error("could not download article part: " + ne.getMessage());
                        }
                        client.disconnect();
                    }
                }
                catch (Exception e) {
                    log.error("failed retreiving article bodies " + e.getMessage());
                    Assert.fail("failed retrieving parts: " + e.getMessage());
                }
            }
            /*
                else {
                    try {
                        File cache = new File("/tmp/foo.test");
                        client.getArticleBody(Long.parseLong(header.getID()), cache );
                    }
                    catch (Exception e) {
                        Assert.assertTrue(false);
                    }
                }
             */
        }
        catch (Exception e) {
            Assert.fail("blew up somehow: " + e.getMessage());
        }
    }
    
    
    @Test
    @Ignore
    public void testGetGroups() {
        client = new NntpClientV2(nServer, dbManager);
        try {
            client.connect();
            String groups = client.getGroupList( nServer );
            Assert.assertNotNull( "groupList was null",groups );
            Assert.assertFalse(groups.isEmpty());
            log.info("groups list had " + groups.length() + " entries");
        }
        catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    @Ignore
    public void testUpdateHeaderState() {
        client = new NntpClientV2(nServer, dbManager);
        try {
            client.connect();
            client.setGroup(nGroup);
            client.disconnect();
            
            System.out.println("high ID is " + nGroup.getHighID());
        }
        catch (Exception e) {
            Assert.assertTrue(false);
        }
    }
    
    /*
    @Test
    @Ignore
    public void testVerifyHeaders() {
    	try {
            NntpClientV2 client = new NntpClientV2(nServer);
            client.connect();
            client.setGroup(nGroup);
    		
            ArrayList<NntpArticleHeader> headers = cm.getCachedHeaders( nGroup, null );
            log.info("preparing to validate " + headers.size() + 
            		" headers in group " + nGroup.getName());
            Iterator<NntpArticleHeader> iter = headers.iterator();
            while (iter.hasNext()) {
            	NntpArticleHeader header = iter.next();
            	header = cm.getCachedHeader(nGroup, header.getID());
            	Assert.assertNotNull("article header was null", header);
            	long[] ids = new long[1];
            	if (header.isMultipart()) {
            		ids = header.getParts();
            		Assert.assertNotNull("article ID list was null", ids);
            	}
            	else {
            		ids[0] = Long.parseLong(header.getID());
            	}
            	for (int x=0; x > ids.length; x++) {
            		long id = ids[x];
            		// verify
            		try {
            			client.verifyHeader(id);
            		}
            		catch (Exception error) {
            			log.warn("could not verify header " + header.getSubject());
            			break;
            		}
            	}
            }
            client.disconnect();
    	}
    	catch (Exception e) {
    		Assert.assertTrue(false);
    	}
    }
	*/
    
    @Test
    @Ignore
    public void testXoverParsing() throws Exception {
        client = new NntpClientV2(nServer, dbManager);
        BufferedReader reader = getTestHeaders();
        String line;
        int x = 0;
        while ((line = reader.readLine()) != null) {
            Assert.assertNotNull(line);
            String[] header = line.split("\t+");
            NntpArticleHeader myHeader = new NntpArticleHeader();
            client.buildArticleHeader(myHeader, header);
            
            // look for things we are interested in
            Assert.assertNotNull(myHeader.getSubject());
            Assert.assertNotNull(myHeader.getID());
            Assert.assertNotNull("MsgID was null", myHeader.getMsgID());
            String msgid = myHeader.getMsgID();
            Assert.assertTrue("MsgID was malformed", msgid.indexOf("<") == 0 );
            x++;
        }
        
        reader.close();
        log.info("read this many lines: " + x);
    }
    
    @Test
    @Ignore
    public void testHeaderMultiMap() throws Exception {
        client = new NntpClientV2(nServer, dbManager);
        Pattern pattern = Pattern
                .compile("^.*[\\(\\[](\\d+)\\/(\\d+)[\\)\\]].*");
        Matcher regex;
        HashMap multipartMap = new HashMap();
        
        BufferedReader reader = getTestHeaders();
        String line;
        int x = 0;
        int foundmultipart = 0;
        while ((line = reader.readLine()) != null) {

            // Everything below is from NntpClientV2 and deals 
            // with multipart article detection and mapping

            // dealing with XOVER output, tab delimited
            String[] header = line.split("\t+");

            if (header.length > 5) { // we've got an xover line
                // set up our initial regex matcher
                regex = pattern.matcher(header[1]);
                try {
                    NntpArticleHeader currentHeader = new NntpArticleHeader();
                    // setup some test basics
                    currentHeader.setServer(nGroup.getServer());
                    currentHeader.setPort(nGroup.getPort());
                    currentHeader.setGroup(nGroup.getName());
                    regex.reset(header[1]); // set up our regex test on subject

                    // look for multipart article
                    // based on subject naming conventions
                    if (regex.matches() && (regex.groupCount() > 1)
                        && (Integer.parseInt(regex.group(2)) > 1)) {
                        

                        // if more than 0 parts
                        if (Integer.parseInt(regex.group(2)) > 0) {
                            // mangle the header by removing the multipart info
                            // header[1] is now a key for all parts
                            header[1] = header[1].substring(0, (regex.start(1)) - 1);

                            // inner loop consts
                            int placeholder = Integer.parseInt(regex.group(1)) - 1;

                            // part of an existing multipart?
                            if (!multipartMap.containsKey(header[1])) {
                                addNewHeaderToMap(header, currentHeader, multipartMap, regex, placeholder);
                            }
                            // else we have this multipart already
                            else {
                                foundmultipart++;
                                addHeaderToMap(currentHeader, multipartMap,
                                        header, placeholder);
                            }
                        }
                    }
                    // singlepart article
                    else {
                        //currentHeader.setSource(grp.getServer(), grp.getPort(), grp.getName());
                        currentHeader = buildArticleHeader(currentHeader, header);
                        //cm.addHeader(currentHeader);
                        //grpCount++;
                        //count++;
                    }
                } 
                catch (ArrayIndexOutOfBoundsException aob) {
                    log.warn("caught exception composing NntpArticleHeader: "
                          + line + aob.toString());
                }
            }
            x++;
        }
        
        reader.close();
        log.info("I have this in my multipartmap: " + multipartMap.size());
        log.info("read this many lines: " + x);
        log.info("foundmultipart was " + foundmultipart);
        
        testHeaderSerialization(multipartMap);
        //testDBAccess(multipartMap);
    }
        
    public void testDBAccess(HashMap headers) {
        try {
            Collection myHeaders = headers.values();
            Iterator iter = myHeaders.iterator();
            while (iter.hasNext()) {
                NntpArticleHeader myHeader = (NntpArticleHeader)iter.next();
                this.validateHeader(myHeader);
                if (myHeader.isMultipart() && myHeader.isComplete()) {
                    dbManager.addHeader(myHeader);
                }
                if (!myHeader.isMultipart()) {
                    dbManager.addHeader(myHeader);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail("error:" + e.getMessage());
        }
    }
    
    
    /**
     * simple routine to test header serialization
     */
    public void testHeaderSerialization(HashMap headers) {
        Collection myHeaders = headers.values();
        Iterator iter = myHeaders.iterator();
        while (iter.hasNext()) {
            NntpArticleHeader myHeader = (NntpArticleHeader)iter.next();
            try {
                HashMap hash = DBUtils.mapHeader(myHeader);
            }
            catch (Exception e) {
                Assert.fail("header could not be mapped to a hash: " + e.getMessage());
            }
            String json = myHeader.getPartsAsJSON();
            myHeader.setPartsFromJSON(json);
        }
        
    }
    
    /**
     * Add a new Header into a Multipart Map
     * @param header
     * @param currentHeader
     * @param multipartMap
     * @param regex
     */
    private void addNewHeaderToMap(String[] header, NntpArticleHeader currentHeader, 
            HashMap multipartMap, Matcher regex, int placeholder ) {
        try {
            // order parts by regex.group(1) on header
            currentHeader.setAsMultipart(true);
            currentHeader.setTotalParts(Integer.parseInt(regex.group(2)));
            // add this header ID to the array for this multipart
            if (placeholder < 0) {
                placeholder = 0;
            }
            currentHeader.addMultiPartID(placeholder, Long.parseLong(header[0]), header[4]);
            currentHeader = buildArticleHeader(currentHeader, header);
            // add to multipart Map
            multipartMap.put(header[1], currentHeader);
        } 
        catch (ArrayIndexOutOfBoundsException obe) {
            //log.warn("caught exception composing NntpArticleHeader: "
            //      + line + obe.toString());
            multipartMap.remove(header[1]);
        }
    }
    
    private void addHeaderToMap(NntpArticleHeader currentHeader, HashMap multipartMap,
            String[] header, int placeholder) {
        
        try {
            // place id in array in proper order based
            // on posting part
            currentHeader = (NntpArticleHeader) multipartMap.get(header[1]);

            if (placeholder < 0) {
                placeholder = 0;
            }
            currentHeader.addMultiPartID(placeholder, Long.parseLong(header[0]), header[4]);

            // update bytes for this multipart header[5]
            try {
                int bytes = Integer.parseInt(currentHeader.getBytes());
                if (header[5].indexOf("<") > -1) {
                    bytes += Integer.parseInt(header[6]);
                } 
                else {
                    bytes += Integer.parseInt(header[5]);
                }
                currentHeader.setBytes(Integer.toString(bytes));
            } 
            catch (NumberFormatException nfe) {
                log.error("malformed header?");
            }

            // place into persistance store, removing from multipartMap
            if (currentHeader.isComplete()) {
                //multipartMap.remove(header[1]);
                //cm.addHeader(currentHeader);
                //grpCount++;
                //count++;
            } 
            else { // else put it back in the map
                multipartMap.put(header[1], currentHeader);
            }

        } 
        catch (ArrayIndexOutOfBoundsException obe) {
            //log.warn("caught exception composing NntpArticleHeader: "
            //      + line + obe.toString());
            multipartMap.remove(header[1]);
        }
        
    }
    
    private NntpArticleHeader buildArticleHeader(NntpArticleHeader myHeader,
            String[] header) throws ArrayIndexOutOfBoundsException {
        int x = 0;
        // normally 9 headers
        myHeader.setID(Long.valueOf(header[x++])); // 0
        myHeader.setSubject(header[x++]); // 1
        myHeader.setFrom(header[x++]); // 2
        myHeader.setDate(header[x++]); // 3
        myHeader.setMsgID(header[x++]); // 4
        
        if (header[x].indexOf("<") > -1) { // 5
            x++;
        }
        myHeader.setBytes(header[x++]); // 6
        myHeader.setLines(header[x++]); // 7

        return myHeader;
    }
    
    
}
