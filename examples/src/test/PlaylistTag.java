package test;

import java.net.URLEncoder;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.scilla.info.AudioInfo;

/**
 * The playlist tag puts an url to a playlist servlet into an JSP
 * attributes.
 *
 * @author R.W. van 't Veer
 * @version $Revision: 1.8 $
 */
public class PlaylistTag extends TagSupport {
    /** logger */
    private static final Log log = LogFactory.getLog(PlaylistTag.class);

    /** default playlist type */
    public final static String LIST_TYPE_DEFAULT = "m3u";
    /** JSP attribute (any scope) to try and get playlist type from */
    public final static String LIST_TYPE_ATTRIBUTE = "playlistListType";
    /** default stream type */
    public final static String STREAM_TYPE_DEFAULT = "ogg";
    /** JSP attribute (any scope) to try and get stream type from */
    public final static String STREAM_TYPE_ATTRIBUTE = "playlistStreamType";
    /** basename of the playlist servlet; without ".m3u" or ".pls" */
    public final static String PLAYLIST_SERVLET_BASE = "playlist";

    public int doStartTag ()
    throws JspException {
	StringBuffer out = new StringBuffer();

	try {
	    // select servlet
	    out.append(PLAYLIST_SERVLET_BASE);
	    out.append('.');
	    if (listtype != null) {
		out.append(listtype);
	    } else {
		String t = (String) pageContext.findAttribute(LIST_TYPE_ATTRIBUTE);
		if (t != null) {
		    out.append(t);
		} else {
		    out.append(LIST_TYPE_DEFAULT);
		}
	    }

	    // handle location
	    out.append('?');
	    out.append(PlaylistServlet.PATH_PARAM);
	    out.append('=');
	    Object obj = pageContext.findAttribute(name);
	    if (obj instanceof DirectoryBean) {
		String fname = ((DirectoryBean) obj).getPath();
		out.append(URLEncoder.encode(fname));
	    } else if (obj instanceof AudioInfo) {
		String fname = ((AudioInfo) obj).getString(DirectoryBean.LOCATION_KEY);
		out.append(URLEncoder.encode(fname));
	    } else {
		throw new Exception("oeps, unknown stream");
	    }

	    // stream type
	    out.append('&');
	    out.append(PlaylistServlet.OUTPUT_TYPE_PARAM);
	    out.append('=');
	    if (streamtype != null) {
		out.append(streamtype);
	    } else {
		String t = (String) pageContext.findAttribute(STREAM_TYPE_ATTRIBUTE);
		if (t != null) {
		    out.append(t);
		} else {
		    out.append(STREAM_TYPE_DEFAULT);
		}
	    }

	    // handle recursion
	    if (recursive) {
		out.append('&');
		out.append(PlaylistServlet.RECURS_PARAM);
		out.append("=1");
	    }

	    // encode session
	    String url = out.toString();
	    url = ((HttpServletResponse)pageContext.getResponse()).encodeURL(url);

	    // place var attribute in page context
	    pageContext.setAttribute(var, url, getScopeInt());

	} catch (Exception ex) {
	    log.error("failed to create playlist url", ex);
	    throw new JspException("failed to create playlist url", ex);
	}

        return SKIP_BODY;
    }

    /**
     * @param v name of attribute containing directory or track bean
     */
    public void setName (String v) {
	name = v;
    }
    private String name;

    /**
     * @param v name of attribute to put url in
     */
    public void setVar (String v) {
	var = v;
    }
    private String var;

    /**
     * @param v recursive option, set to <tt>true</tt> of recursion
     */
    public void setRecursive (String v) {
	recursive = (new Boolean(v)).booleanValue();
    }
    private boolean recursive = false;

    /**
     * @param v type of playlist, i.e. <tt>m3u</tt> or <tt>pls</tt>
     */
    public void setListtype (String v) {
	listtype = v;
    }
    private String listtype = null;

    /**
     * @param v type of stream, i.e. <tt>ogg</tt> or <tt>mp3</tt>
     */
    public void setStreamtype (String v) {
	streamtype = v;
    }
    private String streamtype = STREAM_TYPE_DEFAULT;

    /**
     * @param v JSP scope to put <tt>var</tt> attribute in
     */
    public void setScope (String v) {
	scope = v;
    }
    private String scope;

    /**
     * @return JSP scope as an integer
     * @see javax.servlet.jsp.PageContext
     * @throws JspException when scope not a valid value
     */
    private int getScopeInt ()
    throws JspException {
	if (scope == null || "page".equalsIgnoreCase(scope)) {
	    return PageContext.PAGE_SCOPE;
	} else if ("request".equalsIgnoreCase(scope)) {
	    return PageContext.REQUEST_SCOPE;
	} else if ("session".equalsIgnoreCase(scope)) {
	    return PageContext.SESSION_SCOPE;
	} else if ("application".equalsIgnoreCase(scope)) {
	    return PageContext.APPLICATION_SCOPE;
	}

	throw new JspException("unexpected value for scope attribute: " + scope);
    }
}
