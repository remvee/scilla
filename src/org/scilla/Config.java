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

import java.util.Properties;
import java.util.Vector;
import java.util.StringTokenizer;
import java.io.InputStream;
import java.io.IOException;

import org.scilla.converter.*;

public class Config
{
    private static Config _instance = null;
    static final String PROPERTY_FILE = "org/scilla/Config.properties";
    static final String CACHE_DIR_PROPERTY = "Config.cacheDir";
    static final String SOURCE_DIR_PROPERTY = "Config.sourceDir";
    static final String CONVERTERS_PROPERTY = "Config.converters";

    private Properties param = new Properties();

    protected Config ()
    {
	InputStream in = null;
	try
	{
	    in = this.getClass().getClassLoader().getResourceAsStream(PROPERTY_FILE);
	    if (in != null)
	    {
		param.load(in);
		System.err.println("Config: properties loaded");
	    }
	    else
	    {
		System.err.println("Config: no properties loaded");
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

    public static synchronized Config getInstance ()
    {
        if (_instance == null) _instance = new Config();
        return _instance;

    }

    public static String getParameter (String key)
    {
        String value = getInstance().param.getProperty(key);
        return value;
    }

    public static String getCacheDir ()
    {
	return getParameter(CACHE_DIR_PROPERTY);
    }

    public static String getSourceDir ()
    {
	return getParameter(SOURCE_DIR_PROPERTY);
    }

    static Class[] converters = null;
    public static Class[] getConverters ()
    {
	if (converters == null)
	{
	    String s = getParameter(CONVERTERS_PROPERTY);
	    StringTokenizer st = new StringTokenizer(s, ",:; \t\r\n");
	    Vector v = new Vector();
	    while (st.hasMoreTokens())
	    {
		String cn = null;
		try
		{
		    cn = st.nextToken();
		    Class c = Class.forName(cn);
		    Converter cnv = (Converter) c.newInstance();
		    if (cnv.isFunctional())
		    {
			v.add(Class.forName(cn));
		    }
		    else
		    {
			System.err.println("Config.getConverters: converter not functional: " + cn);
		    }
		}
		catch (Throwable ex)
		{
		    System.err.println("Config.getConverters: unable to access converter: " + cn + ": " + ex.toString());
		}
	    }
	    converters = (Class[]) v.toArray(new Class[0]);
	}
	return converters;
    }
}
