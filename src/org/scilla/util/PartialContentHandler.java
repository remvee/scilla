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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handle (partial) content requests.
 *
 * @version $Revision: 1.4 $
 * @author R.W. van 't Veer
 */
public class PartialContentHandler {
    private static final Log log = LogFactory.getLog(PartialContentHandler.class);
    private static final int BUFFER_SIZE = 4096;

    /** name of range request header */
    public static final String RANGE_HEADER = "range";
    /** byte range spec */
    public static final String BYTE_RANGE = "bytes=";
    /** name of range response header */
    public static final String CONTENT_RANGE_HEADER = "Content-Range";

    /**
     * Process servlet request.  Determine if request is a
     * request for partial content and write the needed headers
     * and status back to the client and write the request bytes
     * range.  If the request is not for partial content, just
     * write all data.
     * @param request HTTP request object
     * @param response HTTP response object
     * @param in stream to read data from
     * @param len length of data to be write or <tt>-1</tt> if unknown,
     * in which case partial content request are handled as normal requests
     * @throws IOException when reading or writing fails
     */
    public static void process(HttpServletRequest request,
            HttpServletResponse response, InputStream in, long len)
            throws IOException {
        OutputStream out = response.getOutputStream();

        // can only do partial content when length is unknown
        if (len != -1) {
            String rangeHeader = request.getHeader(RANGE_HEADER);
            // was partial content requested?
            if (rangeHeader != null && rangeHeader.startsWith(BYTE_RANGE)) {
                String byteSpec = rangeHeader.substring(BYTE_RANGE.length());
                int sepPos = byteSpec.indexOf('-');
                // does the byte spec describe a range?
                if (sepPos != -1) {
                    long offset = 0;
                    long endpoint = -1;

                    // determine offset
                    if (sepPos > 0) {
                        String s = byteSpec.substring(0, sepPos).trim();
                        offset = Integer.parseInt(s);
                    }
                    // determine endpoint
                    if (sepPos != byteSpec.length() - 1) {
                        String s = byteSpec.substring(sepPos + 1).trim();
                        endpoint = Integer.parseInt(s);
                    } else {
                        endpoint = len - 1;
                    }

                    // notify receiver this is partial content
                    response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                    String contentRange = offset + "-" + endpoint + "/" + len;
                    response.setHeader(CONTENT_RANGE_HEADER, contentRange);
                    log.debug("send range header: " + CONTENT_RANGE_HEADER
                            + ": " + contentRange);
                    response.setContentLength((int) (endpoint - offset + 1));

                    // skip till offset
                    if (offset > 0) {
                        in.skip(offset);
                    }

                    // write partial data
                    int n;
                    byte[] b = new byte[BUFFER_SIZE];
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

                    // done
                    return;
                }

                log.info("didn't understand request.. treat as normal request");
                if (log.isDebugEnabled()) {
                    logHeaders(request);
                }
            }

            // inform client of data size
            response.setContentLength((int) len);
        }

        // write all content to client
        int n;
        byte[] b = new byte[BUFFER_SIZE];
        while ((n = in.read(b)) != -1) {
            out.write(b, 0, n);
        }
    }

    private static void logHeaders(HttpServletRequest request) {
        for (Enumeration en = request.getHeaderNames(); en.hasMoreElements();) {
            String key = (String) en.nextElement();
            String val = request.getHeader(key);
            log.debug("header: " + key + "=" + val);
        }
    }
}