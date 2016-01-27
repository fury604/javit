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

/**
 * TaskManager.java
 *
 * This is a Manager for all Tasks in the application.
 * It determines the number of available Threads from the
 * ThreadPool and assigns RunnableTarget Tasks to a Thread.
 *
 * The TaskManager also runs the DecodeManager. The DecodeManager
 * is run in a separate thread. It only ever decodes things
 * one at a time in a single thread.
 *
 * Tasks are queued until Threads become available from
 * the Pool and allows for an atomic level of manipulation
 * of queued Tasks and running Threads.
 *
 */
package net.nexxus.task;

import java.util.*;

import javax.swing.event.EventListenerList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.nexxus.event.*;
import net.nexxus.util.*;
import net.nexxus.nntp.*;
import net.nexxus.decode.DecodeManager;

public class TaskManager implements Runnable {
    
    public static final String PROP_POOL_SIZE = "pool_size";
    
    public static boolean isRunning = false;
    public static Vector queue = new Vector();    // used for incoming Tasks

    private static TaskManager ref;
    
    private int POOL_SIZE = 3;        
    
    private int DECODE_PRIORITY = 2;  // should allow properties to define
    private static int nextID = 0;
    public static Hashtable tasklist = new Hashtable();  // used for currently running tasks

    private static EventListenerList listenerList = new EventListenerList();
    private ThreadPool pool = new ThreadPool("pool", POOL_SIZE);  // pool size from props and make setable
    private static DecodeManager decodeManager = DecodeManager.getInstance();
    private Map threadMap = Collections.synchronizedMap(new HashMap());  // for mapping all threads to tasks
    private String cacheDir = null;
    private String downloadDir = null;
    
    private static Logger log = LogManager.getLogger(TaskManager.class.getName());
    
    // c'tor - ensure singleton semantics
    private TaskManager() {
        
        // check for properties definitions
        /*
        Properties p = ComponentManager.getProperties();
        if ( p.getProperty(PROP_POOL_SIZE) != null  ) {
            String value = p.getProperty(PROP_POOL_SIZE);
            int tmp = Integer.parseInt(value);
            if ( tmp < 11 ) { // be safe
                POOL_SIZE = tmp;
                // redo ThreadPool
                pool = new ThreadPool("pool", POOL_SIZE);
            }
        }
        if ( p.getProperty("decoder_thread_priority") != null  ) {
            String value = p.getProperty("decoder_thread_priority");
            int tmp = Integer.parseInt(value);
            if ( tmp < 11 ) { // be safe
                DECODE_PRIORITY = tmp;
            }
        }
        // configure decodeManager as well as ourself
        if (p.getProperty("cache_dir") != null) {
        	this.cacheDir = p.getProperty("cache_dir");
        	this.decodeManager.setCacheDir(this.cacheDir);
        	
        }
        if (p.getProperty("download_dir") != null) {
        	this.downloadDir = p.getProperty("download_dir");
        	this.decodeManager.setDownloadDir(this.downloadDir);
        }
        */
	
        // add a Listener to our ThreadPool
        pool.addThreadPoolListener( new ThreadPoolListener() {
            public void threadStarted(ThreadPoolEvent e) {
            }
            public void threadFinished(ThreadPoolEvent e) {
                Thread t = e.getThread();
                if ( threadMap.containsKey(t) ) {
                    log.debug("threadMap contains key " + t.getName() 
                            + " and pool size of " + pool.getPooledCount());
                    log.debug("have " + pool.getAvailableCount() 
                            + " threads waiting for stuff to do");
                }
                else {
                    log.debug("thread not found in threadMAP");
                }
            }
            public void threadExiting(ThreadPoolEvent e) {
            }
        });
        
        // start up a separate ThreadGroup
        // with a single thread for our decoder
        ThreadGroup decoderThreadGroup = new ThreadGroup("decoderThreadGroup");
        decoderThreadGroup.setMaxPriority(DECODE_PRIORITY);
        Thread decodeThread = new Thread(decoderThreadGroup, decodeManager, "decodeManager");
        decodeThread.start();
    }
    
    /**
     * provide instance method
     */
    public synchronized static TaskManager getInstance() {
        if ( ref == null ) {
            ref = new TaskManager();
        }
        return ref;
    }
    
    //////////////////////////////
    //// queuing methods
    //////////////////////////////

