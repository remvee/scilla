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

import java.io.PrintStream;

/**
 * The scilla simple logger implementation.
 *
 * @version $Revision: 1.2 $
 * @author R.W. van 't Veer
 */
public class LoggerSimpleImpl implements Logger
{
    String cname;
    PrintStream out = System.out;

    boolean isDebugEnabled = true;
    boolean isInfoEnabled = true;

    private LoggerSimpleImpl (String cname)
    {
	this.cname = cname;
    }

    public static Logger getInstance (Class clazz)
    {
	return new LoggerSimpleImpl(clazz.getName());
    }

    public void configure () throws Exception { /* nop */ }

    public void assert (boolean assertion, String msg)
    {
	if (! assertion) debug(msg);
    }
    public void debug (Object msg)
    {
	out.println(cname+": DEBUG: "+msg);
    }
    public void debug (Object msg, Throwable t)
    {
	out.println(cname+": DEBUG: "+msg+": "+t);
    }
    public void info(Object msg)
    {
	out.println(cname+": INFO: "+msg);
    }
    public void info(Object msg, Throwable t)
    {
	out.println(cname+": INFO: "+msg+": "+t);
    }
    public void warn(Object msg)
    {
	out.println(cname+": WARN: "+msg);
    }
    public void warn(Object msg, Throwable t)
    {
	out.println(cname+": WARN: "+msg+": "+t);
    }
    public void error(Object msg)
    {
	out.println(cname+": ERROR: "+msg);
    }
    public void error(Object msg, Throwable t)
    {
	out.println(cname+": ERROR: "+msg+": "+t);
    }
    public void fatal(Object msg)
    {
	out.println(cname+": FATAL: "+msg);
    }
    public void fatal(Object msg, Throwable t)
    {
	out.println(cname+": FATAL: "+msg+": "+t);
    }
    public boolean isDebugEnabled() { return isDebugEnabled; }
    public boolean isInfoEnabled() { return isInfoEnabled; }
}
