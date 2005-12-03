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

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The scilla configuration class.
 *
 * @version $Revision: 1.17 $
 * @author R.W. van 't Veer
 */
public class Config extends HashMap {
    static final Log log = LogFactory.getLog(Config.class);

    /** key to cache directory */
    public static final String CACHE_DIR_KEY = "cache.dir";
    /** property file containing default data */
    public static final String PROPERTY_FILE = "org/scilla/Config.properties";

    /** singleton constructor */
    protected Config () {
        super();

        // defaults
        put(CACHE_DIR_KEY, System.getProperty("java.io.tmpdir"));

        // property file data
        Properties prop = new Properties();
        InputStream in = null;
        try {
            ClassLoader cl = getClass().getClassLoader();
            in = cl.getResourceAsStream(PROPERTY_FILE);
            if (in != null) {
                prop.load(in);
                log.info("properties loaded: " + PROPERTY_FILE);
            } else {
                log.fatal("properties not avialable: " + PROPERTY_FILE);
            }
        } catch (IOException ex) {
            log.fatal("can't load properties: " + PROPERTY_FILE, ex);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.warn(e);
                }
            }
        }
        putAll(prop);

        // env-entry data
        try {
            Context ctx = new InitialContext();
            Context env = (Context) ctx.lookup("java:comp/env");
            for (Enumeration en = env.list("scilla"); en.hasMoreElements();) {
                Object v = en.nextElement();
                if (v instanceof NameClassPair) {
                    String key = ((NameClassPair) v).getName();
                    String val = env.lookup("scilla/" + key).toString();
                    put(key, val);

                    log.info("env-entry: " + key + "="+val);
                }
            }
        } catch (NamingException ex) {
            log.info("no env-entries for scilla available");
        }
    }

    /**
     * @return only instance for current classloader
     */
    public static synchronized Config getInstance () {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }
    private static Config instance = null;

    /**
     * Get boolean value from configuration.
     * @param key handle to resource
     * @return true if key exists and is set to <CODE>true</CODE>
     * @see java.lang.Boolean#valueOf(String)
     */
    public boolean getBoolean (String key) {
        String v = getString(key);
        return v != null && Boolean.valueOf(v).booleanValue();
    }

    /**
     * Get integer value from configuration.
     * @param key handle to resource
     * @return integer value associated with key or <tt>-1</tt>
     * when value does not exist
     * @throws NumberFormatException when value associated with
     * key is not a number
     */
    public int getInt (String key)
    throws NumberFormatException {
        String v = getString(key);
        return v == null ? -1 : Integer.parseInt(v);
    }

    /**
     * Get string value from configuration.
     * @param key handle to resource
     * @return string value associated with key
     */
    public String getString (String key) {
        return (String) get(key);
    }

    /**
     * Get string array from configuration.  This method
     * uses a StringTokenizer to split a string into an
     * array of strings.
     * @param key handle to resource
     * @return array of strings associated with key
     */
    public String[] getStringArray (String key) {
        if (stringArrays.containsKey(key)) {
            return (String[]) stringArrays.get(key);
        }

        String v = getString(key);
        if (v == null) {
            return null;
        }

        StringTokenizer st = new StringTokenizer(v);
        List l = new Vector();
        while (st.hasMoreTokens()) {
            l.add(st.nextToken());
        }
        String[] val = (String[]) l.toArray(new String[l.size()]);

        stringArrays.put(key, val);
        return val;
    }
    private Map stringArrays = new HashMap();

    /**
     * Get class array from configuration.  This method
     * initializers class objects associated with this key and
     * return an array of these classes.
     * @param key handle to resource
     * @return array of classes associated with key
     */
    public Class[] getClassArray(String key) {
        if (classArrays.containsKey(key)) {
            return (Class[]) classArrays.get(key);
        }

        String[] v = getStringArray(key);
        if (v == null) {
            return null;
        }

        List l = new Vector();
        for (int i = 0; i < v.length; i++) {
            String cn = v[i];
            try {
                l.add(Class.forName(cn));
            } catch (ClassNotFoundException ex) {
                log.warn("getClasses(" + key + "): " + cn + ": " + ex);
            } catch (NoClassDefFoundError ex) {
                log.warn("getClasses(" + key + "): " + cn + ": " + ex);
            }
        }
        Class[] val = (Class[]) l.toArray(new Class[l.size()]);

        classArrays.put(key, val);
        return val;
    }
    private Map classArrays = new HashMap();
}
