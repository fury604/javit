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
package net.nexxus.gui.groups;

import javax.swing.table.*;
import javax.swing.event.TableModelEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import net.nexxus.nntp.NntpGroup;

public class GroupsListTableModel extends AbstractTableModel {

    final String[] columnNames = { "group name", "article count" };
    private static ArrayList data = new ArrayList();
    //private boolean DEBUG = true;
    private CountAsc cntAsc = new CountAsc();
    private CountDesc cntDesc = new CountDesc();
    private GroupAsc grpAsc = new GroupAsc();
    private GroupDesc grpDesc = new GroupDesc();
    private boolean lastCount = true;
    private boolean lastSort = true;
	
    // default c'tor
    public GroupsListTableModel() { 
    	super();
    }

    public int getColumnCount() { 
    	return columnNames.length; 
    }
    public int getRowCount() { 
    	return data.size(); 
    }
    public Object getRow(int row) { 
    	return data.get(row); 
    }
    public String getColumnName(int col) { 
    	return columnNames[col]; 
    }
    // return co-ordinate value
    public Object getValueAt(int row, int col) {
    	NntpGroup group = (NntpGroup)data.get(row);
    	if ( col == 0 ) {
    		return group.getName();
    	}
    	if ( col == 1 ) {
    		return String.valueOf(group.getCount());
    	}
    	else {
    		return group.getName();
    	}
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

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
    	if (col < 2) {
    		return false;
    	}
    	else {
    		return true;
    	}
    }

    /**
     * fill(ArrayList fill)
     *
     * fill our Model with data
     */
    public void fill(ArrayList fill) {
    	clearModel();
    	for (int i=0; i <= (fill.size()-1); i++) {
    		NntpGroup group = (NntpGroup)fill.get(i);
    		data.add(group);
    	}
    	Collections.sort(data);
    	fireTableChanged(new TableModelEvent(this));
    }

    //// clear the model of data
    public void clearModel() { 
    	data.clear(); 
    }

    // sort the datamodel
    public void sortByCount() {
    	if (lastCount) {
    		Collections.sort(data, cntAsc);
    		lastCount = false;
    	} 
    	else {
    		Collections.sort(data, cntDesc);
    		lastCount = true;
    	}
    }

    public void sortByGroup() {
    	if (lastSort) {
    		Collections.sort(data, grpAsc);
    		lastSort = false;
    	} 
    	else {
    		Collections.sort(data, grpDesc);
    		lastSort = true;
    	}
    }
    
    //////////////////////////////////////////////
    // Comparator classes to sort the dataModel
    //////////////////////////////////////////////

    private class CountAsc implements Comparator {
    	public CountAsc() {}
    	public int compare(Object a, Object b) { 
    		long cntA = ((NntpGroup)a).getCount();
    		long cntB = ((NntpGroup)b).getCount();
    		if ( cntA < cntB ) return -1;
    		if ( cntA == cntB ) return 0;
    		return 1; 
    	}
    	public boolean equals(Object a) { return false; }
    }

    private class CountDesc implements Comparator {
    	public CountDesc() {}
    	public int compare(Object a, Object b) { 
    		long cntA = ((NntpGroup)a).getCount();
    		long cntB = ((NntpGroup)b).getCount();
    		if ( cntA < cntB ) return 1;
    		if ( cntA == cntB ) return 0;
    		return -1; 
    	}
    	public boolean equals(Object a) { return false; }
    }


    private class GroupAsc implements Comparator {
    	public GroupAsc() {}
    	public int compare(Object a, Object b) { 
    		String nameA = ((NntpGroup)a).getName();
    		String nameB = ((NntpGroup)b).getName();
    		return nameA.compareTo(nameB); 
    	}
    	public boolean equals(Object a) { return false; }
    }


    private class GroupDesc implements Comparator {
    	public GroupDesc() {}
    	public int compare(Object a, Object b) { 
    		String nameA = ((NntpGroup)a).getName();
    		String nameB = ((NntpGroup)b).getName();
    		int result =  nameA.compareTo(nameB); 
    		if ( result == 0 ) return 0;
    		if ( result < 0 ) return 1;
    		return -1;
    	}
    	public boolean equals(Object a) { return false; }
    }
}
