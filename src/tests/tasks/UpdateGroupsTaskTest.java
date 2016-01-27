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

import junit.framework.Assert;
import net.nexxus.nntp.NntpClientV2;
import net.nexxus.task.UpdateGroupsTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import tests.BaseTest;

public class UpdateGroupsTaskTest extends BaseTest {

    private static Logger log = LogManager.getLogger(UpdateGroupsTaskTest.class);
    private static int PRIORITY = 2;
    
    @Test
    //@Ignore
    public void updateGroupsTask() {
        try {
            NntpClientV2 client = new NntpClientV2(nServer, dbManager);
            //log.debug("server " + nServer.getServer());
            //log.debug("port: " + nServer.getPort());
            //Assert.assertNotNull("nServer was null: " + nServer);
            UpdateGroupsTask task = new UpdateGroupsTask(dbManager, client, nServer);
            Thread t = new Thread(task);
            t.setPriority(PRIORITY);
            t.start();
            t.join();
        }
        catch (Exception e) {
            Assert.fail("failed running task: " + e.getMessage());
        }
        
    }
}
