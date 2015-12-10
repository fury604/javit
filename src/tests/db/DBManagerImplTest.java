package tests.db;

import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import net.nexxus.db.DBManager;
import net.nexxus.db.DBManagerImpl;
import net.nexxus.nntp.NntpArticleHeader;

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
