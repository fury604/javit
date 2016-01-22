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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

public class ArticlePanel extends JPanel {

	private BorderLayout layout = new BorderLayout();
	private static ArticleTable articleTable = new ArticleTable();;

	/**
	 * give us the window in which Articles 
	 * are displayed in.
	 */
	public ArticlePanel() {
		super();
		this.setLayout(layout);

		// the ArticleTable will contain our NntpArticles
		JScrollPane scrollPane = new JScrollPane(articleTable);
		articleTable.setBackground(Color.WHITE);

		// create a JTextField to hold keyword search
		// terms
		JPanel filterPanel = new JPanel();
		FlowLayout flowLayout = new FlowLayout();
		filterPanel.setLayout(flowLayout);
		JLabel filterLabel = new JLabel("filter");
		final JTextField filterText = new JTextField(15);

		// add KeyListener to JTextField to trigger filter on KeyReleased
		// keyword search in ArticleTable
		filterText.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {}
			public void keyPressed(KeyEvent e) {};
			public void keyReleased(KeyEvent e) {
				String filter = filterText.getText();
				((ArticleTableModel)articleTable.getModel()).filterTable(filter);
			};
		});

		filterPanel.add(filterLabel);
		filterPanel.add(filterText);

		this.add(filterPanel, BorderLayout.NORTH);
		this.add(scrollPane, BorderLayout.CENTER);
	}

	public ArticleTable getArticleTable() { 
		return articleTable; 
	}

}
