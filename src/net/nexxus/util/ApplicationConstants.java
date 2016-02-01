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

import java.awt.Font;

public class ApplicationConstants {
    
    public static int CUTOFF = 0;
    public static Font LUCIDA_FONT = new Font("Lucida",Font.PLAIN,11);
    public static Font LUCIDA_BOLD_FONT = new Font("Lucida",Font.BOLD,11);;
    public static Font LUCIDA_BRIGHT_REGULAR_FONT = new Font("Lucida Bright Regular",Font.PLAIN,11);
    public static Font BITSTREAM_CHARTER_FONT = new Font("Bitstream Charter",Font.PLAIN,10);
    public static Font BITSTREAM_CHARTER_12_FONT = new Font("Bitstream Charter",Font.PLAIN,12);
    public static Font HELVETICA_FONT = new Font("Helvetica",Font.PLAIN,12);
    
    public static String RESOURCE_PATH = "net/nexxus/gui/resources";
    public static String DOWNLOAD_DIR = "downloads";
    public static String CACHE_DIR = "cache";
    public static String HEADERS_DIR = "headers";
    
    public static String APP_CONFIG_FILE = "javit.conf";
    
    public static int POOL_SIZE = 6;

}
