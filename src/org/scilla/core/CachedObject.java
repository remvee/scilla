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

package org.scilla.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import org.scilla.*;

/**
 * A cached object is a media object already existing in cache,
 * finished or not.
 *
 * @author R.W. van 't Veer
 * @version $Revision: 1.11 $
 */
public class CachedObject implements MediaObject
{
    private static final Logger log = LoggerFactory.get(CachedObject.class);
    private static final CacheManager cache = CacheManager.getInstance();

    /** buffer size */
    public static final int BUFFER_SIZE = 4096;
    /** milis to wait in wait for file loop */

    // output file name
    private String filename;

    /**
     * Construct media object for cached object.
     * @param filename full name of result file
     */
    public CachedObject (String filename)
    {
	this.filename = filename;
    }

    public InputStream getStream ()
    throws ScillaException
    {
	RunnerObject runner = cache.getRunner(filename);
	log.debug("write: runner="+runner);

	return new MediaStream(filename, runner);
    }

    /**
     * @return file length or -1 if still being generated
     */
    public long getLength ()
    {
	if (cache.getRunner(filename) != null) return -1;

	File f = new File(filename);
	if (log.isDebugEnabled()) log.debug("length="+f.length());
	return f.length();
    }
}
