package tests.decode;


import junit.framework.Assert;

import net.nexxus.decode.DecodeManager;
import net.nexxus.nntp.NntpArticleHeader;
import net.nexxus.task.Task;

import org.junit.Ignore;
import org.junit.Test;

import tests.BaseTest;

public class DecodeManagerTest extends BaseTest {
    
    @Test
    @Ignore
    public void testDecodeMultipart() {
        DecodeManager decoder = DecodeManager.getInstance();
        decoder.setCacheDir(cacheDir);
        decoder.setDownloadDir(downloadDir);
        decoder.add(getTask());
        
        Thread t = new Thread(decoder);
        t.start();
        try {
            Thread.currentThread().sleep(3000);
        }
        catch (Exception e) {
            Assert.fail("decoder thread died: " + e.getMessage());
        }
    }
    
    
    /*
     * setup our tasks
     */
    private Task getTask() {
        /*
         * DecodeManager makes assumptions about cache
         * header file naming. We override elements in the
         * NntpArticleHeader to point to the test files
         */
        NntpArticleHeader header = new NntpArticleHeader();
        header.setServer("test.server");
        header.setGroup("test.group");
        header.setID(1L);
        header.setSubject("test");
        header.setTotalParts(2);
        header.addMultiPartID(0, 1, "msgID");
        header.addMultiPartID(1, 2, "msgID");
        Task task = new Task(header);
        
        return task;
    }

}
