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

package test;

import java.io.*;
import java.util.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.scilla.*;
import org.scilla.info.*;
import org.scilla.util.*;

/**
 * This servlet stream requests.
 *
 * @version $Revision: 1.4 $
 * @author R.W. van 't Veer
 */
public class StreamServlet extends HttpServlet {
    private static final Log log = LogFactory.getLog(StreamServlet.class);

    private static final int BUFFER_SIZE = 4096;

    // TODO make configurable!
    private int oggBitrate = 50;
    // TODO make configurable!
    private int mp3Samplerate = 16;
    // TODO make configurable!
    private int mp3MaxBitrate = 56;

    /**
     * Handle stream request via servlet interface.
     * @param request HTTP request
     * @param response HTTP responce
     */
    public void doGet (HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        Request req = null;
        try {
            req = createFromHttpServletRequest(request);
            log.info("doGet ["+request.getRemoteAddr()+"]: request="+req);

	    // debugging info
	    if (log.isDebugEnabled()) {
		logHeaders(request);
	    }

	    // TODO hack around ugly ogg vorbis mime-type
	    String outputType = req.getOutputType();
	    if (outputType.equals("audio/ogg-vorbis")) {
		outputType = "application/x-ogg";
	    }

	    // headers
            response.setContentType(outputType);
            if (outputType.startsWith("audio/")) {
                addStreamHeaders(req, response);
            }

            // get stream
            long len = req.getLength();
            InputStream in = req.getStream();

	    // winamp vorbis hack, winamp can't handle ogg
	    // streaming with unknown length..
	    if (req.getOutputType().equals("audio/ogg-vorbis") && len == -1L) {
		log.debug("unknown length vorbis");
		String t = request.getHeader("user-agent");
		if (t != null && t.toLowerCase().indexOf("winamp") != -1) {
		    log.debug("hack for winamp");
		    response.setContentLength(estimatedLength(req));
		}
	    }

            // write data
	    try {
		PartialContentHandler.process(request, response, in, len);
	    } finally {
		in.close();
	    }
        } catch (ScillaNoOutputException ex) {
            log.info("doGet! ", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                               ex.getMessage());
        } catch (ScillaConversionFailedException ex) {
            log.info("doGet! ", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                               ex.getMessage());
        } catch (ScillaOutputIOException ex) {
            log.debug("doGet! ", ex);
            /* probably and broken pipe */
        } catch (ScillaNoInputException ex) {
            log.info("doGet! ", ex);
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                               ex.getMessage());
        } catch (ScillaInputIOException ex) {
            log.info("doGet! ", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                               ex.getMessage());
        } catch (ScillaNoConverterException ex) {
            log.info("doGet! ", ex);
            response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,
                               ex.getMessage());
        } catch (ScillaIllegalRequestException ex) {
            log.info("doGet! ", ex);
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                               ex.getMessage());
        } catch (ScillaException ex) {
            log.warn("doGet! ", ex);
            throw new ServletException("Scilla FAILED!", ex);
        } catch (Exception ex) {
            log.warn("doGet! ", ex);
            throw new ServletException(ex);
        }
    }

    /**
     * Lookup last modified time
     * @param request HTTP request
     * @return last modification time in millis
     */
    public long getLastModified (HttpServletRequest request) {
        Request req = null;
        try {
            req = createFromHttpServletRequest(request);
            long t = req.lastModified();

            if (log.isDebugEnabled()) {
                log.debug("getLastModified="+(new Date(t)));
	    }
            return req.lastModified();
        } catch (Exception ex) {
            // ignore
        }

        return 0L;
    }

    /**
     * Add audio info to stream headers using "<TT>icy-name</TT>" and
     * "<TT>x-audiocast-name</TT>".
     * @param req scilla request object
     * @param response servlet response object
     */
    private void addStreamHeaders (Request req, HttpServletResponse response) {
        try {
	    AudioInfo info = (AudioInfo) InfoFactory.get(req.getInputFile());
            String title = "";
            if (info.getArtist() != null && info.getArtist().length() != 0) {
                title += info.getArtist() + " - ";
            }
            if (info.getPerformer() != null && info.getPerformer().length() != 0) {
                title += info.getPerformer() + " - ";
            }
            if (info.getAlbum() != null && info.getAlbum().length() != 0) {
                title += info.getAlbum() + " - ";
            }
            if (info.getTitle() != null && info.getTitle().length() != 0) {
                title += info.getTitle();
            }
            if (title.endsWith(" - ")) {
                title = title.substring(0, title.lastIndexOf(" - "));
            }
            if (title.length() == 0) {
                title = req.getInputFile();
                title = title.substring(title.lastIndexOf(File.separator)+1);
                title = title.substring(0, title.lastIndexOf('.'));
            }

            response.setHeader("icy-name", title);
            response.setHeader("x-audiocast-name", title);

            log.debug("addStreamHeaders: "+title);
        } catch (Throwable t) {
            log.warn("addStreamHeaders", t);
        }
    }

    private Request createFromHttpServletRequest (HttpServletRequest req)
    throws Exception {
        // source file
        String source = AppConfig.getSourceDir() + File.separator + req.getPathInfo();
        if (source.indexOf("/../") != -1) {
            throw new ScillaIllegalRequestException();
        }

	// does source file carry output suffix?
	String outType = MimeType.getExtensionFromFilename(source);
        List pars = new Vector();
	if (! (new File(source)).exists()) {

	    // result mime type
	    outType = MimeType.getExtensionFromFilename(source);
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

	// conversion parameters
	if (outType.equals("ogg")) {
	    pars.add(new RequestParameter("bitrate", ""+oggBitrate));
	} else if (outType.equals("mp3")) {
	    pars.add(new RequestParameter("resample", ""+mp3Samplerate));
	    pars.add(new RequestParameter("maxbitrate", ""+mp3MaxBitrate));
	}

        return new Request(source, type, pars);
    }

    private static String stripSuffix (String fname) {
	int i = fname.lastIndexOf('.');
	int j = fname.lastIndexOf(File.separator);
	return (i != -1 && i > j) ? fname.substring(0, i) : fname;
    }

    private int estimatedLength (Request req) {
	try {
	    AudioInfo info = (AudioInfo) InfoFactory.get(req.getInputFile());
	    int len = (oggBitrate * 1000 * (info.getLength() + 1)) / 8;
	    log.debug("estimatedLength="+len);
	    return len;
	} catch (Throwable ex) {
	    log.debug("estimatedLength failed", ex);
	    // ignore
	}
	return -1;
    }

    private void logHeaders (HttpServletRequest request) {
	for (Enumeration en = request.getHeaderNames(); en.hasMoreElements();) {
	    String key = (String) en.nextElement();
	    String val = request.getHeader(key);
	    log.debug("header: "+key+"="+val);
	}
    }
}
