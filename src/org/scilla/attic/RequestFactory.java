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

import java.net.URLDecoder;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;

import org.scilla.util.*;

public class RequestFactory
{
    public static Request createFromHttpServletRequest (HttpServletRequest req)
    throws ScillaIllegalRequestException
    {
        // source file
        String source = req.getPathInfo();
	if (("/"+source).indexOf("/../") != -1)
	{
	    throw new ScillaIllegalRequestException();
	}

        // source mime type
        String type = MimeTypeFactory.getTypeFromFilename(source);

        // conversion parameters from QUERY_STRING
        Vector pars = new Vector();
	String qs = req.getQueryString();
	if (qs != null)
	{
	    StringTokenizer st = new StringTokenizer(qs, "&");
	    while (st.hasMoreTokens())
	    {
		String t = st.nextToken();
		int i = t.indexOf('=');
		String k = URLDecoder.decode(i > 0 ? t.substring(0, i) : t);
		String v = i > 0 ? URLDecoder.decode(t.substring(i+1)) : null;
		pars.add(new RequestParameter(k, v));
	    }
	}

        return new Request(source, type, pars);
    }

    public static Request createFromArgv (String[] args)
    {
        // source file
        String source = args[0];

        // mime type
        String type = MimeTypeFactory.getTypeFromFilename(source);

        // conversion parameters
        Vector pars = new Vector();
	for (int i = 1; i < args.length; i++)
	{
	    String s = args[i];
	    int j = s.indexOf("=");
	    String key = s.substring(0, j);
	    String val = s.substring(j + 1);
	    pars.add(new RequestParameter(key, val));
	}

        return new Request(source, type, pars);
    }
}
