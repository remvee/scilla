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

import java.util.Vector;
import java.util.Iterator;
import java.io.OutputStream;
import java.io.File;

import org.scilla.core.*;
import org.scilla.util.*;

public class Request
{
    public final static String OUTPUT_TYPE_PROPERTY = "outputtype";

    String source = null;
    String type = null;
    Vector param = null;

    MediaObject obj = null;
    CacheManager cache = CacheManager.getInstance();

    /**
     * Construct a new request object.
     * @param source	media source identifier
     * @param type	source mime-type
     * @param param	conversion parameters
     */
    public Request (String source, String type, Vector param)
    {
        this.source = source;
        this.type = type;
        this.param = param;
    }

    public void write (OutputStream out)
    throws ScillaException
    {
        obj = cache.get(this);
        obj.write(out);
    }

    public String getSource ()
    {
	return source;
    }
    public String getInputFile ()
    {
        return Config.getSourceDir() + File.separator + source;
    }
    public String getInputType () { return type; }
    public String getOutputType ()
    {
	String typePar = null;

	for (Iterator it = param.iterator(); it.hasNext(); )
	{
	    RequestParameter rp = (RequestParameter) it.next();
	    if (OUTPUT_TYPE_PROPERTY.equals(rp.key))
	    {
		typePar = rp.val;
		break;
	    }
	}

	return (typePar != null)
		?  MimeTypeFactory.getTypeFromFileExtension(typePar)
	    	: type;
    }
    public long lastModified ()
    {
	return (new File(getInputFile())).lastModified();
    }
    public Vector getParameters () { return param; }
    public boolean needConverter () { return ! param.isEmpty(); }
    public boolean allowCaching () { return true; }

    public String toString ()
    {
	StringBuffer b = new StringBuffer();
	b.append("source=" + source + "\n");
	for (Iterator it = param.iterator(); it.hasNext(); )
	{
	    RequestParameter rp = (RequestParameter) it.next();
	    b.append(rp.key + "=" + rp.val + "\n");
	}
	return b.toString();
    }

    public String toHTML ()
    {
	StringBuffer b = new StringBuffer("<DL>");
	b.append("<DT>source<DD>" + source);
	for (Iterator it = param.iterator(); it.hasNext(); )
	{
	    RequestParameter rp = (RequestParameter) it.next();
	    b.append("<DT>" + rp.key + "<DD>" + rp.val);
	}
	b.append("</DL>");
	return b.toString();
    }
}
