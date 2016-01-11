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
