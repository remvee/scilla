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

package org.scilla.info;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.scilla.util.MimeType;

/**
 * The scilla media info factory.
 *
 * @version $Revision: 1.1 $
 * @author R.W. van 't Veer
 */
public class InfoFactory {
    private final static Log log = LogFactory.getLog(InfoFactory.class);

    public static Info get (String fname) {
	CacheEntry ce = (CacheEntry) infoCache.get(fname);
	long timestamp = (new File(fname)).lastModified();

	// see if cache entry valid
	if (ce != null && ce.timestamp == timestamp) {
	    if (log.isDebugEnabled()) {
		log.debug("pull info ("+ce.info+") from cache ("+ce+") for "+fname);
	    }
	    return ce.info;
	}

	// determine type of info
	String type = MimeType.getTypeFromFilename(fname);

	// get info
	Info info = null;
	if (type.startsWith("audio/")) {
	    info = new AudioInfo(fname);
	} else if (type.startsWith("image/")) {
	    info = new ImageInfo(fname);
	} else if (type.startsWith("video/")) {
	    // info = new VideoInfo(fname);
	}

	// cache it
	if (info != null) {
	    ce = new CacheEntry();
	    ce.info = info;
	    ce.timestamp = timestamp;
	    infoCache.put(fname, ce);

	    if (log.isDebugEnabled()) {
		log.debug("cached ("+ce+") info ("+info+") for "+fname);
	    }
	}

	return info;
    }

    /** filename to info map for caching media info */
    private static Map infoCache = Collections.synchronizedMap(new HashMap());
    /** simple cache entry */
    private static class CacheEntry {
	/** media info */
	Info info = null;
	/** file timestamp */
	long timestamp = -1L;
    }
}
