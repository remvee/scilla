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

package org.scilla;

import java.io.File;
import java.util.Iterator;
import java.util.Hashtable;

import org.scilla.core.*;

public class CacheManager
{
    private static CacheManager _instance = null;
    Hashtable runners = new Hashtable();

    protected CacheManager ()
    {
        // launch cleanup daemon
    }

    public static synchronized CacheManager getInstance ()
    {
        if (_instance == null) _instance = new CacheManager();
        return _instance;
    }

    public RunnerObject getRunner (String filename)
    {
	return (RunnerObject) runners.get(filename);
    }

    public void addRunner (String filename, RunnerObject obj)
    {
	runners.put(filename, obj);
    }

    public void removeRunner (String filename)
    {
	runners.remove(filename);
    }

    public synchronized MediaObject get (Request req)
    throws ScillaException
    {
	MediaObject obj;

	String infilename = req.getInputFile();
	String outfilename = getCacheFilename(req);
	File infile = new File(infilename);
	File outfile = new File(outfilename);

	if (infilename != null && outfilename != null
	    // runner?
	    && getRunner(outfilename) != null
	    // source exists, output in cache and source not newer than cache
	    || infile.exists() && outfile.exists()
		&& infile.lastModified() < outfile.lastModified())
	{
	    // create cached object
	    obj = new CachedObject(outfilename);
	}
	else
	{
	    // create MediaObject
	    obj = MediaFactory.createObject(req);
	    if (req.allowCaching() && obj.allowCaching())
	    {
		// ensure existence of output directory
		ensureCacheDirectoryFor(outfilename);
		// create CachingObject
		obj = new CachingObject((RunnerObject) obj, outfilename);
	    }
	}

	return obj;
    }

    final static String MAX_FN_LEN_PROPERTY = "CacheManager.maxFileNameLen";
    static int MAX_FILENAME_LEN = 32;

    // try to set max filename len from config
    static
    {
	String s = Config.getInstance().getParameter(MAX_FN_LEN_PROPERTY);
	if (s != null) try
	{
	    MAX_FILENAME_LEN = Integer.parseInt(s);
	}
	catch (NumberFormatException e)
	{
	    e.printStackTrace();
	}
    }

    String getCacheFilename (Request req)
    {
	StringBuffer result = new StringBuffer(req.getSource());
	result.append("?");

	// append parameters to result
	for (Iterator it = req.getParameters().iterator(); it.hasNext(); )
	{
	    RequestParameter rp = (RequestParameter) it.next();
	    result.append(rp.key); result.append("="); result.append(rp.val);
	    if (it.hasNext()) result.append("&");
	}

	// encode this to a legal filename
	StringBuffer encoded = new StringBuffer();
	char[] data = result.toString().toCharArray();
	for (int i = 0; i < data.length; i++)
	{
	    if (Character.isLetterOrDigit(data[i]))
	    {
		encoded.append(data[i]);
	    }
	    else
	    {
		encoded.append("_"+(int)data[i]);
	    }
	}
	result = encoded;

	// chopup, making directories using MAX_FILENAME_LEN
	// avoid filename/ directory clash
	if (result.length() % (MAX_FILENAME_LEN) == 0) result.append('x');
	data = result.toString().toCharArray();
	encoded = new StringBuffer();
	for (int i = 0; i < data.length; i++)
	{
	    if (i % (MAX_FILENAME_LEN) == 0) encoded.append(File.separator);
	    encoded.append(data[i]);
	}
	result = encoded;


	// prepend cache path
	return Config.getCacheDir() + File.separator + result;
    }

    void ensureCacheDirectoryFor (String fn)
    {
	String path = fn.substring(0, fn.lastIndexOf(File.separator));
	(new File(path)).mkdirs();
    }
}
