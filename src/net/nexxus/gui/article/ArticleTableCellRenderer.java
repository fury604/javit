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

import java.awt.Font;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import net.nexxus.nntp.NntpArticleHeader;
import net.nexxus.util.ApplicationConstants;


public class ArticleTableCellRenderer extends DefaultTableCellRenderer {

	public ArticleTableCellRenderer() {
		super();
	}

	public Component getTableCellRendererComponent(JTable table, 
			Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		
		Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		// value is NntpArticleHeader
		// cell is HeadersTableCellRender
		if (cell instanceof ArticleTableCellRenderer) {
			NntpArticleHeader header = (NntpArticleHeader)value;
			ArticleTableCellRenderer c = (ArticleTableCellRenderer)cell;
			
			if ( column == 0 ) {
				c.setText(header.getBytes());
			}
			
			if ( column == 1 ) {
				c.setText(header.getSubject());
			}
			
			if ( column == 2 ) {
				c.setText(header.getDate());
			}
			
			if ( column == 3 ) {
				c.setText(header.getFrom());
			}

			// set Font
			c.setFont(ApplicationConstants.LUCIDA_BRIGHT_REGULAR_FONT);
			c.setForeground(Color.BLACK);

			if ( header.getStatus().equals( NntpArticleHeader.STATUS_READ ) ) {
				c.setForeground(Color.LIGHT_GRAY);
			}
			
			if ( header.getStatus().equals( NntpArticleHeader.STATUS_QUEUED ) ) {
				c.setForeground(Color.ORANGE);
			}
			
			if ( header.getStatus().equals( NntpArticleHeader.STATUS_DOWNLOADING ) ) {
				c.setForeground(Color.BLUE);
			}
			
			if ( header.getStatus().equals( NntpArticleHeader.STATUS_ERROR ) ) {
				c.setForeground(Color.RED);
			}
			
			return c;
		}

		return cell;
	}
}
