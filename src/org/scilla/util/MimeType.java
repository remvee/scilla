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

import java.util.Iterator;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.scilla.Config;

/**
 * Class for mapping filenames to mime types and visa versa.
 * TODO ugly handling of <tt>param</tt> element..
 * @version $Revision: 1.8 $
 * @author R.W. van 't Veer
 */
public class MimeType {
    private static final Log log = LogFactory.getLog(MimeType.class);

    /** name of property file to hold mime types */
    public static final String PROPERTY_FILE = "org/scilla/util/MimeType.properties";
    /** prefix of mime type properties */
    public static final String PROPERTY_PREFIX = "MimeTypeExt";

    private static Properties param = null;

    private static void readProperties () {
        param = new Properties();
        MimeType f = new MimeType();
    }

    protected MimeType () {
        InputStream in = null;
        try {
            in = this.getClass().getClassLoader().getResourceAsStream(PROPERTY_FILE);
            if (in != null) {
                param.load(in);
                log.debug("properties loaded: " + PROPERTY_FILE);
            } else {
                log.fatal("properties not available: " + PROPERTY_FILE);
            }
        } catch (IOException ex) {
            log.fatal("can't load properties: " + PROPERTY_FILE, ex);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @param ext filename extension
     * @return mime type
     */
    public static String getTypeFromFileExtension(String ext) {
	if (log.isDebugEnabled()) {
	    log.debug("ext="+ext);
	}

        // public methods using param must make sure the propfile is loaded
        if (param == null) {
            readProperties();
	}

        String type = param.getProperty(PROPERTY_PREFIX+"."+ext);
	if (log.isDebugEnabled()) {
	    log.debug("type="+type);
	}
        return type;
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
	if (log.isDebugEnabled()) {
	    log.debug("type="+type);
	}

        // public methods using param must make sure the propfile is loaded
        if (param == null) {
            readProperties();
	}

        // find first match
	for (Iterator it = param.keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();
            if (type.equals(param.getProperty(key))) {
		String ext = getExtensionFromFilename(key);
		if (log.isDebugEnabled()) {
		    log.debug("ext="+ext);
		}
                return ext;
            }
        }

	log.debug("no extension found");
        return null;
    }

    /**
     * @param fname filename
     * @return filename extension
     */
    public static String getExtensionFromFilename (String fname) {
        int i = fname.lastIndexOf('.');
        if (i != -1) {
            return fname.substring(i+1);
        }

        return null;
    }
}
