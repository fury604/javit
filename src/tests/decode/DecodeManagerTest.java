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
