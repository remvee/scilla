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

/**
 * The Request class holds a scilla media object request.
 *
 * @author R.W. van 't Veer
 * @version $Id: Request.java,v 1.2 2001/09/21 12:38:27 remco Exp $
 */
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

    /**
     * Write media object data to stream.
     * @param out output stream
     * @throws ScillaException when reading or writing fails
     */
    public void write (OutputStream out)
    throws ScillaException
    {
        obj = cache.get(this);
        obj.write(out);
    }

    /**
     * @return source file relative to source directory
     */
    public String getSource ()
    {
	return source;
    }

    /**
     * convenience method
     * @return full path to source file
     * @see Config#getSourceDir()
     */
    public String getInputFile ()
    {
        return Config.getSourceDir() + File.separator + source;
    }

    /**
     * @return input mime type
     */
    public String getInputType () { return type; }

    /**
     * @return output mime type
     * @see #OUTPUT_TYPE_PROPERTY
     */
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

    /**
     * @return last modified time of input file in millis
     */
    public long lastModified ()
    {
	return (new File(getInputFile())).lastModified();
    }

    /**
     * @return request paramaters
     */
    public Vector getParameters () { return param; }

    /**
     * @return true if this request needs a converter
     */
    public boolean needConverter () { return ! param.isEmpty(); }

    /**
     * @return true if this request can be cached
     */
    public boolean allowCaching () { return true; }

    /**
     * @return request description
     */
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

    /**
     * @return request description in HTML format
     */
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
