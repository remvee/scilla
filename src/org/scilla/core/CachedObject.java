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
 * @version $Revision: 1.3 $
 */
public class CachedObject implements MediaObject
{
    final static int BUFFER_SIZE = 4096;
    final static int WAIT_FOR_FILE_TIMEOUT = 100;
    final static int WAIT_FOR_READ_TIMEOUT = 100;

    String filename;
    CacheManager cache = CacheManager.getInstance();

    /**
     * Create media object.
     * @param filename full name of result file
     */
    public CachedObject (String filename)
    {
	this.filename = filename;
    }

    /**
     * Write data to stream.  If a converter is still running this
     * method will follow the file till the converter is finished.
     * @param out stream to write to
     * @throws ScillaException when a read or write problem occures
     */
    public void write (OutputStream out)
    throws ScillaException
    {
	RunnerObject obj = cache.getRunner(filename);
	File f = new File(filename);

	// see if simular request is in running list
	if (obj != null && ! obj.hasFinished())
	{
	    // wait for file to appear
	    while (! f.exists() && ! obj.hasFinished())
	    {
		try
		{
		    Thread.currentThread().sleep(WAIT_FOR_FILE_TIMEOUT);
		}
		catch (InterruptedException ex) { }
	    }
	    if (! f.exists()) throw new ScillaNoOutputException();
	}

	// catch up with output file and write it to out
	InputStream in = null;
	try
	{
	    in = new FileInputStream(f);
	    int n;
	    byte[] b = new byte[BUFFER_SIZE];

	    // read without going beyond EOF when k
	    while (obj != null && ! obj.hasFinished())
	    {
		try
		{
		    Thread.currentThread().sleep(WAIT_FOR_READ_TIMEOUT);
		}
		catch (InterruptedException ex) { }

		while (in.available() > 0 && ! obj.hasFinished())
		{
		    n = in.read(b);
		    try
		    {
			out.write(b, 0, n);
		    }
		    catch (IOException ex)
		    {
			throw new ScillaOutputIOException(ex);
		    }
		}
	    }

	    // till EOF
	    while ((n = in.read(b)) != -1)
	    {
		try
		{
		    out.write(b, 0, n);
		}
		catch (IOException ex)
		{
		    throw new ScillaOutputIOException(ex);
		}
	    }
	}
	catch (IOException ex)
	{
	    throw new ScillaInputIOException(ex);
	}
	finally
	{
	    if (in != null)
	    {
		try { in.close(); }
		catch (IOException ex) { }
	    }
	}
    }

    public boolean allowCaching () { return false; }
}
