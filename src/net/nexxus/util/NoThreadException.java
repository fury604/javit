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

/**
 * This exception is thrown by a {@link ThreadPool} when no thread is
 * available.
 */
public class NoThreadException extends InterruptedException {
  private boolean mIsClosed;

  public NoThreadException(String message) {
    super(message);
  }

  public NoThreadException(String message, boolean isClosed) {
    super(message);
    mIsClosed = isClosed;
  }

  public boolean isThreadPoolClosed() {
    return mIsClosed;
  }
}
