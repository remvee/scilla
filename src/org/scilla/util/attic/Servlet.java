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

import java.io.*;
import java.util.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.scilla.*;
import org.scilla.util.mp3.*;

/**
 * This servlet handles media requests.
 *
 * @version $Revision: 1.17 $
 * @author R.W. van 't Veer
 */
public class Servlet extends HttpServlet
{
    private static final Logger log = LoggerFactory.get(Servlet.class);

    private static final int BUFFER_SIZE = 4096;

    private static final String RANGE_HEADER = "range";
    private static final String BYTE_RANGE = "bytes=";
    private static final String CONTENT_RANGE_HEADER = "Content-Range";

    /**
     * Initialize scilla.
     */
    public void init (ServletConfig config)
    throws ServletException
    {
	Config scillaConfig = ConfigProvider.get();
	Enumeration en = config.getInitParameterNames();
	while (en.hasMoreElements())
	{
	    String key = (String) en.nextElement();
	    String val = config.getInitParameter(key);
	    scillaConfig.setString(key, val);
	}
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
	    log.info("doGet ["+request.getRemoteAddr()+"]: request="+req);

	    long len = req.getLength();
	    response.setContentType(req.getOutputType());

	    if (req.getOutputType().equals("audio/mpeg")
		    || req.getOutputType().equals("audio/mp3"))
	    {
		addStreamHeaders(req, response);
	    }

	    // handle range requests
	    long offset = 0;
	    long endpoint = -1;
	    if (len != -1)
	    {
		String rangeHeader = request.getHeader(RANGE_HEADER);
		if (rangeHeader != null && rangeHeader.startsWith(BYTE_RANGE))
		{
		    String byteSpec = rangeHeader.substring(BYTE_RANGE.length());
		    int sepPos = byteSpec.indexOf('-');
		    if (sepPos != -1)
		    {
			if (sepPos > 0)
			{
			    String s = byteSpec.substring(0, sepPos);
			    log.debug("offset: "+s);
			    offset = Integer.parseInt(s);
			}
			if (sepPos != byteSpec.length()-1)
			{
			    String s = byteSpec.substring(sepPos + 1);
			    log.debug("endpoint: "+s);
			    endpoint = Integer.parseInt(s);
			}
			else
			{
			    endpoint = len - 1;
			}

			// notify receiver this is partial content
			response.setStatus(response.SC_PARTIAL_CONTENT);
			String contentRange = offset+"-"+endpoint+"/"+len;
			response.setHeader(CONTENT_RANGE_HEADER, contentRange);
			len = endpoint-offset+1;
			log.debug("content-length: "+len);
			response.setContentLength((int)len);
		    }
		}
		else
		{
		    // no range, normal request
		    response.setContentLength((int) len);
		}
	    }

	    // write request result
	    {
		OutputStream out = response.getOutputStream();
		InputStream in = req.getStream();
		if (offset > 0) in.skip(offset);

		int n;
		int count = 0;
		byte[] b = new byte[BUFFER_SIZE];
		while ((n = in.read(b)) != -1)
		{
		    count += n;

		    // TODO test this code!
		    if (endpoint != -1)
		    {
			if (offset + n > endpoint)
			{
			    n = (int) (endpoint - offset) + 1;
			    if (n > 0) out.write(b, 0, n);
			    break;
			}
			offset += n;
		    }
		    out.write(b, 0, n);
		}
	    }
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

    /**
     * Add MP3 info to stream headers using "<TT>icy-name</TT>" and
     * "<TT>x-audiocast-name</TT>".
     * @param req scilla request object
     * @param response servlet response object
     */
    private void addStreamHeaders (Request req, HttpServletResponse response)
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

	    response.setHeader("icy-name", title);
	    response.setHeader("x-audiocast-name", title);

	    log.debug("addStreamHeaders: "+title);
	}
	catch (Throwable t)
	{
	    log.warn("addStreamHeaders", t);
	}
    }
}
