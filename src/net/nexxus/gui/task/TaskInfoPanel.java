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
import java.awt.GridLayout;

import java.util.Properties;

import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//import net.nexxus.task.TaskManager;
import net.nexxus.task.RunnableTask;
//import net.nexxus.util.ComponentManager;

/**
 * this class is the Panel which contains all
 * of the informational widgets related to running tasks.
 *
 * These are the download progress meters for each 
 * downloading thread. Text labels for what each Task
 * thread is doing.
 */
public class TaskInfoPanel extends JPanel implements Runnable {

    private static TaskInfoPanel ref;
    private static Logger log = LogManager.getLogger(TaskInfoPanel.class.getName());
    private TaskProgressPanel[] progress;
    private GridLayout layout = new GridLayout(3,1);
    private Dimension size = new Dimension( 800, 200 );
    
    private TaskInfoPanel() {
        super();
        int pool_size = 3;  // legacy value
        /*
        Properties p = ComponentManager.getProperties();
        if ( p.getProperty(TaskManager.PROP_POOL_SIZE) != null  ) {
            String value = p.getProperty(TaskManager.PROP_POOL_SIZE);
            pool_size = Integer.parseInt(value);
        }
        */
        // setup progress bars for our Thread count
        progress = new TaskProgressPanel[pool_size];
        
        for ( int x=0; x < progress.length; x++ ) {
            TaskProgressPanelThread t = new TaskProgressPanelThread();
            t.start();
            progress[x] = t.getPanel();
            progress[x].setText("thread - " + x);
            add( progress[x] );
        }
        
        setLayout(new GridLayout(pool_size,1));
    }
    
    public synchronized static TaskInfoPanel getInstance() {
        if ( ref == null ) ref = new TaskInfoPanel();
        return ref;
    }

    public void registerTask ( String thread_name, RunnableTask rt ) {
        for ( int x=0; x < progress.length; x++ ) {
            if ( thread_name.equals("pool " + x) ) {
                progress[x].listenTo(rt);
                break;
            }
        }
    }
    
    public void run() {}

}
