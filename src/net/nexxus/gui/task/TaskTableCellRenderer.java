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

import java.awt.Font;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import net.nexxus.task.*;

public class TaskTableCellRenderer extends DefaultTableCellRenderer {


	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
		
		Component cell = 
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
 
		// we can make determinations using:
		if ( table.getValueAt(row, column) instanceof UpdateHeadersTask ) {
			System.out.println("there be shit going on");
		}

		if( value instanceof String ) {
			String text = (String) value;
			if( text != null ) { 
				cell.setFont(new Font("Lucida Bright Regular",Font.PLAIN,11)); 
			}
			if ( column == 0 ) {
				// this is the status column
				// determine color based on status
				if ( text.equals(Task.QUEUED) ) {
					cell.setForeground(Color.GRAY); 
				}
				if ( text.equals(Task.DOWNLOADING) || text.equals(Task.UPDATING) ) {
					cell.setForeground(Color.BLACK);
				}
				if ( text.equals(Task.WAITING_TO_DECODE) ) {
					cell.setForeground(Color.GRAY);
				}
				if ( text.equals(Task.DECODING) ) {
					cell.setForeground(Color.BLUE);
				}
				if ( text.substring(0,5).equals("error") ) {
					cell.setForeground(Color.RED);
				}
			} 
		}
		return cell;
	}
}
