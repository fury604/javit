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
package net.nexxus.gui.dialog;

import javax.swing.*;
import javax.swing.event.EventListenerList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.awt.event.*;

import net.nexxus.event.AddServerEvent;
import net.nexxus.event.GUIEvent;
import net.nexxus.event.GUIEventListener;
import net.nexxus.gui.groups.ServerNode;
import net.nexxus.nntp.NntpServer;
import net.nexxus.util.ApplicationConstants;

public class AddServerDialog extends JDialog implements ActionListener {

	private static Logger log = LogManager.getLogger(AddServerDialog.class.getName());

    private static final String title = "Add Server"; 

    //private static JTextField serverLabel = new JTextField(10);
    private static JTextField serverAddress = new JTextField(15);
    private static JTextField serverPort = new JTextField(4);
    private static JTextField serverUsername = new JTextField(10);
    private static JTextField serverPassword = new JTextField(10);

    private static final JButton okButton = new JButton("OK");
    private static final JButton cancelButton = new JButton("Cancel");

    private static final JLabel panelTitle = new JLabel("Connection Parameters");
    //private static final JLabel label = new JLabel("Label:");
    private static final JLabel address = new JLabel("Address:");
    private static final JLabel port = new JLabel("Port:");
    private static final JLabel username = new JLabel("Username:");
    private static final JLabel password = new JLabel("Password:");

    private static JPanel panel = new JPanel();
    private static GridBagLayout layout = new GridBagLayout();
    private static GridBagConstraints constraints = new GridBagConstraints();

    private static Font labelFont = ApplicationConstants.HELVETICA_FONT;
    private static Font titleFont = ApplicationConstants.LUCIDA_BOLD_FONT;

    private static EventListenerList listenerList = new EventListenerList();

    private static final String ACTION_OK = new String("ACTION_OK");
    private static final String ACTION_CANCEL = new String("ACTION_CANCEL");

    public AddServerDialog(Frame aFrame) {
    	super(aFrame);
	
    	// set title & default size + LayoutManager
    	setTitle(title);
    	setSize(400,190);

    	panel.setLayout(layout);
    	constraints.fill = GridBagConstraints.BOTH;
    	constraints.weightx = 1.0;
    	constraints.gridwidth = GridBagConstraints.REMAINDER;
    	constraints.anchor = GridBagConstraints.WEST;

    	// Title
    	layout.setConstraints(panelTitle, constraints);
    	panelTitle.setFont(titleFont);
    	panel.add(panelTitle);

    	// Address
    	constraints.weightx = 0.0;
    	constraints.gridwidth = GridBagConstraints.RELATIVE;
    	layout.setConstraints(address, constraints);
    	address.setFont(labelFont);
    	panel.add(address);
    	constraints.gridwidth = GridBagConstraints.REMAINDER;
    	layout.setConstraints(serverAddress,constraints); 
    	panel.add(serverAddress);

    	// Port
    	constraints.weightx = 0.0;
    	constraints.gridwidth = GridBagConstraints.RELATIVE;
    	layout.setConstraints(port, constraints);
    	port.setFont(labelFont);
    	panel.add(port);
    	constraints.gridwidth = GridBagConstraints.REMAINDER;
    	layout.setConstraints(serverPort,constraints); 
    	panel.add(serverPort);

    	// Username
    	constraints.weightx = 0.0;
    	constraints.gridwidth = GridBagConstraints.RELATIVE;
    	layout.setConstraints(username, constraints);
    	username.setFont(labelFont);
    	panel.add(username);
    	constraints.gridwidth = GridBagConstraints.REMAINDER;
    	layout.setConstraints(serverUsername,constraints); 
    	panel.add(serverUsername);

    	// Password
    	constraints.weightx = 0.0;
    	constraints.gridwidth = GridBagConstraints.RELATIVE;
    	layout.setConstraints(password, constraints);
    	password.setFont(labelFont);
    	panel.add(password);
    	constraints.gridwidth = GridBagConstraints.REMAINDER;
    	layout.setConstraints(serverPassword,constraints); 
    	panel.add(serverPassword);

    	// ok button
    	constraints.weightx = 1.0;
    	constraints.gridwidth = GridBagConstraints.RELATIVE;
    	constraints.anchor = GridBagConstraints.CENTER;
    	layout.setConstraints(okButton,constraints); 
    	okButton.setActionCommand(ACTION_OK);
    	okButton.addActionListener(this);
    	okButton.setFont(labelFont);
    	panel.add(okButton);
	
    	// cancel button
    	constraints.weightx = 1.0;
    	constraints.gridwidth = GridBagConstraints.RELATIVE;
    	layout.setConstraints(cancelButton,constraints); 
    	cancelButton.setActionCommand(ACTION_CANCEL);
    	cancelButton.addActionListener(this);
    	cancelButton.setFont(labelFont);
    	panel.add(cancelButton);

    	// we're all setup
    	setContentPane(panel);
    }

    /** 
     * Watch for ACTION_OK or ACTION_CANCEL
     */
    public void actionPerformed(ActionEvent e) {
    	if ( e.getActionCommand().equals(ACTION_OK) ) {
    		// create a ServerNode
    		if ( validateEntry() ) {
    			if ( serverUsername.getText() != null && 
    				 serverPassword.getText() != null ) {
    				NntpServer snode = 
    					new NntpServer(serverAddress.getText(), Integer.parseInt(serverPort.getText()),
										serverUsername.getText(), serverPassword.getText());
    				ServerNode node = new ServerNode(snode);
    				AddServerEvent event = new AddServerEvent(node);
    				fireEvent(event);
    				clearAndHide();
    			} else {
    				NntpServer snode = 
    					new NntpServer(serverAddress.getText(), Integer.parseInt(serverPort.getText()));
    				ServerNode node = new ServerNode(snode);
    				AddServerEvent event = new AddServerEvent(node);
    				fireEvent(event);
    				clearAndHide();
    			}
    		}
    	}
    	if( e.getActionCommand().equals(ACTION_CANCEL) ) { 
    		clearAndHide(); 
    	}
    }

    /** This method clears the dialog and hides it. */
    public void clearAndHide() {
    	serverAddress.setText(new String());
    	serverPort.setText(new String());
    	serverUsername.setText(new String());
    	serverPassword.setText(new String());
        setVisible(false);
    }

    private boolean validateEntry() {
    	boolean valid = false;
    	if ( 
			 (serverAddress.getText().length() != 0) && 
			 (serverPort.getText().length() != 0) 
	       ) {
    		valid = true;
    	}
    	log.debug("text size " + serverAddress.getText().length());
    	return valid;
    }

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
