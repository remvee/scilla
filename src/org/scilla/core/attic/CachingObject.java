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

import java.io.OutputStream;

import org.scilla.*;

/**
 * A caching object is a media object currently being written to
 * cache.
 *
 * @author R.W. van 't Veer
 * @version $Revision: 1.4 $
 */
public class CachingObject implements MediaObject
{
    String filename;
    RunnerObject obj;
    CacheManager cache = CacheManager.getInstance();

    /**
     * Create media object and register runner.
     * @param obj running conversion
     * @param filename full name of result file
     */
    public CachingObject (RunnerObject obj, String filename)
    {
        this.obj = obj;
	this.filename = filename;
	cache.addRunner(filename, obj);
    }

    public void write (OutputStream out)
    throws ScillaException
    {
	// make sure directory for cache file exists
	// change output file to cache location
	obj.setOutputFile(filename);
	obj.setDeleteOutput(false);

	// let runner handle the rest
	obj.write(out);

	// remove runner
	cache.removeRunner(filename);
    }

    /**
     * @return always false, already being cached
     */
    public boolean allowCaching () { return false; }
}
