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
