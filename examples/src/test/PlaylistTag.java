package test;

import java.net.URLEncoder;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PlaylistTag extends TagSupport {
    /** logger */
    private static final Log log = LogFactory.getLog(PlaylistTag.class);

    public PlaylistTag () {
	super();
    }

    public int doStartTag ()
    throws JspException {
	// build playlist url
	String url = "playlist.m3u?d=";

	// handle location
	Object obj = pageContext.findAttribute(name);
	if (obj instanceof DirectoryBean) {
	    url += URLEncoder.encode(((DirectoryBean)obj).getPath());
	} else if (obj instanceof TrackBean) {
	    url += URLEncoder.encode(((TrackBean)obj).getFilename());
	} else {
	    throw new JspException("oeps, unknown stream");
	}

	// handle recursion
	if (recursive) {
	    url += "&r=1";
	}

	// encode session
	url = ((HttpServletResponse)pageContext.getResponse()).encodeURL(url);

	// place var attribute in page context
	pageContext.setAttribute(var, url, getScopeInt());

        return SKIP_BODY;
    }

    public void setName (String v) {
	name = v;
    }
    public String getName () {
	return name;
    }
    private String name;

    public void setVar (String v) {
	var = v;
    }
    public String getVar () {
	return var;
    }
    private String var;

    public void setRecursive (String v) {
	recursive = (new Boolean(v)).booleanValue();
    }
    public String getRecursive () {
	return (new Boolean(recursive)).toString();
    }
    private boolean recursive = false;;


    public void setScope (String v) {
	scope = v;
    }
    public String getScope () {
	return scope;
    }
    private String scope;

    public int getScopeInt ()
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
