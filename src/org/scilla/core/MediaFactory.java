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

import org.scilla.*;
import org.scilla.converter.*;

/**
 * The MediaFactory creates a runner or file object.
 *
 * @version $Revision: 1.7 $
 * @author R.W. van 't Veer
 */
public class MediaFactory
{
    static Logger log = LoggerFactory.getLogger(MediaFactory.class);

    static Config config = ConfigFactory.get();

    /**
     * Create a runner or file object for given request.
     * @param req media object request
     * @return runner or file media object
     */
    public static MediaObject createObject (Request req)
    throws ScillaException
    {
	// see if source exists
	String file = req.getInputFile();
	if (file != null && ! (new File(file).exists()))
	{
	    throw new ScillaNoInputException();
	}

	// its a conversionless hit 
	if (! req.needConverter())
	{
	    return new FileObject(req.getInputFile());
	}

        // find appropriate converter
	Converter conv = null;
	Class[] convs = config.getClasses(Config.CONVERTERS_KEY);
	for (int i = 0; i < convs.length; i++)
	{
	    Converter c;
	    try
	    {
		c = (Converter) convs[i].newInstance();
	    }
	    catch (Exception ex)
	    {
		log.warn("createObject: '"+convs[i]+"'", ex);
		continue;
	    }

	    if (! c.isFunctional()) continue;
	    if (! c.isValidInputType(req.getInputType())) continue;
	    if (! c.isValidOutputType(req.getOutputType())) continue;

	    // supports all parameters?
	    boolean hasAll = true;
	    Iterator it = req.getParameters().iterator();
	    while (it.hasNext())
	    {
		RequestParameter rp = (RequestParameter) it.next();
		if (!c.hasParameter(rp.key))
		{
		    hasAll = false;
		    break;
		}
	    }
	    if (hasAll)
	    {
		conv = c;
		break;
	    }
	}
	if (conv == null) throw new ScillaNoConverterException();

	// configure converter
	conv.setInputFile(req.getInputFile());
	conv.setInputType(req.getInputType());
	conv.setOutputType(req.getOutputType());
	conv.setParameters(req.getParameters());

	// log creation of converter
	if (log.isInfoEnabled())
	{
	    log.info("createObject: "+conv.getClass().getName());
	}

	// create runner object
	return new RunnerObject(conv);
    }
}
