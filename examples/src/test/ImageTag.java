package test;

import java.io.File;
import java.util.*;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.scilla.*;
import org.scilla.util.*;
import org.scilla.info.*;

/**
 * This image tag creates an <tt>img</tt> HTML tag to an
 * optionally transformed image with the proper <tt>width</tt>
 * and <tt>height</tt> attributes set.
 * @version $Id: ImageTag.java,v 1.18 2003/04/21 12:47:08 remco Exp $
 * @author R.W. van 't Veer
 */
public class ImageTag extends BodyTagSupport {
    /** logger */
    private static final Log log = LogFactory.getLog(ImageTag.class);

    /** image mapping */
    public final static String SERVLET_MAPPING = "/img";

    public ImageTag () {
	super();
    }

    public int doStartTag ()
    throws JspException {
	clearParameters();
	return EVAL_BODY_TAG;
    }

    public int doEndTag ()
    throws JspException {
	HttpServletRequest pageRequest = (HttpServletRequest) pageContext.getRequest();

	// content negotiation..  sort of.. TODO
	preferredOutputtype = null;
	if (getOutputtype() == null) {
	    try {
		ImageInfo inputInfo = (ImageInfo) InfoFactory.get(getRequest().getInputFile());

		// images not jpeg or gif need special care
		if (! inputInfo.isJPEG() && ! inputInfo.isGIF()) {
		    String acceptHeader = pageRequest.getHeader("accept");

		    // handle PNGs
		    boolean acceptPNG = acceptHeader != null && acceptHeader.indexOf("image/png") != -1;
		    if (! acceptPNG && inputInfo.isPNG()) {
			// indexed or transparent images become gifs others jpegs
			preferredOutputtype =
				inputInfo.isIndexed() || inputInfo.hasAlphaChannel()
				? "gif" : "jpg";
		    }
		}
	    } catch (Exception ex) {
		log.warn("failed to identify input", ex);
		throw new JspException("failed to identify input", ex);
	    }
	}

	// only putting image url in var?
	if (var != null) {
	    // TODO scope??
	    try {
		pageContext.setAttribute(var, getImageUrl());
	    } catch (Exception ex) {
		throw new JspException("failed to determine image url", ex);
	    }
	    return EVAL_PAGE;
	}

	try {
	    // start build img tag
	    StringBuffer out = new StringBuffer();
	    out.append("<img");

	    out.append(" src=\"");
	    out.append(HTMLUtil.escape(getImageUrl()));
	    out.append('"');

	    // try to get and pass height and width
	    try {
		ImageInfo outputInfo = (ImageInfo) InfoFactory.get(getRequest().getOutputFile());
		if (outputInfo != null) {
		    int width = outputInfo.getWidth();
		    int height = outputInfo.getHeight();
		    if (width != -1 && height != -1) {
			out.append(" width=\"");
			out.append(width+"");
			out.append("\" height=\"");
			out.append(height+"");
			out.append('"');
		    }
		}
	    } catch (Exception ex) {
		// ignore
	    }

	    // typical image properties
	    if (getAlt() != null) {
		out.append(" alt=\""+HTMLUtil.escape(getAlt())+"\"");
	    }
	    if (getBorder() != null) {
		out.append(" border=\""+HTMLUtil.escape(getBorder())+"\"");
	    }
	    if (getStyle() != null) {
		out.append(" style=\""+HTMLUtil.escape(getStyle())+"\"");
	    }
	    if (getStyleClass() != null) {
		out.append(" class=\""+HTMLUtil.escape(getStyleClass())+"\"");
	    }

	    // close tag
	    out.append(" />");

	    // write it!
	    pageContext.getOut().print(out.toString());
	} catch (Exception ex) {
	    log.error("failed to build image tag", ex);
	    throw new JspException("failed to build image tag", ex);
	}

	return EVAL_PAGE;
    }

    public int doAfterBody ()
    throws JspException {
	bodyContent.clearBody();
        return SKIP_BODY;
    }

    public void setSrc (String src) {
	this.src = src;
	absSrc = null;
    }
    public String getSrc ()
    throws Exception {
	if (name != null) {
	    ImageInfo img = (ImageInfo) pageContext.findAttribute(name);
	    absSrc = img.getPathName();
	    String source = AppConfig.getSourceDir();
	    if (! absSrc.startsWith(source)) {
		throw new Exception("data not found");
	    }
	    absSrc = absSrc.substring(source.length());
	}

	if (absSrc != null) {
	    return absSrc;
	}

	// absolute location
	HttpServletRequest pageRequest = (HttpServletRequest) pageContext.getRequest();
	String pageLocation = pageRequest.getServletPath();
	int i = pageLocation.lastIndexOf('/');
	if (i == -1) {
	    throw new JspException("unable to determine relative location");
	}
	pageLocation = pageLocation.substring(0, i);

	// run through parent directories
	String t = src.toString();
	while (t.startsWith("../")) {
	    t = t.substring(3);
	    int j = pageLocation.lastIndexOf('/');
	    if (j == -1) {
		throw new JspException("too many ../");
	    }
	    pageLocation = pageLocation.substring(0, j);
	}

	// security check
	if (t.indexOf("..") != -1) {
	    throw new JspException("illegal location");
	}

	return absSrc = pageLocation + "/" + t;
    }
    private Object src = null;
    private String absSrc = null;

