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
package net.nexxus.gui.article;

import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.*;

import net.nexxus.nntp.*;
import net.nexxus.task.*;
import net.nexxus.event.*;

public class ArticleTableModel extends AbstractTableModel {
    
	private static Logger log = LogManager.getLogger(ArticleTableModel.class.getName());
    
    public static boolean SQCH_ON = false;
    public static int SQCH_VAL = 0; // must be arg in negative ( ie. -12 )
    public static ArrayList data = new ArrayList();
    
    private static java.text.SimpleDateFormat fm1 =
            new java.text.SimpleDateFormat("dd MMM yyyy kk:mm:ss zzz");
    private static java.text.SimpleDateFormat fm2 =
            new java.text.SimpleDateFormat("EEE, dd MMM yyy kk:mm:ss zzz");
    private boolean DEBUG = true;
    private static String modelGroup = null;
    private static ArrayList filtered = new ArrayList();
    private Matcher regex;
    private static Pattern pattern = Pattern.compile("^.*");
    private static String filterString = new String();
    
    // give us the column headers
    final String[] columnNames = {
        NntpClient.XOVER_HDRS[5],
        NntpClient.XOVER_HDRS[1],
        NntpClient.XOVER_HDRS[3],
        NntpClient.XOVER_HDRS[2],
        
    };
    
    // default c'tor
    public ArticleTableModel() {
        super();
        // add event listeners for headers update/download
        /*
        TaskManager.getInstance().addGUIEventListener(new GUIEventListener() {
            public void eventOccurred(GUIEvent e) {
                if ( e instanceof ArticleDownloadedEvent ) { // article downloaded
                    DownloadArticleTask t = (DownloadArticleTask)e.getSource();
                    NntpArticleHeader header = (NntpArticleHeader)t.getSource();
                    if ( data.size() > 0 ) {
                        NntpArticleHeader h = (NntpArticleHeader)data.get(0);
                        // server and group match
                        if (header.getServer().equals(h.getServer()) && header.getGroup().equals(h.getGroup())) {
                            if ( data.contains(header) ) {
                                int x = data.indexOf(header);
                                ((NntpArticleHeader)data.get(x)).setStatus(NntpArticleHeader.STATUS_READ);
                            }
                        }
                    }
                }
            }
        });
        */
    }
    
    // return num columns
    public int getColumnCount() { 
    	return columnNames.length; 
    }
    
    // return num rows
    public int getRowCount() { 
    	return filtered.size(); 
    }
    
    // return the Object from a Row
    public Object getRow(int row) { 
    	return filtered.get(row); 
    }
    
    // return column name
    public String getColumnName(int col) { 
    	return columnNames[col]; 
    }
    
    // return co-ordinate value
    public Object getValueAt(int row, int col) {
        NntpArticleHeader header = (NntpArticleHeader)filtered.get(row);
        return header;
    }
    
    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }
    
    /**
     * Don't need to implement this method unless your table's
     * data can change.
     */
    public void setValueAt(Object value, int row, int col) {
        fireTableCellUpdated(row, col);
    }
    
    /**
     * fill(ArrayList fill)
     *
     * fill our Model with data
     */
    public void fill(ArrayList fill) {
        data.clear();
        data.addAll(fill);
        filter();
        fireTableChanged(new TableModelEvent(this));
    }
    
    public void clearModel() { 
    	filtered.clear(); 
    	data.clear(); 
    }
    private void changed() {
        fireTableChanged(new TableModelEvent(this));
    }
    
    /**
     * filterTable(String filter)
     *
     * set the current filter string to filter and refilter
     */
    public void filterTable(String filter) {
        filterString = filter;
        filter();
    }
    
    /**
     * filter()
     *
     * filter data contents using the current regex filter
     */
    private void filter() {
        log.debug("in filtered");
        pattern = Pattern.compile("^.*" + filterString + ".*", Pattern.CASE_INSENSITIVE);
        filtered.clear();
        
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, SQCH_VAL);
        java.util.Date cutoff_point = cal.getTime();
        
        for (int x=0; x < data.size(); x++) {
            NntpArticleHeader header = (NntpArticleHeader)data.get(x);
            
            if ( modelGroup == null ) {
                modelGroup = header.getGroup();
            }
            
            // do regex filter on Subject
            // ensure we have a pattern in the first place
            if ( filterString.length() > 0 ) {
                regex = pattern.matcher(header.getSubject());
                regex.reset(header.getSubject());
                if ( regex.matches() ) {
                	filtered.add(header);
                }
            } 
            else {
                filtered.add(header);
            }
        }
        Collections.sort(filtered);
        fireTableChanged(new TableModelEvent(this));
    }
    
}
