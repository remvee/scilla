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

import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import java.io.InputStream;
import java.io.File;
import java.net.URL;

import org.scilla.core.*;
import org.scilla.util.*;

/**
 * The Request class holds a scilla media object request.
 *
 * @author R.W. van 't Veer
 * @version $Revision: 1.19 $
 */
public class Request {
    private final static Logger log = LoggerFactory.get(Request.class);
    private final static Config config = ConfigProvider.get();
    private final static CacheManager cache = CacheManager.getInstance();

    public final static String OUTPUT_TYPE_PARAMETER = "outputtype";

    private String source = null;
    private URL url = null;
    private String type = null;
    private List param = null;

    private MediaObject obj = null;

    /**
     * Construct a new request object.
     * @param source	media source identifier
     * @param type	source mime-type
     * @param param	conversion parameters
     * @throws ScillaException when object not retrievable
     */
    public Request (String source, String type, List param)
    throws ScillaException {
        this.source = source;
        this.type = type;
        this.param = param;
    }

    /**
     * Construct a new request object for a remote object.
     * @param url	location of media source
     * @param type	source mime-type
     * @param param	conversion parameters
     * @throws ScillaException when object not retrievable
     */
    public Request (URL url, String type, List param)
    throws ScillaException {
        this.url = url;
        this.type = type;
        this.param = param;

	// fetch remote object
	source = cache.getRemoteObjectFile(url);
    }

    /**
     * Wrapper around obj member.
     */
    private synchronized MediaObject getObject ()
    throws ScillaException {
        if (obj == null) {
            obj = cache.getMediaObject(this);
        }
        return obj;
    }

    /**
     * Get media input stream.
     * @throws ScillaException accessing media fails
     */
    public InputStream getStream ()
    throws ScillaException {
        return getObject().getStream();
    }

    /**
     * @return length of requested data or -1 if unknown
     */
    public long getLength ()
    throws ScillaException {
        return getObject().getLength();
    }

    /**
     * @return source file relative to source directory
     */
    public String getSource () {
        return source;
    }

    /**
     * convenience method
     * @return full path to source file
     * @see org.scilla.Config#SOURCE_DIR_KEY
     */
    public String getInputFile () {
	if (url == null) {
	    return config.getString(Config.SOURCE_DIR_KEY) + File.separator + source;
	}
	return source;
    }

    /**
     * Get filename of object after transformations.  Wait for
     * transformations to finish if still running.
     * @return filename of transformed object
     */
    public String getOutputFile ()
    throws ScillaException {
	return getObject().getFilename();
    }

    /**
     * @return input mime type
     */
    public String getInputType () {
        return type;
    }

    /**
     * @return output mime type
     * @see #OUTPUT_TYPE_PARAMETER
     */
    public String getOutputType () {
        String typeP = getParameter(OUTPUT_TYPE_PARAMETER);
        return typeP != null
               ? MimeType.getTypeFromFileExtension(typeP)
               : type;
    }

    /**
     * @return last modified time of input file in millis
     */
    public long lastModified () {
        return (new File(getInputFile())).lastModified();
    }

    /**
     * @return request paramaters
     */
    public List getParameters () {
        return param;
    }

    /**
     * @return request paramaters keys
     */
    public List getParameterKeys () {
        List l = new Vector();
        Iterator it = param.iterator();
        while (it.hasNext()) {
            RequestParameter rp = (RequestParameter) it.next();
            l.add(rp.key);
        }
        return l;
    }

    /**
     * @param key parameter identifier
     * @return value or null if parameter not set
     */
    public String  getParameter(String key) {
        RequestParameter rp;
        Iterator it = param.iterator();
        while (it.hasNext()) {
            rp = (RequestParameter) it.next();
            if (key.equals(rp.key)) {
                return rp.val;
	    }
        }
        return null;
    }

    /**
     * @return true if this request needs a converter
     */
    public boolean needConverter () {
        return ! param.isEmpty();
    }

    /**
     * @return request description
     */
    public String toString () {
        StringBuffer b = new StringBuffer();
        b.append("source=" + source + ", ");
        for (Iterator it = param.iterator(); it.hasNext(); ) {
            RequestParameter rp = (RequestParameter) it.next();
            b.append(rp.key + "=" + rp.val + ", ");
        }
        return b.substring(0, b.length()-2);
    }

    /**
     * @return request description in HTML format
     */
    public String toHTML () {
        StringBuffer b = new StringBuffer("<DL>");
        b.append("<DT>source<DD>" + source);
        for (Iterator it = param.iterator(); it.hasNext(); ) {
            RequestParameter rp = (RequestParameter) it.next();
            b.append("<DT>" + rp.key + "<DD>" + rp.val);
        }
        b.append("</DL>");
        return b.toString();
    }
}
