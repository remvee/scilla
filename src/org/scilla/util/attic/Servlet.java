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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.scilla.*;
import org.scilla.info.*;

/**
 * This servlet handles media requests.
 *
 * @version $Revision: 1.22 $
 * @author R.W. van 't Veer
 */
public class Servlet extends HttpServlet {
    private static final Log log = LogFactory.getLog(Servlet.class);

    private static final int BUFFER_SIZE = 4096;

    /**
     * Initialize scilla.
     */
    public void init (ServletConfig config)
    throws ServletException {
        Config scillaConfig = ConfigProvider.get();
        Enumeration en = config.getInitParameterNames();
        while (en.hasMoreElements()) {
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
    throws ServletException, IOException {
        Request req = null;
        try {
            req = RequestFactory.createFromHttpServletRequest(request);
            log.info("doGet ["+request.getRemoteAddr()+"]: request="+req);

            long len = req.getLength();
            response.setContentType(req.getOutputType());

            if (req.getOutputType().startsWith("audio/")) {
                addStreamHeaders(req, response);
            }

            // get streams
            InputStream in = req.getStream();
            OutputStream out = response.getOutputStream();

            // handle range requests
            PartialContent pc = new PartialContent(request, response, len);
            if (pc.isPartial) {
                long offset = pc.offset;
                long endpoint = pc.endpoint;

                if (offset > 0) {
                    in.skip(offset);
		}

                int n;
                byte[] b = new byte[BUFFER_SIZE];
                try {
                    while ((n = in.read(b)) != -1) {
                        if (endpoint != -1) {
                            if (offset + n > endpoint) {
                                n = (int) (endpoint - offset) + 1;
                                if (n > 0) {
                                    out.write(b, 0, n);
				}
                                break;
                            }
                            offset += n;
                        }
                        out.write(b, 0, n);
                    }
                } finally {
                    in.close();
                }
            } else {
		// write all content to client
                int n;
                byte[] b = new byte[BUFFER_SIZE];
                try {
                    while ((n = in.read(b)) != -1) {
                        out.write(b, 0, n);
		    }
                } finally {
                    in.close();
                }
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
            req = RequestFactory.createFromHttpServletRequest(request);
            long t = req.lastModified();

            if (log.isDebugEnabled()) {
                log.debug("getLastModified="+(new Date(t)));
	    }
            return req.lastModified();
        } catch (ScillaException ex) {
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
    void addStreamHeaders (Request req, HttpServletResponse response) {
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

    /**
     * Helper class to interpet HTTP servlet partial content requests.
     */
    public class PartialContent {
        /** byte offset to begin writing */
        public long offset = 0;
        /** byte endpoint inclusive to stop writing */
        public long endpoint = -1;
        /** <TT>true</TT> when requested for parital content and length if known */
        public boolean isPartial = false;

        /** name of range request header */
        public static final String RANGE_HEADER = "range";
        /** byte range spec */
        public static final String BYTE_RANGE = "bytes=";
        /** name of range response header */
        public static final String CONTENT_RANGE_HEADER = "Content-Range";


        /**
         * Prepare a possible partial content request.  If
         * <TT>len</TT> is not -1, try to get a range spec from
         * range header, modify response accordingly and set
         * public fields {@link #offset}, {@link #endpoint} and
         * {@link #isPartial}.  Otherwise keep {@link #isPartial}
         * is <TT>false</TT>.
         * @param request HTTP servlet request object
         * @param response HTTP servlet response object
         * @param len length of the requested content or -1 if
         * length unknown.
         */
        public PartialContent (HttpServletRequest request, HttpServletResponse response, long len) {
            // can only do partial content when length is unkwown
            if (len != -1) {
                String rangeHeader = request.getHeader(RANGE_HEADER);
                if (rangeHeader != null && rangeHeader.startsWith(BYTE_RANGE)) {
                    String byteSpec = rangeHeader.substring(BYTE_RANGE.length());
                    int sepPos = byteSpec.indexOf('-');
                    if (sepPos != -1) {
                        if (sepPos > 0) {
                            String s = byteSpec.substring(0, sepPos);
                            offset = Integer.parseInt(s);
                        }
                        if (sepPos != byteSpec.length()-1) {
                            String s = byteSpec.substring(sepPos + 1);
                            endpoint = Integer.parseInt(s);
                        } else {
                            endpoint = len - 1;
                        }

                        // notify receiver this is partial content
                        response.setStatus(response.SC_PARTIAL_CONTENT);
                        String contentRange = offset+"-"+endpoint+"/"+len;
                        response.setHeader(CONTENT_RANGE_HEADER, contentRange);
                        response.setContentLength((int)(endpoint-offset+1));
                        isPartial = true;
                    }
                } else {
		    response.setContentLength((int)len);
		}
            }
        }
    }
}