    public synchronized void add(RunnableTask task) {
    	
        // DownloadArticleTask
        if (task instanceof DownloadArticleTask) {
            task.setTaskID(++nextID);
            queue.add(task);
            Task t = new Task((NntpArticleHeader)task.getSource()); // source is NNtpArticle
            t.setTaskID(nextID);
            t.setStatus(Task.QUEUED);
            //log.debug("task added");
            tasklist.put(new Integer(t.getTaskID()), t);
            UpdateHeadersEvent event = new UpdateHeadersEvent(t);
            fireEvent(event);
        } 
        // UpdateHeadersTask
        else if (task instanceof UpdateHeadersTask) {
            task.setTaskID(++nextID);
            queue.add(0, task);   // always at at the head of the queue
            Task t = new Task((NntpGroup)task.getSource());
            t.setTaskID(nextID);
            t.setStatus(Task.QUEUED);
            tasklist.put(new Integer(t.getTaskID()), t);
            UpdateHeadersEvent event = new UpdateHeadersEvent(t);
            fireEvent(event);
        }
        // UpdateGroupsTask
        else if (task instanceof UpdateGroupsTask){
            task.setTaskID(++nextID);
            queue.add(task);
            Task t = new Task((NntpServer)task.getSource());
            t.setTaskID(nextID);
            tasklist.put(new Integer(t.getTaskID()),t);
            UpdateHeadersEvent event = new UpdateHeadersEvent(t);
            fireEvent(event);
        } 
        else {
            log.debug("managed to lose a Task in the queue");
        }
    }
    
    /**
     * cancel Task
     */
    public synchronized void cancel(Task t) {
        // first do queued Tasks
        ListIterator<RunnableTask> it = queue.listIterator();
        while (it.hasNext()) {
            RunnableTask rt = it.next();
            if ( rt.getTaskID() == t.getTaskID() ) {
                it.remove();
                tasklist.remove(new Integer(t.getTaskID()));
                CancelArticleDownloadEvent event = new CancelArticleDownloadEvent(t);
                fireEvent(event);
            }
        }
        // now do running Tasks
        Iterator<RunnableTask> itr = threadMap.values().iterator();
        while (itr.hasNext()) {
            RunnableTask rt = itr.next();
            if ( rt.getTaskID() == t.getTaskID() ) { 
                rt.cancel(); 
            }
        }
    }
    
    public synchronized void cancel(ArrayList tasks) {
        if (tasks.size() < 1) { 
            return; 
        }
        Task myTask = (Task)tasks.get(0);
        for (int x=0; x < tasks.size(); x++) {
            Task t = (Task)tasks.get(x);
            cancel(t);
        }
    }
    
  /*
   * getTaskList()
   * no longer synchronized
   */
    public synchronized Collection getTaskList() {
        return tasklist.values();
    }
    
    /**
     * update a given task
     */
    public synchronized void updateTask(Task t) {
        tasklist.remove(new Integer(t.getTaskID()));
        tasklist.put(new Integer(t.getTaskID()), t);
        UpdateHeadersEvent event = new UpdateHeadersEvent(t);
        fireEvent(event);
    }
    
    // could be dangerous
    public synchronized void removeTask(Task t) {
        tasklist.remove(new Integer(t.getTaskID()));
        UpdateHeadersEvent event = new UpdateHeadersEvent(t);
        fireEvent(event);
    }
    
    public synchronized void cancelTask(Task t) {
        Task tk = (Task)tasklist.get(new Integer(t.getTaskID()));
    }
    
    /**
     * clearErrors() 
     *
     */
    public synchronized void clearErrors() {
        Vector tasks = new Vector(tasklist.values());
        UpdateHeadersEvent event = null;
        int x = 0;
        while (x < tasks.size()) {
            Task t = (Task)tasks.get(x);
            if (t.getStatus().substring(0,5).equals(Task.ERROR)) {
                try {
                    tasklist.remove(new Integer(t.getTaskID()));
                } 
                catch (ConcurrentModificationException cme) {
                }
                event = new UpdateHeadersEvent(t);
            }
            x++;
        }
        if ( event != null ) {
            fireEvent(event);
        }
    }
    
    ///////////////////////////////
    //// GUIEventListener methods
    ///////////////////////////////
    
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
    
    //////////////
    //// main
    //////////////
    
