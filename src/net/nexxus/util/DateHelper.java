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

import java.util.Date;
import java.text.SimpleDateFormat;

public class DateHelper {

  public static SimpleDateFormat format_A = new SimpleDateFormat("dd MMM yyyy kk:mm:ss zzz");
  public static SimpleDateFormat format_B = new SimpleDateFormat("EEE, dd MMM yyy kk:mm:ss zzz");

  /**
   * convenience methods to parse a Date
   */
  public static Date parse( String date ) throws Exception {
    try {
      return format_A.parse( date );
    }
    catch ( Exception e ) {
      return format_B.parse( date );
    }
  }
  
}
