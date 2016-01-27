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

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.nexxus.event.ArticleDownloadErrorEvent;
import net.nexxus.event.GUIEventListener;
import net.nexxus.event.GUIEvent;
import net.nexxus.event.ArticlePartDownloadedEvent;
import net.nexxus.event.HeaderDownloadedEvent;
import net.nexxus.event.HeadersUpdateErrorEvent;
import net.nexxus.event.HeadersUpdatedEvent;
import net.nexxus.nntp.NntpArticleHeader;
import net.nexxus.nntp.NntpGroup;
import net.nexxus.task.DownloadArticleTask;
import net.nexxus.task.RunnableTask;
//import net.nexxus.task.DownloadArticleTask;
import net.nexxus.task.UpdateHeadersTask;

/**
 * this class servers as a container for a
 * TaskProgressBar and a JLabel
 *
 * Together they make a single TaskProgressPanel
 * that will be our progress meter
 */
public class TaskProgressPanel extends JPanel {

    private JProgressBar progressBar = new JProgressBar();
    private Dimension size = new Dimension(100, 10);
    
    private JLabel label = new JLabel();
    private String prefix = new String();

    private GridBagLayout layout = new GridBagLayout();
    private GridBagConstraints con = new GridBagConstraints();
    
    private static Logger log = LogManager.getLogger(TaskProgressPanel.class.getName());

    public TaskProgressPanel() {
        super();
        setLayout( layout );

        // set up progress bar
        progressBar.setPreferredSize( size );
        progressBar.setIndeterminate(false);

        con.gridwidth = GridBagConstraints.RELATIVE;
        con.fill = GridBagConstraints.NONE;
        con.weightx = 0.0;
        layout.setConstraints( progressBar, con );
        add( progressBar );
        
        con.gridwidth = GridBagConstraints.REMAINDER;
        con.fill = GridBagConstraints.HORIZONTAL;
        con.weightx = 1.0;
        con.insets = new Insets( 0,5,0,0 );
        layout.setConstraints( label, con );
        label.setFont( new Font("Lucida Bright Regular",Font.PLAIN,11) );
        add( label );
    }

    /**
     * listen to the given Runnable Task
     */
    public void listenTo( RunnableTask rt ) {
        rt.addGUIEventListener(new GUIEventListener() {

            public void eventOccurred(GUIEvent e) {
                if (e instanceof ArticlePartDownloadedEvent) {
                    ArticlePartDownloadedEvent ev = (ArticlePartDownloadedEvent)e;
                    if ( ev.getPartNumber() == ev.getTotalParts() ) {
                        progressBar.setValue( ev.getTotalParts() );
                        label.setText( prefix );
                        progressBar.setValue( 0 );
                    }
                    else {
                        progressBar.setMaximum( ev.getTotalParts() );
                        progressBar.setValue( ev.getPartNumber() );
                    }
                }
                
                if (e instanceof ArticleDownloadErrorEvent) {
                    label.setText( prefix );
                    progressBar.setValue( 0 );
                }
                
                if (e instanceof HeadersUpdatedEvent) {
                    label.setText( prefix );
                    progressBar.setIndeterminate(false);
                    progressBar.setValue( 0 );
                }
                
                if (e instanceof HeadersUpdateErrorEvent) {
                    progressBar.setIndeterminate(false);
                    progressBar.setValue( 0 );
                }

                if (e instanceof HeaderDownloadedEvent) {
                    HeaderDownloadedEvent ev = (HeaderDownloadedEvent)e;
                    if ( ev.getNumber() == ev.getTotal() ) {
                        label.setText( prefix );
                        progressBar.setValue( 0 );
                    }
                    else {
                        progressBar.setValue( ev.getNumber() );
                    }
                }
            }
        });
        
        // now set up our label
        if ( rt instanceof DownloadArticleTask ) {
            NntpArticleHeader header = (NntpArticleHeader)rt.getSource();
            label.setText( prefix + " : " + header.getSubject() );
        }
        
        if ( rt instanceof UpdateHeadersTask ) {
            NntpGroup group = (NntpGroup)rt.getSource();
            label.setText( prefix + " : updating headers for " + group.getName() );
            this.progressBar.setIndeterminate(true);
        }
    }

    // expose our JLabel
    public void setText( String text ) {
        prefix = text;
        label.setText( prefix );
    }
    
}
