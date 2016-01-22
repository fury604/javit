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
package net.nexxus.gui.task;

import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.EventListenerList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import net.nexxus.event.GUIEvent;
import net.nexxus.event.GUIEventListener;
import net.nexxus.nntp.NntpArticleHeader;
import net.nexxus.nntp.NntpClient;
import net.nexxus.task.Task;

public class TaskTableModel extends AbstractTableModel {

	private static final List data = Collections.synchronizedList(new ArrayList());
	// give us the column headers
	final String[] columnNames = { "status","server","group","subject" };
	private static EventListenerList listenerList = new EventListenerList();
	private static Logger log = LogManager.getLogger(TaskTableModel.class.getName());

	// default c'tor
	public TaskTableModel() { 
		super(); 
	}

	// return num columns
	public int getColumnCount() { 
		return columnNames.length; 
	}

	// return num rows
	public int getRowCount() { 
		return data.size(); 
	}

	// return the Object from a Row
	public Task getRow(int row) { 
		return (Task)data.get(row); 
	}

	public void setRow(int row, Task t) {
		data.set(row, t);
		fireTableChanged(new TableModelEvent(this));
	}

	// return column name
	public String getColumnName(int col) { 
		return columnNames[col]; 
	}

	// return co-ordinate value
	public Object getValueAt(int row, int col) {
		if ( row >= data.size() ) {
			return "stuff";
		}
		Task t = (Task)data.get(row);
		if ( col == 0 ) {
			return t.getStatus();
		}
		if ( col == 1 ) {
			return t.getServer();
		}
		if ( col == 2 ) {
			return t.getGroup();
		}
		if ( col == 3 ) {
			return t.getSubject();
		}
		else {
			return "stuff";
		}
	}

	/**
	 * JTable uses this method to determine the default renderer/
	 * editor for each cell.  
	 */
	public Class getColumnClass(int c) { 
		return getValueAt(0, c).getClass(); 
	}

	/**
	 * fill(ArrayList fill)
	 *
	 * fill our Model with data
	 */
	public void fill(Collection fill) {
		// first clean out exisiting data
		data.clear();
		data.addAll(fill);
		try { 	
			Collections.sort(data); 
		}
		catch (Exception e) {
			log.error("could not sort Collection : " + e.toString());
		}
		fireTableChanged(new TableModelEvent(this));
	}


	/////////////////////////
	// synchronized methods
	/////////////////////////
	public synchronized void addTask(Task t) {
		data.add(t);
		fireTableChanged(new TableModelEvent(this));
	}

	public synchronized int getTaskIndex(Task t) {
		if ( data.contains(t) ) { return data.indexOf(t); }
		return -1;
	}

	public synchronized void updateTask(int idx, String status) {
		java.util.ListIterator it = data.listIterator();
		while ( it.hasNext() ) {
			Task current = (Task)it.next();
			if ( current.getTaskID() == idx ) {
				current.setStatus(status);
				it.set(current);
			}
		}
		fireTableChanged(new TableModelEvent(this));
	}

	public synchronized void updateDecodingHeader(NntpArticleHeader header) {
		java.util.ListIterator it = data.listIterator();
		while ( it.hasNext() ) {
			Task current = (Task)it.next();
			if ( current.getServer().equals(header.getServer()) &&
					current.getGroup().equals(header.getGroup()) && 
					current.getSubject().equals(header.getSubject()) ) {
				if (current.getStatus().equals(Task.DECODING)) it.remove();
				else {
					current.setStatus(Task.DECODING);
					it.set(current);
				}
			}
		}
		fireTableChanged(new TableModelEvent(this));
	}

	public synchronized void removeTask(int idx) { 
		java.util.ListIterator it = data.listIterator();
		while ( it.hasNext() ) {
			Task current = (Task)it.next();
			if ( current.getTaskID() == idx ) {
				it.remove();
			}
		}
		fireTableChanged(new TableModelEvent(this));
	}

	public synchronized void removeDecodedHeader(NntpArticleHeader header) {
		java.util.ListIterator it = data.listIterator();
		while ( it.hasNext() ) {
			Task current = (Task)it.next();
			if ( current.getServer().equals(header.getServer()) &&
					current.getGroup().equals(header.getGroup()) && 
					current.getSubject().equals(header.getSubject()) ) {
				it.remove();
			}
		}
		fireTableChanged(new TableModelEvent(this));
	}


	public synchronized void clearErrors() {
		java.util.ListIterator it = data.listIterator();
		while ( it.hasNext() ) {
			Task current = (Task)it.next();
			if ( current.getStatus().equals(Task.ERROR) ) {
				it.remove();
			}
		}
		fireTableChanged(new TableModelEvent(this));
	}

	public void clearModel() { 
		data.clear(); 
	}

	/////////////////////////////
	// provide Listener methods
	// for event system
	/////////////////////////////

	// add Listener to GUIevents
	public void addGUIEventListener(GUIEventListener listener) {
		listenerList.add(GUIEventListener.class, listener);
	}

	// remove Listener to GUIevents
	public void removeGUIEventListener(GUIEventListener listener) {
		listenerList.remove(GUIEventListener.class, listener);
	}

	// tell all the Listeners about the Event
	protected void fireEvent(GUIEvent event) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==GUIEventListener.class) {
				((GUIEventListener)listeners[i+1]).eventOccurred(event);
			}
		}
	}
}
