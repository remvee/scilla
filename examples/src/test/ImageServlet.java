package test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.scilla.Request;
import org.scilla.RequestParameter;
import org.scilla.ScillaException;
import org.scilla.ScillaIllegalRequestException;
import org.scilla.util.MimeType;
import org.scilla.util.PartialContentHandler;

public class ImageServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog(ImageServlet.class);

    private static final int BUFFER_SIZE = 4096;

    public static final String APPLICATION_CTX = "/r/";

    public static final String SCILLA_SOURCE_CTX = "/s/";

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Request req = null;
        try {
            req = createFromHttpServletRequest(request);

            // get stream
            long len = req.getLength();
            InputStream in = req.getStream();

            // write data
            try {
                PartialContentHandler.process(request, response, in, len);
            } finally {
                in.close();
            }
        } catch (Throwable ex) {
            throw new ServletException("request failed", ex);
        }
    }

    /**
     * Lookup last modified time
     * 
     * @param request
     *            HTTP request
     * @return last modification time in millis
     */
    public long getLastModified(HttpServletRequest request) {
        Request req = null;
        try {
            req = createFromHttpServletRequest(request);
            long t = (new File(req.getOutputFile())).lastModified();

            if (log.isDebugEnabled()) {
                log.debug("getLastModified=" + (new Date(t)));
            }

            return t;
        } catch (Exception ex) {
            if (log.isDebugEnabled()) {
                log.debug("ignoring", ex);
            }
        }

        return 0L;
    }

    private Request createFromHttpServletRequest(HttpServletRequest request)
            throws Exception {
        // does pathinfo contain a valid source
        String source = request.getPathInfo();
        log.debug("source=" + source);
        if (source == null || !source.startsWith("/")
                || ("/" + source).indexOf("/../") != -1) { throw new ScillaIllegalRequestException(); }

        // source mime type
        String type = MimeType.getTypeFromFilename(source);
        if (type == null) { throw new ScillaException("unknow input type"); }

        // conversion parameters from QUERY_STRING
        List pars = new Vector();
        String qs = request.getQueryString();
        if (qs != null) {
            StringTokenizer st = new StringTokenizer(qs, "&");
            while (st.hasMoreTokens()) {
                String t = st.nextToken();
                int i = t.indexOf('=');
                String k = URLDecoder.decode(i > 0 ? t.substring(0, i) : t);
                String v = i > 0 ? URLDecoder.decode(t.substring(i + 1)) : null;
                pars.add(new RequestParameter(k, v));
            }
        }

        Request req = null;
        if (source.startsWith(APPLICATION_CTX)) {
            String fname = source.substring(APPLICATION_CTX.length() - 1);
            URL url = getServletConfig().getServletContext().getResource(fname);
            log.debug("url=" + url);
            req = new Request(url, type, pars);
        } else if (source.startsWith(SCILLA_SOURCE_CTX)) {
            String fname = AppConfig.getSourceDir();
            fname += source.substring(SCILLA_SOURCE_CTX.length() - 1);
            fname = (new File(fname)).getCanonicalPath(); // uniq location for
                                                          // this file
            log.debug("fname=" + fname);
            req = new Request(fname, type, pars);
        } else {
            // no context no request..
            throw new ScillaIllegalRequestException();
        }

        log.debug("request=" + req);
        return req;
    }
}