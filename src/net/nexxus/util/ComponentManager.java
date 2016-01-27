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
package net.nexxus.util;

import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.nexxus.db.DBManager;
import net.nexxus.db.DBManagerImpl;

public class ComponentManager {
    private static Logger log = LogManager.getLogger(ComponentManager.class.getName());
            
    public DBManager getDBManager() {
        try {
            Properties p = getApplicationProperties();
            DBManager dbManager = new DBManagerImpl(p);
            return dbManager;
        }
        catch (Exception e) {
            log.error("failed creating DBManager instance: " + e.getMessage());
        }
        
        return null;
    }
    
    private Properties getApplicationProperties() throws Exception {
        Properties p = new Properties();
        InputStream conf = Thread.currentThread().getContextClassLoader().getResourceAsStream(ApplicationConstants.APP_CONFIG_FILE);
        p.load(conf);
        
        return p;
    }

}
