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

import org.w3c.dom.Element;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * The scilla log4j logger implementation.
 *
 * @version $Revision: 1.2 $
 * @author R.W. van 't Veer
 */
public class LoggerLog4jImpl implements Logger
{
    Category cat;

    public LoggerLog4jImpl () { }

    private LoggerLog4jImpl (Category cat)
    {
	this.cat = cat;
    }

    public static Logger getInstance (Class clazz)
    {
	return new LoggerLog4jImpl(Category.getInstance(clazz));
    }

    public void configure (Object obj)
    {
	if (obj instanceof Properties)
	{
	    PropertyConfigurator.configure((Properties) obj);
	}
	else if (obj instanceof Element)
	{
	    DOMConfigurator.configure((Element) obj);
	}
	else
	{
	    BasicConfigurator.configure();
	}
    }

    public void assert (boolean assertion, String msg) { cat.assert(assertion, msg); }
    public void debug (Object msg) { cat.debug(msg); }
    public void debug (Object msg, Throwable t) { cat.debug(msg, t); }
    public void info(Object msg) { cat.info(msg); }
    public void info(Object msg, Throwable t) { cat.info(msg, t); }
    public void warn(Object msg) { cat.warn(msg); }
    public void warn(Object msg, Throwable t) { cat.warn(msg, t); }
    public void error(Object msg) { cat.error(msg); }
    public void error(Object msg, Throwable t) { cat.error(msg, t); }
    public void fatal(Object msg) { cat.fatal(msg); }
    public void fatal(Object msg, Throwable t) { cat.fatal(msg, t); }
    public boolean isDebugEnabled() { return cat.isDebugEnabled(); }
    public boolean isInfoEnabled() { return cat.isInfoEnabled(); }
}
