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

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;
import java.lang.reflect.Method;

/**
 * The scilla logger factory.
 *
 * @version $Revision: 1.4 $
 * @author R.W. van 't Veer
 */
public class LoggerFactory
{
    static Logger log = null;

    private static Class loggerClass = null;
    private static Method getInstanceMethod = null;

    /**
     * Need the classloader to pick up configuration files.
     */
    private LoggerFactory ()
    {
	// see if we can load the log4j logger
	try
	{
	    loggerClass = this.getClass().forName("org.scilla.LoggerLog4jImpl");
	    Logger virgin = (Logger) loggerClass.newInstance();
	    virgin.configure();
	    getInstanceMethod = loggerClass.getMethod("getInstance", new Class[] { Class.class });
	}
	catch (Throwable t) { t.printStackTrace(); /* nop */ }

	// fallback to simple logger
	if (getInstanceMethod == null)
	{
	    try
	    {
		getInstanceMethod = LoggerSimpleImpl.class.getMethod("getInstance", new Class[] { Class.class });
	    }
	    catch (Throwable t) // logging is dead
	    {
		t.printStackTrace();
	    }
	}

	// try to say hi
	try
	{
	    log = (Logger) getInstanceMethod.invoke(null, new Object[] { LoggerFactory.class });
	    log.info("---> LOGGING STARTED <---");
	}
	catch (Throwable t)
	{
	    t.printStackTrace();
	}
    }

    private static LoggerFactory _instance = null;
    private volatile static boolean configuredFlag = false;

    private static synchronized void configure ()
    {
	if (_instance == null)
	{
	    _instance = new LoggerFactory();
	    configuredFlag = true;
	}
    }

    /**
     * Get logger for class.
     * @return logger object
     */
    public static synchronized Logger get (Class clazz)
    {
	if (! configuredFlag) LoggerFactory.configure();

	Logger log = null;
	try
	{
	    log = (Logger) getInstanceMethod.invoke(null, new Object[] { clazz });
	}
	catch (Throwable t) { /* nop */ }
	return log;
    }
}
