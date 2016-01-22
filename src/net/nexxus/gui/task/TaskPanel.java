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

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * this class is the wrapping Panel used to contain
 * all the elements used in the Task view window.
 *
 * it requires the TaskTable and the Taskbar components
 * and methods to expose these elements
 */
public class TaskPanel extends JPanel {

    private TaskInfoPanel taskInfoPanel = TaskInfoPanel.getInstance();
    private TaskTable taskTable = new TaskTable();

    public TaskPanel() {
        super();
        
        setLayout( new BorderLayout() );
        Thread t = new Thread( taskInfoPanel );
        t.start();
        
        add( taskInfoPanel, BorderLayout.NORTH );
        JScrollPane taskPane = new JScrollPane( taskTable );
        add( taskPane, BorderLayout.CENTER );
    }
    
    /**
     * expose our TaskTable
     */
    public TaskTable getTaskTable() {
        return taskTable;
    }
    
    public TaskInfoPanel getTaskInfoPanel() {
        return taskInfoPanel;
    }
}
