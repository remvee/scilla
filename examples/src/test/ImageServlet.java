package test;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.scilla.*;
import org.scilla.core.*;
import org.scilla.util.*;

public class ImageServlet extends HttpServlet {
    private static final Log log = LogFactory.getLog(ImageServlet.class);
    private static final int BUFFER_SIZE = 4096;

    public void doGet (HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        Request req = null;
        try {
            req = createFromHttpServletRequest(request);

            long len = req.getLength();
            response.setContentType(req.getOutputType());

            // get streams
            InputStream in = req.getStream();
            OutputStream out = response.getOutputStream();

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
	} catch (Throwable ex) {
	    throw new ServletException("request failed", ex);
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
            long t = (new File(req.getOutputFile())).lastModified();

            if (log.isDebugEnabled()) {
                log.debug("getLastModified="+(new Date(t)));
	    }

            return t;
        } catch (Exception ex) {
            if (log.isDebugEnabled()) {
                log.debug("ignoring", ex);
	    }
        }

        return 0L;
    }

    private Request createFromHttpServletRequest (HttpServletRequest req)
    throws Exception {
        // source file
        String source = req.getPathInfo();
	log.debug("source="+source);
        if (source == null || !source.startsWith("/") || ("/"+source).indexOf("/../") != -1) {
            throw new ScillaIllegalRequestException();
        }

        // source mime type
        String type = MimeType.getTypeFromFilename(source);
        if (type == null) {
            throw new ScillaException("unknow input type");
        }

        // conversion parameters from QUERY_STRING
        Vector pars = new Vector();
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

	URL url = getServletConfig().getServletContext().getResource(source);
	log.debug("url="+url);

        return new Request(url, type, pars);
    }
}
