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
import org.scilla.converter.*;

/**
 * A runner object is a media object currently being converted.
 *
 * @author R.W. van 't Veer
 * @version $Revision: 1.5 $
 */
public class RunnerObject implements MediaObject
{
    final static int BUFFER_SIZE = 4096;
    final static int WAIT_FOR_FILE_TIMEOUT = 100;
    final static int WAIT_FOR_READ_TIMEOUT = 100;

    Converter conv;
    CacheManager cache = CacheManager.getInstance();
    boolean deleteOutput = true;

    /**
     * Create media object.
     * @param conv ready to run coonverter
     */
    public RunnerObject (Converter conv)
    {
        this.conv = conv;
    }

    /**
     * A wrapper for Converter.start().  This runner will be added to
     * CacheManager runner list.  The write method will remove it upon
     * completion.
     * @see org.scilla.converter.Converter#start()
     * @see org.scilla.core.CacheManager#addRunner(String,RunnerObject)
     */
    public void start ()
    {
	// are we caching this?
	if (! deleteOutput)
	{
	    cache.addRunner(conv.getOutputFile(), this);
	}

	// start converter thread
	conv.start();
    }

    /**
     * @return true if runner has finished
     */
    public boolean hasFinished ()
    {
	return conv.hasFinished();
    }

    /**
     * Write data to stream.  First wait for the file to appear then
     * follow it till converter is finished.  Upon completion remove
     * this runner from the CacheManager runner list.
     * @param out stream to write to
     * @throws ScillaException when a read or write problem occures
     * @see #start()
     * @see org.scilla.core.CacheManager#removeRunner(String)
     */
    public void write (OutputStream out)
    throws ScillaException
    {
	String filename = conv.getOutputFile();

	// wait for file to appear
	File f = new File(filename);
	while (! f.exists() && ! conv.hasFinished())
	{
	    try
	    {
		Thread.currentThread().sleep(WAIT_FOR_FILE_TIMEOUT);
	    }
	    catch (InterruptedException ex) { }
	}
	if (! f.exists()) throw new ScillaNoOutputException();

	// catch up with output file and write it to out
	InputStream in = null;
	try
	{
	    in = new FileInputStream(f);
	    int n;
	    byte[] b = new byte[BUFFER_SIZE];
	    do
	    {
		try
		{
		    Thread.currentThread().sleep(WAIT_FOR_READ_TIMEOUT);
		}
		catch (InterruptedException ex) { }

		while (in.available() > 0 && ! conv.hasFinished())
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
	    while (! conv.hasFinished()); // until converter thread ready

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

	// remove runner if we cached this
	if (! deleteOutput)
	{
	    cache.removeRunner(filename);
	}
	else
	{
	    // delete file
	    f.delete();
	}
    }

    /**
     * Change result file location.
     * Don't call this method after write().
     * @param fname output filename
     * @see #write(java.io.OutputStream)
     */
    public void setOutputFile (String fname) { conv.setOutputFile(fname); }

    /**
     * Don't call this method after write().
     * @param flag true if output should be deleted when conversion is
     * finished
     */
    public void setDeleteOutput (boolean flag) { deleteOutput = flag; }

    /**
     * @return -1 because file length currently unknown
     */
    public long getLength () { return -1; }

    /**
     * @return always true
     */
    public boolean allowCaching () { return true; }
}
