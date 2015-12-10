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
