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
 * @version $Revision: 1.17 $
 */
public class RunnerObject implements MediaObject
{
    private static final Logger log = LoggerFactory.get(RunnerObject.class);
    private static final CacheManager cache = CacheManager.getInstance();

    /** buffer size */
    public static final int BUFFER_SIZE = 4096;
    /** milis to wait in wait for file loop */
    public static final int WAIT_FOR_FILE_TIMEOUT = 100;
    /** milis to wait for nieuw data in read loop */
    public static final int WAIT_FOR_READ_TIMEOUT = 100;

    private Converter conv;

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
	cache.addRunner(conv.getOutputFile(), this);

	// start converter thread
	final Converter conv_ = conv;
	Thread convThread = new Thread()
	{
	    public void run ()
	    {
		conv_.convert();
	    }
	};
	convThread.start();
    }

    /**
     * Determine if converter still running.  If so try to remove
     * runner from the CacheManager runner list.
     * @return true if converter has finished
     * @see org.scilla.core.CacheManager#removeRunner(String)
     */
    public boolean hasFinished ()
    {
	boolean flag = conv.hasFinished();

	// remove me runner list when we are finished
	if (flag)
	{
	    String fn = conv.getOutputFile();
	    cache.removeRunner(fn);
	    if (! exitSuccess())
	    {
		(new File(fn)).delete();
	    }
	}


	return flag;
    }

    /**
     * A wrapper for Converter.exitSuccess().
     * @return true if converter exit success
     * @see org.scilla.converter.Converter#exitSuccess()
     */
    public boolean exitSuccess ()
    {
	return conv.exitSuccess();
    }

    /**
     * A wrapper for Converter.getErrorMessage().
     * @return conversion error message
     * @see org.scilla.converter.Converter#getErrorMessage()
     */
    public String getErrorMessage()
    {
	return conv.getErrorMessage();
    }

    /**
     * Change result file location.
     * Don't call this method after getStream().
     * TODO: illegalstate exception
     * @param fname output filename
     * @see #write(java.io.OutputStream)
     */
    public void setOutputFile (String fname) { conv.setOutputFile(fname); }

    /**
     * @return -1 because file length currently unknown
     */
    public long getLength () { return -1; }

    public InputStream getStream ()
    throws ScillaException
    {
	String filename = conv.getOutputFile();
	return new MediaStream(filename, this);
    }
}
