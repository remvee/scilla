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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.xml.DOMConfigurator;


/**
 * The scilla log4j logger implementation.
 *
 * @version $Revision: 1.5 $
 * @author R.W. van 't Veer
 */
public class LoggerLog4jImpl implements Logger
{
    org.apache.log4j.Logger logger;

    public static final String LOG4J_PROPERTY_FILE = "org/scilla/log4j.properties";
    public static final String LOG4J_XML_FILE      = "org/scilla/log4j.xml";

    public LoggerLog4jImpl () { }

    private LoggerLog4jImpl (org.apache.log4j.Logger logger)
    {
	this.logger = logger;
    }

    public static Logger getInstance (Class clazz)
    {
	return new LoggerLog4jImpl(org.apache.log4j.Logger.getLogger(clazz));
    }

    /**
     * Configure log4j.  First by trying <CODE>LOG4J_XML_FILE</CODE>,
     * if that fails <CODE>LOG4J_PROPERTY_FILE</CODE> and if that
     * fails <CODE>BasicConfigurator</CODE> is used.
     * @see #LOG4J_XML_FILE
     * @see org.apache.log4j.xml.DOMConfigurator
     * @see #LOG4J_PROPERTY_FILE
     * @see org.apache.log4j.PropertyConfigurator
     * @see org.apache.log4j.BasicConfigurator
     */
    public void configure ()
    throws Exception
    {
	ClassLoader cl = this.getClass().getClassLoader();
	boolean configured = false;

	if (! configured) // configure from xml file
	{
	    InputStream in = null;
	    try
	    {
		in = cl.getResourceAsStream(LOG4J_XML_FILE);
		if (in != null)
		{
		    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		    Document d = db.parse(in);
		    DOMConfigurator.configure(d.getDocumentElement());
		    configured = true;
		}
	    }
	    catch (Exception e)
	    {
		e.printStackTrace();
	    }
	    finally
	    {
		if (in != null)
		{
		    try { in.close(); }
		    catch (IOException e) { e.printStackTrace(); }
		}
	    }
	}

	if (! configured) // configure from properties file
	{
	    InputStream in = null;
	    try
	    {
		in = cl.getResourceAsStream(LOG4J_PROPERTY_FILE);
		if (in != null)
		{
		    Properties prop = new Properties();
		    prop.load(in);
		    PropertyConfigurator.configure(prop);
		    configured = true;
		}
	    }
	    catch (IOException e)
	    {
		e.printStackTrace();
	    }
	    finally
	    {
		if (in != null)
		{
		    try { in.close(); }
		    catch (IOException e) { e.printStackTrace(); }
		}
	    }
	}
    }

    public void assertLog (boolean assertion, String msg) { logger.assertLog(assertion, msg); }
    public void debug (Object msg) { logger.debug(msg); }
    public void debug (Object msg, Throwable t) { logger.debug(msg, t); }
    public void info(Object msg) { logger.info(msg); }
    public void info(Object msg, Throwable t) { logger.info(msg, t); }
    public void warn(Object msg) { logger.warn(msg); }
    public void warn(Object msg, Throwable t) { logger.warn(msg, t); }
    public void error(Object msg) { logger.error(msg); }
    public void error(Object msg, Throwable t) { logger.error(msg, t); }
    public void fatal(Object msg) { logger.fatal(msg); }
    public void fatal(Object msg, Throwable t) { logger.fatal(msg, t); }
    public boolean isDebugEnabled() { return logger.isDebugEnabled(); }
    public boolean isInfoEnabled() { return logger.isInfoEnabled(); }
}
