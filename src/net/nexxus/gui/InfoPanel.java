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
package net.nexxus.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.GridLayout;
import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.JLabel;

import net.nexxus.util.ApplicationConstants;

public class InfoPanel extends JPanel {

	//public static CacheProgressBar bar = new CacheProgressBar();

	private String meterLabel = "Header Meter";
	private String articleMode = "Article Mode:";
	private JLabel articleModeLabel = new JLabel("3 Day");

	/**
	 * constructor set up everything at this point
	 */
	public InfoPanel() {
		super(new GridLayout(2,2));

		// assign a JLabel 
		JLabel headerMeter = new JLabel(meterLabel);
		headerMeter.setFont(ApplicationConstants.BITSTREAM_CHARTER_FONT);
		add(headerMeter);

		// now a ProgressBar
		//CacheProgressBar bar = new CacheProgressBar();
		//add(bar);

		JLabel modeLabel = new JLabel(articleMode);
		modeLabel.setFont(ApplicationConstants.BITSTREAM_CHARTER_FONT);
		add(modeLabel);

		articleModeLabel.setFont(ApplicationConstants.BITSTREAM_CHARTER_FONT);
		add(articleModeLabel);
		ApplicationConstants.CUTOFF = 3;
	}

	/**
	 * public expose our JLabel containing ARTICLE_MODE
	 */
	public JLabel getArticleModeLabel() { 
		return articleModeLabel; 
	}

	/**
	 * provide an interface to our article_mode label
	 *
	 */
	public void listenTo( Component table ) {

		table.addKeyListener( new KeyListener() {

			public void keyTyped(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
			};

			public void keyReleased(KeyEvent e) {
				if ( e.getKeyCode() == KeyEvent.VK_1 ) { // 1 = 1 day old filter
				    articleModeLabel.setText("One Day");
				    ApplicationConstants.CUTOFF = 1;
				}
				else if ( e.getKeyCode() == KeyEvent.VK_2 ) { 
				    articleModeLabel.setText("Three Day");
				    ApplicationConstants.CUTOFF = 3;
				}
				else if ( e.getKeyCode() == KeyEvent.VK_3 ) { 
				    articleModeLabel.setText("Seven Day");
				    ApplicationConstants.CUTOFF = 7;
				}
				else if ( e.getKeyCode() == KeyEvent.VK_4 ) { 
				    articleModeLabel.setText("One Month");
				    ApplicationConstants.CUTOFF = 30;
				}
				else if ( e.getKeyCode() == KeyEvent.VK_5 ) { 
				    articleModeLabel.setText("One Year");
				    ApplicationConstants.CUTOFF = 365;
				}
				else if ( e.getKeyCode() == KeyEvent.VK_6 ) { 
				    articleModeLabel.setText("Two Years");
				    ApplicationConstants.CUTOFF = 730;
				}
				else if ( e.getKeyCode() == KeyEvent.VK_7 ) { 
				    articleModeLabel.setText("Three Years");
				    ApplicationConstants.CUTOFF = 1095;
				}
				else if ( e.getKeyCode() == KeyEvent.VK_8 ) { 
				    articleModeLabel.setText("Four Years");
				    ApplicationConstants.CUTOFF = 1460;
				}
				else if ( e.getKeyCode() == KeyEvent.VK_9 ) { 
				    articleModeLabel.setText("Five Years");
				    ApplicationConstants.CUTOFF = 1825;
				}
				else if ( e.getKeyCode() == KeyEvent.VK_0 ) { // 0 = turn filter off
				    articleModeLabel.setText("disabled");
				    ApplicationConstants.CUTOFF = 0;
				}
			};
		});
	}

}