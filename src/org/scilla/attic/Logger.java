/*
 * scilla
 *
 * Copyright (C) 2001  R.W. van 't Veer
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 */

package org.scilla;

/**
 * The scilla logger interface.  Here is the list of logging
 * levels.
 * <OL>
 *   <LI>fatal</LI>
 *   <LI>error</LI>
 *   <LI>warn</LI>
 *   <LI>info</LI>
 *   <LI>debug</LI>
 * </OL>
 *
 * @version $Revision: 1.3 $
 * @author R.W. van 't Veer
 */
public interface Logger
{
    /**
     * Configure logger interface.
     * @param obj a configuration object
     * @throws Exception when configuration fails
     */
    public void configure () throws Exception;

    /**
     * If <CODE>assertion</CODE> parameter is <CODE>false</CODE>,
     * then logs <CODE>msg</CODE> as an error
     * statement.
     * @param assertion
     * @param msg error message
     */
    public void assertLog (boolean assertion, String msg);
    /**
     * Log a message at <EM>debug</EM> level.
     * @param msg message to log
     */
    public void debug (Object msg);
    /**
     * Log a message at <EM>debug</EM> level.
     * @param msg message to log
     */
    public void debug (Object msg, Throwable t);
    /**
     * Log a message at <EM>info</EM> level.
     * @param msg message to log
     * @param t exception to log, including stack trace
     */
    public void info (Object msg);
    /**
     * Log a message and exception at <EM>info</EM> level.
     * @param msg message to log
     * @param t exception to log, including stack trace
     */
    public void info (Object msg, Throwable t);
    /**
     * Log a message at <EM>warn</EM> level.
     * @param msg message to log
     */
    public void warn (Object msg);
    /**
     * Log a message and exception at <EM>warn</EM> level.
     * @param msg message to log
     * @param t exception to log, including stack trace
     */
    public void warn (Object msg, Throwable t);
    /**
     * Log a message at <EM>error</EM> level.
     * @param msg message to log
     */
    public void error (Object msg);
    /**
     * Log a message and exception at <EM>error</EM> level.
     * @param msg message to log
     * @param t exception to log, including stack trace
     */
    public void error (Object msg, Throwable t);
    /**
     * Log a message at <EM>fatal</EM> level.
     * @param msg message to log
     */
    public void fatal (Object msg);
    /**
     * Log a message and exception at <EM>fatal</EM> level.
     * @param msg message to log
     * @param t exception to log, including stack trace
     */
    public void fatal (Object msg, Throwable t);
    /**
     * Test if debug level logging is enabled.
     * @return true if debug level logging is enabled
     */
    public boolean isDebugEnabled ();
    /**
     * Test if info level logging is enabled.
     * @return true if info level logging is enabled
     */
    public boolean isInfoEnabled ();
}