    public void setOutputtype (String v) {
	outputtype = v;
    }
    public String getOutputtype () {
	return outputtype;
    }
    private String outputtype = null;
    private String preferredOutputtype = null;

    public void setTransform (String v) {
       transform = v;
    }
    public String getTransform () {
       return transform;
    }
    private String transform = null;

    public void clearParameters () {
	requestParameters = new ArrayList();
    }
    public void addParameter (RequestParameter rp) {
	requestParameters.add(rp);
    }
    private List requestParameters = new ArrayList();

    public void setName (String v) {
	name = v;
    }
    public String getName () {
	return name;
    }
    private String name = null;

    public void setVar (String v) {
	var = v;
    }
    public String getVar () {
	return var;
    }
    private String var;

// image attributes
    public void setAlt (String v) {
	alt = v;
    }
    public String getAlt () {
	return alt;
    }
    private String alt = "";

    public void setBorder (String v) {
	border = v;
    }
    public String getBorder () {
	return border;
    }
    private String border = null;

    public void setStyle (String v) {
	style = v;
    }
    public String getStyle () {
	return style;
    }
    private String style = null;

    public void setStyleClass (String v) {
	styleClass = v;
    }
    public String getStyleClass () {
	return styleClass;
    }
    private String styleClass = null;

// private stuff
    /**
     * Create a request object from the current source and
     * transformations properties.
     */
    private Request getRequest ()
    throws Exception {
        // source mime type
        String type = null;
	ImageInfo img = null;
	if (name != null) {
	    img = (ImageInfo) pageContext.findAttribute(name);
	    type = MimeType.getTypeFromFilename(img.getPathName());
	} else {
	    type = MimeType.getTypeFromFilename(getSrc());
	}

        if (type == null) {
            throw new ScillaException("unknow input type");
        }

	// create request according to context
	Request req = null;
	if (img != null) {
	    String fname = img.getPathName();
	    req = new Request(fname, type, getRequestParameters());
	} else {
	    URL url = pageContext.getServletContext().getResource(getSrc());
	    req = new Request(url, type, getRequestParameters());
	}

	return req;
    }

    /**
     * Create an URL to the scilla servlet from the current
     * source and transformations properties.
     */
    private String getImageUrl ()
    throws Exception {
	StringBuffer out = new StringBuffer();

	HttpServletRequest pageRequest = (HttpServletRequest) pageContext.getRequest();
	HttpServletResponse pageResponse = (HttpServletResponse) pageContext.getResponse();

	out.append(pageRequest.getContextPath());
	out.append(SERVLET_MAPPING);

	out.append(name != null ? ImageServlet.SCILLA_SOURCE_CTX : ImageServlet.APPLICATION_CTX);
	// strip leading slash, already in context spec
	String fname = getSrc();
	while (fname.startsWith("/")) {
	    fname = fname.substring(1);
	}
	out.append(fname);

	// translate property to query string
	Iterator it = getRequestParameters().iterator();
	for (int i = 0; it.hasNext(); i++) {
	    RequestParameter rp = (RequestParameter) it.next();
	    out.append(i > 0 ? '&' : '?');
	    out.append(URLEncoder.encode(rp.key));
	    out.append('=');
	    out.append(URLEncoder.encode(rp.val));
	}

	return pageResponse.encodeURL(out.toString());
    }

    /**
     * Translate transform property to request parameters.
     */
    private List getRequestParameters () {
	List result = new ArrayList(requestParameters);

	if (outputtype != null) {
	    result.add(new RequestParameter(Request.OUTPUT_TYPE_PARAMETER, outputtype));
	} else if (preferredOutputtype != null) {
	    result.add(new RequestParameter(Request.OUTPUT_TYPE_PARAMETER, preferredOutputtype));
	}

	if (transform == null) {
	    return result;
	}

	StringTokenizer st = new StringTokenizer(transform, ";");
	while (st.hasMoreTokens()) {
	    String t = st.nextToken();
	    int paren = t.indexOf('(');
	    if (paren == -1) {
		result.add(new RequestParameter(t, null));

		if (log.isDebugEnabled()) {
		    log.debug(t);
		}
	    } else {
		String key = t.substring(0, paren);
		String val = t.substring(paren+1, t.length() - 1);
		result.add(new RequestParameter(key, val));

		if (log.isDebugEnabled()) {
		    log.debug(key+"="+val);
		}
	    }
	}

	return result;
    }
}