    /**
     * implement Runnable interface
     */
    public void run() {
        isRunning = true;
        while(true) {
            if ( queue.size() > 0 ) {
                // thread is available, assign Runnable
                try {
                    final RunnableTask rt = (RunnableTask)queue.remove(0);
                    
                    // add GUIEventListener here
                    // so the RunnableTask can receive events and 
                    // update the other GUI components
                    rt.addGUIEventListener(new GUIEventListener() {
                        
                        public void eventOccurred(final GUIEvent event) {
                            
                            if (event instanceof ArticleDownloadedEvent) {
                                DownloadArticleTask dat = (DownloadArticleTask)event.getSource();
                                NntpArticleHeader header = (NntpArticleHeader)dat.getSource();
                                Task task = new Task(header);
                                task.setTaskID(dat.getTaskID());
                                task.setStatus(Task.WAITING_TO_DECODE);
                                tasklist.remove(new Integer(dat.getTaskID()));
                                tasklist.put(new Integer(dat.getTaskID()), task);
                                decodeManager.add(task);
                                fireEvent(event);
                            }
                            
                            if ( event instanceof HeadersUpdatedEvent ) {
                                UpdateHeadersTask t = (UpdateHeadersTask)event.getSource();
                                Task task = new Task((NntpGroup)t.getSource());
                                task.setTaskID(t.getTaskID());
                                task.setStatus(Task.DOWNLOADING);  // to match current in taskList
                                tasklist.remove(new Integer(t.getTaskID()));
                                fireEvent(event);
                            }
                            
                            /*
                            if ( event instanceof GroupsUpdatedEvent ) {
                                NntpServer server = (NntpServer)event.getSource();
                                tasklist.remove(new Integer(rt.getTaskID()));
                                fireEvent(event);
                            }
                            */
                            
                            if (event instanceof ArticleDownloadErrorEvent ) {
                                DownloadArticleTask dat = (DownloadArticleTask)event.getSource();
                                NntpArticleHeader header = (NntpArticleHeader)dat.getSource();
                                Task task = new Task(header);
                                task.setTaskID(dat.getTaskID());
                                task.setStatus("error: " + dat.errorMsg);
                                tasklist.remove(new Integer(dat.getTaskID()));
                                tasklist.put(new Integer(dat.getTaskID()), task);
                                fireEvent(event);
                            }
                            
                            if ( event instanceof HeadersUpdateErrorEvent ) {
                                UpdateHeadersTask uht = (UpdateHeadersTask)event.getSource();
                                Task t = (Task)tasklist.remove(new Integer(uht.getTaskID()));
                                t.setStatus("error: " + uht.errorMsg);
                                tasklist.put(new Integer(uht.getTaskID()), t);
                                fireEvent(event);
                            }
                            
                            //if (event instanceof GroupsUpdateErrorEvent ) {
                            //    NntpServer server = (NntpServer)event.getSource();
                            //}
                            
                            if (event instanceof DownloadCanceledEvent ) {
                                DownloadArticleTask dat = (DownloadArticleTask)event.getSource();
                                NntpArticleHeader header = (NntpArticleHeader)dat.getSource();
                                Task task = new Task(header);
                                task.setTaskID(dat.getTaskID());
                                tasklist.remove(new Integer(dat.getTaskID()));
                                fireEvent(event);
                            }
                        }
                    });
                    
                    // start a RunnableTask and map it in our Map
                    Thread t = pool.start(rt);
                    threadMap.put(t, rt);
                    //log.debug("queue size " + queue.size());
                    
                    // update TaskTable
                    Task ct = new Task(rt.getSource(), Task.DOWNLOADING);
                    ct.setTaskID(rt.getTaskID());
                    log.debug("removing from taskList: " + ct.getTaskID());
                    tasklist.remove(new Integer(ct.getTaskID()));
                    log.debug("adding to taskList: " + ct.getTaskID());
                    tasklist.put(new Integer(ct.getTaskID()),ct);
                    UpdateHeadersEvent event = new UpdateHeadersEvent(ct);
                    fireEvent(event);
                    if ( rt.getSource() instanceof NntpArticleHeader ) {
                        NntpArticleHeader curHeader = (NntpArticleHeader)rt.getSource();
                        curHeader.setStatus(NntpArticleHeader.STATUS_DOWNLOADING);
                    }
                } 
                catch (NoThreadException nte) {
                    log.error("nte exception: " + nte.toString());
                    log.error("queueing target with total " + queue.size());
                } 
                catch (InterruptedException ie) {
                    // give up!
                    log.error("could not queue download " + ie.toString());
                } 
                catch (IndexOutOfBoundsException oob) {
                    log.warn("queue size is 0 somehow: " + oob.toString());
                } 
                catch (Exception e) {
                    log.error("fatal error");
                }
            } 
            else {	// queue size is 0
                try { 
                    Thread.sleep(1000); 
                } 
                catch (InterruptedException ie) {
                    log.warn("queue thread interrupted " + ie.toString());
                }
            }
        } // end while true
    } // end run()
    
}
