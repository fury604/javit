/**
 * RunnableTask.java
 * 
 * This interface represents the Runnable payload for Thread Tasks.
 * Each task will contain the necessary instructions to carry out some 
 * Thread related task in the system, normally done in the background to 
 * the parent thread.
 */
package net.nexxus.task;

import net.nexxus.event.GUIEventListener;
import net.nexxus.event.GUIEvent;
import net.nexxus.event.EventListenerInterface;

public interface RunnableTask extends Runnable, EventListenerInterface {

    public Object getSource();
    public int getTaskID();
    public void setTaskID(int id);
    public void cancel();

}
