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
import java.util.Iterator;
import java.util.Hashtable;

import org.scilla.*;

/**
 * The CacheManager serves cached or fresh objects.  If the requested
 * object is not available in cache, a new conversion will be started.
 *
 * @version $Revision: 1.5 $
 * @author R.W. van 't Veer
 */
public class CacheManager
{
    private static CacheManager _instance = null;
    Hashtable runners = new Hashtable();

    protected CacheManager ()
    {
        // launch cleanup daemon
    }

    /**
     * @return CacheManager for this scilla instance
     */
    public static synchronized CacheManager getInstance ()
    {
        if (_instance == null) _instance = new CacheManager();
        return _instance;
    }

    /**
     * Query runner list.
     * @param filename the name of the outputfile
     * @return RunnerObject for this outputfile or null if no runner
     * available.
     * @see org.scilla.core.CachedObject
     */
    RunnerObject getRunner (String filename)
    {
	return (RunnerObject) runners.get(filename);
    }

    /**
     * Insert into runner list.
     * @param filename the name of the outputfile
     * @param obj the runner object
     * @see org.scilla.core.CachingObject
     */
    void addRunner (String filename, RunnerObject obj)
    {
	runners.put(filename, obj);
    }

    /**
     * Delete from runner list.
     * @param filename the name of the outputfile
     * @see org.scilla.core.CachingObject
     */
    void removeRunner (String filename)
    {
	runners.remove(filename);
    }

    /**
     * Get object from cache.  The CacheManager will lookup a
     * equivalent object in cache.  When no cached object is available
     * an new conversion will be started.
     * @param req request info
     * @return requested object
     * @throws ScillaException when object creation failed
     */
    public synchronized MediaObject get (Request req)
    throws ScillaException 
    {
{
    java.util.Enumeration e = runners.keys();
    System.err.println("<runners");
    while (e.hasMoreElements())
    {
	System.err.println("  "+(String)e.nextElement());
    }
    System.err.println("runners>");
}
	MediaObject obj;

	String infilename = req.getInputFile();
	String outfilename = getCacheFilename(req);
	File infile = new File(infilename);
	File outfile = new File(outfilename);

	// don't cache this object
	if (! req.allowCaching())
	{
	    return MediaFactory.createObject(req);
	}
	// already have runner
	if (getRunner(outfilename) != null)
	{
	    return new CachedObject(outfilename);
	}
	// source exists, output in cache and source not newer than cache
	if (infile.exists() && outfile.exists()
		&& infile.lastModified() < outfile.lastModified())
	{
	    return new CachedObject(outfilename);
	}

	// create new MediaObject
	obj = MediaFactory.createObject(req);
	if (obj.allowCaching())
	{
	    RunnerObject runner = (RunnerObject) obj;

	    // configure for caching
	    ensureCacheDirectoryFor(outfilename);
	    runner.setOutputFile(outfilename);
	    runner.setDeleteOutput(false);

	    // start converter
	    runner.start();
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
