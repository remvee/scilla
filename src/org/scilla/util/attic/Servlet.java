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

package org.scilla.util;

import java.io.File;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.scilla.*;

/**
 * This servlet handles media requests.
 *
 * @version $Revision: 1.5 $
 * @author R.W. van 't Veer
 */
public class Servlet extends HttpServlet
{
    /**
     * Handle media request via servlet interface.
     * @param request HTTP request
     * @param response HTTP responce
     */
    public void doGet (HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
	Request req = null;
	try
	{
	    req = RequestFactory.createFromHttpServletRequest(request);

	    long len = req.getLength();
	    if (len != -1) response.setContentLength((int) len);
	    response.setContentType(req.getOutputType());

	    req.write(response.getOutputStream());
	}
	catch (ScillaNoOutputException ex)
	{
	    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
		    ex.getMessage());
	}
	catch (ScillaConversionFailedException ex)
	{
	    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
		    ex.getMessage());
	}
	catch (ScillaOutputIOException ex)
	{
	    /* probably and broken pipe */
	}
	catch (ScillaNoInputException ex)
	{
	    response.sendError(HttpServletResponse.SC_NOT_FOUND,
		    ex.getMessage());
	}
	catch (ScillaInputIOException ex)
	{
	    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
		    ex.getMessage());
	}
	catch (ScillaNoConverterException ex)
	{
	    response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,
		    ex.getMessage());
	}
	catch (ScillaIllegalRequestException ex)
	{
	    response.sendError(HttpServletResponse.SC_FORBIDDEN,
		    ex.getMessage());
	}
	catch (ScillaException ex)
	{
	    throw new ServletException("Scilla FAILED!", ex);
	}
    }

    /**
     * Lookup last modified time
     * @param request HTTP request
     * @return last modification time in millis
     */
    public long getLastModified (HttpServletRequest request)
    {
	Request req = null;
	try
	{
	    req = RequestFactory.createFromHttpServletRequest(request);
	    return req.lastModified();
	}
	catch (ScillaException ex)
	{
	    // ignore
	}

	return 0L;
    }
}
