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
 *
 * Original Copyright 1997-2011 teatrove.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * @author Brian S O'Neill
 */
package net.nexxus.util;

import java.util.EventObject;

public class ThreadPoolEvent extends EventObject {
	private Thread mThread;
	private Object payload;

	public ThreadPoolEvent(ThreadPool source, Thread thread) {
		super(source);
		mThread = thread;
	}

	public ThreadPoolEvent(ThreadPool source, Thread thread, Object payload) {
		super(source);
		mThread = thread;
		this.payload = payload;
	}


	public ThreadPool getThreadPool() {
		return(ThreadPool)getSource();
	}

	public Thread getThread() {
		return mThread;
	}

	public Object getPayload() {
		return payload;
	}
}
