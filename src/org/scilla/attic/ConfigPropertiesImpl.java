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
 * The scilla configuration implemation using property files.
 *
 * @version $Revision: 1.3 $
 * @author R.W. van 't Veer
 */
public class ConfigPropertiesImpl implements Config
{
    static Logger log = LoggerFactory.getLogger(ConfigPropertiesImpl.class);

    private Properties prop = new Properties();

    private Hashtable stringArrays = new Hashtable();
    private Hashtable classArrays = new Hashtable();
    private Hashtable[] cache = { stringArrays, classArrays };

    public ConfigPropertiesImpl (InputStream in)
    throws IOException
    {
	prop.load(in);
    }

    public Enumeration enumerate ()
    {
	return prop.propertyNames();
    }

    public boolean exists (String key)
    {
	return prop.containsKey(key);
    }

// accessors
    public int getInt(String key)
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
