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

import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import java.util.StringTokenizer;
import java.io.InputStream;
import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Category;

import org.scilla.converter.*;

/**
 * The scilla configuration factory.
 *
 * @version $Revision: 1.1 $
 * @author R.W. van 't Veer
 */
public class ConfigFactory
{
    static Category log = Category.getInstance(ConfigFactory.class);
    static { BasicConfigurator.configure(); }

    static Config config = null;

    public static final String PROPERTY_FILE = "org/scilla/Config.properties";

    private static ConfigFactory _instance = null;

    /** This is a Singleton. */
    protected ConfigFactory () { /* nop */ }

    /**
     * @return singleton instance
     */
    private static synchronized ConfigFactory getInstance ()
    {
        if (_instance == null) _instance = new ConfigFactory();
        return _instance;
    }

    /**
     * Get current configuration object.
     * @return current configuration
     */
    public static synchronized Config get ()
    {
	if (config == null)
	{
	    InputStream in = null;
	    try
	    {
		ConfigFactory thiz = ConfigFactory.getInstance();
		in = thiz.getClass().getClassLoader().getResourceAsStream(PROPERTY_FILE);
		if (in != null)
		{
		    config = new ConfigPropertiesImpl(in);
		    log.info("properties loaded");
		}
		else log.warn("no properties loaded");
	    }
	    catch (IOException e)
	    {
		log.warn(e);
	    }
	    finally
	    {
		if (in != null)
		{
		    try { in.close(); }
		    catch (IOException e) { log.warn(e); }
		}
	    }
	}
	return config;
    }
}
