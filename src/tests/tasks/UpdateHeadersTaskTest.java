package tests.tasks;

import net.nexxus.nntp.NntpClientV2;
import net.nexxus.task.UpdateHeadersTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tests.BaseTest;

@RunWith(JUnit4.class)
public class UpdateHeadersTaskTest extends BaseTest {
    
    private static Logger log = LogManager.getLogger(UpdateHeadersTaskTest.class);
    private NntpClientV2 client = new NntpClientV2(nServer, dbManager);
    private static int PRIORITY = 2;
    
    @Test
    @Ignore
    public void testUpdateHeadersTask() {
        try {
            UpdateHeadersTask task = new UpdateHeadersTask(dbManager, client, nGroup, nServer);
            Thread t = new Thread(task);
            t.setPriority(PRIORITY);
            t.start();
            t.join();
        }
        catch (Exception e) {
            log.error("failed running task: " + e.getMessage());
        }
    }

}
