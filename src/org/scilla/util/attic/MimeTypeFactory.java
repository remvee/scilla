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

package org.scilla.util;

import java.util.Enumeration;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * Class for mapping filenames to mime types and visa versa.
 *
 * @version $Id: MimeTypeFactory.java,v 1.2 2001/09/21 12:38:27 remco Exp $
 * @author R.W. van 't Veer
 */
public class MimeTypeFactory
{
    public static final String PROPERTY_FILE = "org/scilla/util/MimeTypeFactory.properties";
    static final String PROPERTY_PREFIX = "MimeTypeExt";

    static Properties param = null;

    private static void readProperties ()
    {
	param = new Properties();
	MimeTypeFactory f = new MimeTypeFactory();
    }

    protected MimeTypeFactory ()
    {
	InputStream in = null;
	try
	{
	    in = this.getClass().getClassLoader().getResourceAsStream(PROPERTY_FILE);
	    if (in != null)
	    {
		param.load(in);
		System.err.println("MimeTypeFactory: properties loaded");
	    }
	    else
	    {
		System.err.println("MimeTypeFactory: no properties loaded");
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

    /**
     * @param ext filename extension
     * @return mime type
     */
    public static String getTypeFromFileExtension(String ext)
    {
	// public methods using param must make sure the propfile is loaded
	if (param == null) readProperties();

	String type = param.getProperty(PROPERTY_PREFIX+"."+ext);
	return type;
    }

    /**
     * @param fname filename
     * @return mime type
     */
    public static String getTypeFromFilename (String fname)
    {
	return getTypeFromFileExtension(getExtensionFromFilename(fname));
    }

    /**
     * @param type mime type
     * @return filename extension
     */
    public static String getExtensionForType (String type)
    {
	// public methods using param must make sure the propfile is loaded
	if (param == null) readProperties();

	// find first match
	Enumeration e = param.propertyNames();
	while (e.hasMoreElements())
	{
	    String key = (String) e.nextElement();
	    if (type.equals(param.getProperty(key)))
	    {
		return getExtensionFromFilename(key);
	    }
	}

	return null;
    }

    /**
     * @param fname filename
     * @return filename extension
     */
    private static String getExtensionFromFilename (String fname)
    {
	int i = fname.lastIndexOf('.');
	if (i != -1)
	{
	    return fname.substring(i+1);
	}

	return null;
    }
}
