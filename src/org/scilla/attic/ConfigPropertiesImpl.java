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
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.util.StringTokenizer;
import java.io.InputStream;
import java.io.IOException;

/**
 * The scilla configuration implementation using property files.
 *
 * @version $Revision: 1.6 $
 * @author R.W. van 't Veer
 */
public class ConfigPropertiesImpl implements Config
{
    private static final Logger log = LoggerFactory.getLogger(ConfigPropertiesImpl.class);

    /** name of configuration file */
    public static final String PROPERTY_FILE = "org/scilla/Config.properties";

    private final Properties prop = new Properties();

    public ConfigPropertiesImpl ()
    {
	InputStream in = null;

	// read property file
	try
	{
	    ClassLoader cl = this.getClass().getClassLoader();
	    in = cl.getResourceAsStream(PROPERTY_FILE);
	    if (in != null)
	    {
		prop.load(in);
		log.debug("properties loaded: "+PROPERTY_FILE);
	    }
	    else
	    {
		log.fatal("properties not avialable: "+PROPERTY_FILE);
	    }

	}
	catch (IOException ex)
	{
	    log.fatal("can't load properties: " + PROPERTY_FILE, ex);
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

// cache for expensive operations
    private final Hashtable stringArrays = new Hashtable();
    private final Hashtable classArrays = new Hashtable();
    private final Hashtable[] cache = { stringArrays, classArrays };

// accessors
    public Enumeration keys ()
    {
	return prop.propertyNames();
    }

    public boolean exists (String key)
    {
	return prop.containsKey(key);
    }

    public boolean getBoolean (String key)
    {
	return Boolean.valueOf(prop.getProperty(key)).booleanValue();
    }

    public int getInt (String key)
    throws NumberFormatException
    {
	return Integer.parseInt(prop.getProperty(key));
    }

    public String getString (String key)
    {
	return prop.getProperty(key);
    }

    public String[] getStringArray (String key, String delim)
    {
	if (stringArrays.containsKey(key))
	{
	    return (String[]) stringArrays.get(key);
	}

	String s = prop.getProperty(key);
	StringTokenizer st = new StringTokenizer(s, delim);
	Vector v = new Vector();
	while (st.hasMoreTokens())
	{
	    v.add(st.nextToken());
	}
	String[] val = (String[]) v.toArray(new String[0]);

	stringArrays.put(key, val);
	return val;
    }

    public Class[] getClasses (String key)
    {
	if (classArrays.containsKey(key))
	{
	    return (Class[]) classArrays.get(key);
	}

	String s = prop.getProperty(key);
	StringTokenizer st = new StringTokenizer(s, ",:; \t\r\n");
	Vector v = new Vector();
	while (st.hasMoreTokens())
	{
	    String cn = null;
	    try
	    {
		cn = st.nextToken();
		v.add(Class.forName(cn));
	    }
	    catch (Throwable ex)
	    {
		log.warn("getClasses("+key+"): "+cn+": "+ex);
		log.debug("", ex);
	    }
	}
	Class[] val = (Class[]) v.toArray(new Class[0]);

	classArrays.put(key, val);
	return val;
    }

// modifiers
    public void setString (String key, String val)
    {
	// see if we cache some array
	for (int i = 0; i < cache.length; i++)
	{
	    Hashtable ht = cache[i];
	    if (ht.containsKey(key)) ht.remove(key);
	}
	prop.setProperty(key, val);
    }
}
