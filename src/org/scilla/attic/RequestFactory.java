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
import java.net.URLDecoder;
import java.util.List;
import java.util.Vector;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.scilla.util.*;

/**
 * The request factory class creates a request object from a different
 * kind of request.
 *
 * @author R.W. van 't Veer
 * @version $Revision: 1.10 $
 */
public class RequestFactory {
    /** logger */
    private static final Log log = LogFactory.getLog(RequestFactory.class);

    /** scilla configuration object */
    private final static Config config = ConfigProvider.get();

    /**
     * Interpret HTTP request to create a scilla request.
     * @param req HTTP request
     * @throws ScillaIllegalRequestException when request can not be
     * created
     * @return scilla request
     */
    public static Request createFromHttpServletRequest (HttpServletRequest req)
    throws ScillaException {
        // source file
        String source = config.getString(Config.SOURCE_DIR_KEY) +
		File.separator + req.getPathInfo();
        if (source.indexOf("/../") != -1) {
            throw new ScillaIllegalRequestException();
        }
	String sourceType = null;

	// does source file carry output suffix?
        List pars = new Vector();
	if (! (new File(source)).exists()) {

	    // result mime type
	    String outType = MimeType.getExtensionFromFilename(source);
	    pars.add(new RequestParameter("outputtype", outType));
	    log.debug("outputtype from source filename: "+outType);

	    // make sure source exists
	    source = stripSuffix(source);
	    log.debug("source filename: "+source);
	    if (! (new File(source)).exists()) {
		throw new ScillaException("can't find source");
	    }
	}


        // source mime type
        String type = MimeType.getTypeFromFilename(source);
        if (type == null) {
            throw new ScillaException("unknow input type");
        }

        // conversion parameters from QUERY_STRING
        String qs = req.getQueryString();
        if (qs != null) {
            StringTokenizer st = new StringTokenizer(qs, "&");
            while (st.hasMoreTokens()) {
                String t = st.nextToken();
                int i = t.indexOf('=');
                String k = URLDecoder.decode(i > 0 ? t.substring(0, i) : t);
                String v = i > 0 ? URLDecoder.decode(t.substring(i+1)) : null;
                pars.add(new RequestParameter(k, v));
            }
        }

        return new Request(source, type, pars);
    }

    private static String stripSuffix (String fname) {
	int i = fname.lastIndexOf('.');
	int j = fname.lastIndexOf(File.separator);
	return (i != -1 && i > j) ? fname.substring(0, i) : fname;
    }

    /**
     * Interpret argv request to create scilla request.
     * @param args argument list, first argument is the source object
     * and the rest of the arguments consist of param=value pairs
     * @throws ScillaIllegalRequestException when request can not be
     * created
     * @return scilla request
     */
    public static Request createFromArgv (String[] args)
    throws ScillaException {
        // source file
        String source = args[0];

        // mime type
        String type = MimeType.getTypeFromFilename(source);

        // conversion parameters
        List pars = new Vector();
        for (int i = 1; i < args.length; i++) {
            String s = args[i];
            int j = s.indexOf("=");
            String key = s.substring(0, j);
            String val = s.substring(j + 1);
            pars.add(new RequestParameter(key, val));
        }

        return new Request(source, type, pars);
    }
}
