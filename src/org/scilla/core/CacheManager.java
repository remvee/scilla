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
import java.util.Map;

import org.scilla.*;
import org.scilla.util.*;

/**
 * The CacheManager serves cached or fresh objects.  If the requested
 * object is not available in cache, a new conversion will be started.
 *
 * @version $Revision: 1.21 $
 * @author R.W. van 't Veer
 */
public class CacheManager implements RunnerChangeListener
{
    private static final Logger log = LoggerFactory.get(CacheManager.class);
    private static final Config config = ConfigProvider.get();

    private static CacheManager _instance = null;

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
     * Get object from cache.  The CacheManager will lookup a
     * equivalent object in cache.  When no cached object is available
     * an new conversion will be started.
     * @param req request info
     * @return requested object
     * @throws ScillaException when object creation failed
     */
    public MediaObject get (Request req)
    throws ScillaException 
    {
	MediaObject obj = null;

	String ofn = getCacheFilename(req);
	File ifile = new File(req.getInputFile());
	File ofile = new File(ofn);

	// does source really exist
	if (!ifile.exists()) {
	    throw new ScillaNoInputException();
	}
	// conversion still running?
	obj = (RunnerObject) runners.get(ofn);
	if (obj != null) {
	    log.debug("existing runner: "+obj);
	    obj = new CachedObject(ofn, (RunnerObject) obj);
	    log.debug("get="+obj);
	    return obj;
	}
	// output in cache and source not newer than cache
	if (ofile.exists() && ifile.lastModified() < ofile.lastModified()) {
	    obj = new CachedObject(ofn);
	    log.debug("get="+obj);
	    return obj;
	}
	// create new MediaObject
	obj = MediaFactory.createObject(req, ofn);
	if (obj instanceof RunnerObject) {
	    RunnerObject ro = (RunnerObject) obj;

	    // make sure output file can be writen
	    ensureCacheDirectoryFor(ofn);

	    // register change listener
	    runners.put(ofn, ro);
	    ro.addRunnerChangeListener(this);

	    // add runner to list and start converter
	    ro.start();

	    log.debug("get="+obj);
	    return obj;
	}

	log.error("get: could get proper media object");
	return null;
    }

    public void runnerChange (RunnerObject ro, int code) {
	log.debug("runnerChange: "+ro+", "+code);
	if (code == RUNNER_FINISHED) {
	    runners.remove(ro.getOutputFile());
	}
    }
    private Map runners = new Hashtable();

    public final static String MAX_FN_LEN_KEY = "cache.filenamelen.max";
    public static int maxFilenameLen = 32;

    // try to set max filename len from config
    static
    {
	if (config.exists(MAX_FN_LEN_KEY))
	{
	    try
	    {
		maxFilenameLen = config.getInt(MAX_FN_LEN_KEY);
	    }
	    catch (NumberFormatException e)
	    {
		log.error(e);
	    }
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

	// avoid filename/ directory clash
	if (result.length() % (maxFilenameLen) == 0) result.append('x');

	// chopup, making directories using maxFilenameLen
	data = result.toString().toCharArray();
	encoded = new StringBuffer();
	for (int i = 0; i < data.length; i++)
	{
	    if (i % (maxFilenameLen) == 0) encoded.append(File.separator);
	    encoded.append(data[i]);
	}
	result = encoded;

	// append suffix to fool simple OS converters
	String str = result.toString();
	String fn = str.substring(str.lastIndexOf(File.separator));
	String suffix = MimeType.getExtensionForType(req.getOutputType());
	if (fn.length() + suffix.length() + 1 > maxFilenameLen)
	{
	    // insert dummy data
	    for (int i = fn.length(); i < maxFilenameLen; i++)
	    {
		result.append('x');
	    }
	    result.append(File.separator);
	    result.append('x');
	}
	result.append('.');
	result.append(suffix);

	// prepend cache path
	fn = config.getString(Config.CACHE_DIR_KEY)
		+ File.separator + result;

	if (log.isDebugEnabled()) log.debug("getCacheFilename="+fn);
	return fn;
    }

    void ensureCacheDirectoryFor (String fn)
    {
	String path = fn.substring(0, fn.lastIndexOf(File.separator));
	(new File(path)).mkdirs();
    }
}
