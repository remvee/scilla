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
 * The scilla configuration.
 *
 * @see org.scilla.ConfigFactory#get()
 * @version $Revision: 1.5 $
 * @author R.W. van 't Veer
 */
public interface Config
{
    /** key to cache directory */
    public static final String CACHE_DIR_KEY = "cache.dir";
    /** key to source directory */
    public static final String SOURCE_DIR_KEY = "source.dir";
    /** key to converter class list */
    public static final String CONVERTERS_KEY = "converters.classes";

// accessors
    /**
     * Enumerate all configuration keys.
     * @return key enumeration
     */
    public Enumeration enumerate ();

    /**
     * Test if key exists in configuration.
     * @param key handle to resource
     * @return true if key available
     */
    public boolean exists (String key);

    /**
     * Get integer value from configuration.
     * @param key handle to resource
     * @return integer value associated with key
     */
    public int getInt(String key);

    /**
     * Get string value from configuration.
     * @param key handle to resource
     * @return string value associated with key
     */
    public String getString (String key);

    /**
     * Get string array from configuration.  This method
     * typically uses a StringTokenizer to split a string into an
     * array of strings.
     * @param key handle to resource
     * @param delim string for delimiter characters
     * @return array of strings associated with key
     */
    public String[] getStringArray (String key, String delim);

    /**
     * Get class array from configuration.  This method
     * initializers class objects associated with this key and
     * return an array of these classes.
     * @param key handle to resource
     * @return array of classes associated with key
     */
    public Class[] getClasses (String key);

// modifiers
    /**
     * Set string value in configuration.
     * @param key handle to resource
     * @param val string value
     */
    public void setString (String key, String val);
}
