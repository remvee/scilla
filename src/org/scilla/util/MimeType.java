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

import java.util.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class for mapping filenames to mime types and visa versa.
 * Configuration is read from a properties file.  Please note the
 * order of the properties is important; from two types with the
 * same extensions the first will be returned by
 * <tt>getTypeFromFilename</tt>.
 * @version $Revision: 1.11 $
 * @author R.W. van 't Veer
 */
public class MimeType {
    /** logger */
    private static final Log log = LogFactory.getLog(MimeType.class);

    /** name of property file to hold mime types */
    public static final String PROPERTY_FILE = "org/scilla/util/MimeType.properties";

    /** map mime-type specs to list file extension names (first is prefered) */
    private Map typeToExt = new HashMap();
    /** map file extension names to mime-type specs (first is prefered) */
    private Map extToType = new HashMap();

    private MimeType () {
	ClassLoader cl = getClass().getClassLoader();

	// load properties
	Properties p = new Properties();
	{
	    InputStream in = null;
	    try {
		in = cl.getResourceAsStream(PROPERTY_FILE);
		p.load(in);
	    } catch (Throwable ex) {
		final String msg = "unable to load property file \""+PROPERTY_FILE+"\"";
		log.fatal(msg, ex);
		throw new RuntimeException(msg);
	    } finally {
		if (in != null) {
		    try { in.close(); } catch (IOException ex) { /* ignore */ }
		}
	    }
	}

	// read the key order, HACK HACK HACK..
	List keys = new ArrayList();
	{
	    BufferedReader br = null;
	    try {
		InputStream in = cl.getResourceAsStream(PROPERTY_FILE);
		br = new BufferedReader(new InputStreamReader(in));
		String l = null;
		while ((l = br.readLine()) != null) {
		    if (l.trim().startsWith("#") || l.trim().startsWith("!")) {
			continue;
		    }
		    StringTokenizer st = new StringTokenizer(l, "=: \t\n");
		    if (st.hasMoreTokens()) {
			keys.add(st.nextToken());
		    }
		}
	    } catch (Throwable ex) {
		final String msg = "unable to read key order from \""+PROPERTY_FILE+"\"";
		log.fatal(msg, ex);
		throw new RuntimeException(msg);
	    } finally {
		if (br != null) {
		    try { br.close(); } catch (IOException ex) { /* ignore */ }
		}
	    }
	}

	// populate maps
	for (Iterator it = keys.iterator(); it.hasNext();) {
	    String key = (String) it.next();
	    String eval = p.getProperty(key);

	    // property value to list
	    List val = new ArrayList();
	    for (StringTokenizer st = new StringTokenizer(eval); st.hasMoreTokens();) {
		val.add(st.nextToken());
	    }

	    // add type to extensions mappings
	    {
		List l = (List) typeToExt.get(key);
		if (l == null) {
		    l = new ArrayList();
		    typeToExt.put(key, l);
		}
		l.addAll(val);
	    }

	    // extensions to type mappings
	    for (Iterator it1 = val.iterator(); it1.hasNext();) {
		String ext = (String) it1.next();

		List l = (List) extToType.get(ext);
		if (l == null) {
		    l = new ArrayList();
		    extToType.put(ext, l);
		}
		l.add(key);
	    }
	}
	if (log.isDebugEnabled()) {
	    log.debug("extension to type mappings="+extToType);
	    log.debug("type to extension mappings="+typeToExt);
	}
    }

    private static synchronized MimeType getInstance () {
	if (_instance == null) {
	    _instance = new MimeType();
	}
	return _instance;
    }
    private static MimeType _instance = null;

    /**
     * @param ext filename extension
     * @return mime type
     */
    public static String getTypeFromFileExtension (String ext) {
	List l = (List) getInstance().extToType.get(ext);
	return (String) (l != null && l.size() > 0 ? l.get(0) : null);
    }

    /**
     * @param fname filename
     * @return mime type
     */
    public static String getTypeFromFilename (String fname) {
	return getTypeFromFileExtension(getExtensionFromFilename(fname));
    }

    /**
     * @param type mime type
     * @return filename extension
     */
    public static String getExtensionForType (String type) {
	List l = (List) getInstance().typeToExt.get(type);
	return (String) (l != null && l.size() > 0 ? l.get(0) : null);
    }

    /**
     * @param fname filename
     * @return filename extension
     */
    public static String getExtensionFromFilename (String fname) {
        int i = fname.lastIndexOf('.');
        return i == -1 ? null : fname.substring(i + 1);
    }

    /**
     * Debugging..
     */
    public static void main (String[] args) {
	MimeType mt = new MimeType();
	for (int i = 0; i < args.length; i++) {
	    String t = args[i];
	    System.out.println("e2t("+t+")=" + getTypeFromFileExtension(t));
	    System.out.println("t2e("+t+")=" + getExtensionForType(t));
	}
    }
}
