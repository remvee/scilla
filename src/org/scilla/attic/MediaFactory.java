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

import org.scilla.core.*;
import org.scilla.converter.*;

public class MediaFactory
{
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
	if (file != null && ! req.needConverter())
	{
	    return new FileObject(req.getInputFile());
	}

        // find appropriate converter
	Converter conv = null;
	Class[] convs = Config.getInstance().getConverters();
	for (int i = 0; i < convs.length; i++)
	{
	    Converter c;
	    try
	    {
		c = (Converter) convs[i].newInstance();
	    }
	    catch (Exception ex)
	    {
		System.err.println(
			"MediaFactory.createObject: '"+convs[i]+"': "+ex);
		continue;
	    }

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

	// create runner object
	return new RunnerObject(conv);
    }
}
