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
import java.util.Date;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Category;

import org.scilla.*;
import org.scilla.util.mp3.*;

/**
 * This servlet handles media requests.
 *
 * @version $Revision: 1.7 $
 * @author R.W. van 't Veer
 */
public class Servlet extends HttpServlet
{
    static Category log = Category.getInstance(Servlet.class);

    static { BasicConfigurator.configure(); }

    void addStreamHeaders (Request req, HttpServletResponse response)
    {
	try
	{
	    ID3v1 id3 = new ID3v1(new File(req.getInputFile()));
	    String title = "";
	    if (id3.getArtist() != null && id3.getArtist().length() != 0)
	    {
		title += id3.getArtist() + " - ";
	    }
	    if (id3.getAlbum() != null && id3.getAlbum().length() != 0)
	    {
		title += id3.getAlbum() + " - ";
	    }
	    if (id3.getTitle() != null && id3.getTitle().length() != 0)
	    {
		title += id3.getTitle();
	    }
	    if (title.endsWith(" - "))
	    {
		title = title.substring(0, title.lastIndexOf(" - "));
	    }
	    if (title.length() == 0)
	    {
		title = req.getInputFile();
		title = title.substring(title.lastIndexOf(File.separator)+1);
		title = title.substring(0, title.lastIndexOf('.'));
	    }

	    response.setHeader("icy-title", title);
	    response.setHeader("x-audiocast-title", title);
	}
	catch (Throwable t) { /* nop */ }
    }

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
	    log.info("doGet: request="+req);

	    long len = req.getLength();
	    if (len != -1) response.setContentLength((int) len);
	    response.setContentType(req.getOutputType());

	    if (req.getOutputType().equals("audio/mpeg")
		    || req.getOutputType().equals("audio/mp3"))
	    {
		addStreamHeaders(req, response);
	    }

	    req.write(response.getOutputStream());
	}
	catch (ScillaNoOutputException ex)
	{
	    log.info("doGet! ", ex);
	    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
		    ex.getMessage());
	}
	catch (ScillaConversionFailedException ex)
	{
	    log.info("doGet! ", ex);
	    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
		    ex.getMessage());
	}
	catch (ScillaOutputIOException ex)
	{
	    log.debug("doGet! ", ex);
	    /* probably and broken pipe */
	}
	catch (ScillaNoInputException ex)
	{
	    log.info("doGet! ", ex);
	    response.sendError(HttpServletResponse.SC_NOT_FOUND,
		    ex.getMessage());
	}
	catch (ScillaInputIOException ex)
	{
	    log.info("doGet! ", ex);
	    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
		    ex.getMessage());
	}
	catch (ScillaNoConverterException ex)
	{
	    log.info("doGet! ", ex);
	    response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,
		    ex.getMessage());
	}
	catch (ScillaIllegalRequestException ex)
	{
	    log.info("doGet! ", ex);
	    response.sendError(HttpServletResponse.SC_FORBIDDEN,
		    ex.getMessage());
	}
	catch (ScillaException ex)
	{
	    log.warn("doGet! ", ex);
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
	    long t = req.lastModified();

	    if (log.isDebugEnabled())
		log.debug("getLastModified="+(new Date(t)));
	    return req.lastModified();
	}
	catch (ScillaException ex)
	{
	    // ignore
	}

	return 0L;
    }
}
