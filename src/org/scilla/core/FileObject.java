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
import java.io.FileInputStream;
import java.io.IOException;

import org.scilla.*;

public class FileObject implements MediaObject
{
    static final int BUFFER_SIZE = 4096;

    String filename;

    public FileObject (String filename)
    {
        this.filename = filename;
    }

    public void write (OutputStream out)
    throws ScillaException
    {
	FileInputStream in = null;
	try
	{
	    in = new FileInputStream(filename);

	    // write in to out
	    byte[] b = new byte[BUFFER_SIZE];
	    int n;
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
		catch (IOException e) { }
	    }
	}
    }

    public boolean allowCaching ()
    {
        return false; // since this is most likely a direct hit
    }
}
