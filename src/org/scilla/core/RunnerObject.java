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

public class RunnerObject implements MediaObject
{
    final static int BUFFER_SIZE = 4096;
    final static int WAIT_FOR_FILE_TIMEOUT = 100;
    final static int WAIT_FOR_READ_TIMEOUT = 100;

    Converter conv;
    boolean deleteOutput = true;

    public RunnerObject (Converter conv)
    {
        this.conv = conv;
    }

    public boolean hasFinished ()
    {
	return conv.hasFinished();
    }

    public void write (OutputStream out)
    throws ScillaException
    {
	// start converter thread
	conv.start();

	// wait for file to appear
	String filename = conv.getOutputFile();
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

	if (deleteOutput)
	{
	    // delete file
	    f.delete();
	}
    }

    public boolean allowCaching () { return true;  /* if not live stream */ }
    public void setOutputFile (String filename) { conv.setOutputFile(filename); }
    public void setDeleteOutput (boolean flag) { deleteOutput = flag; }
}
