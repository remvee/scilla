package test;

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
 */
public class ImageTag extends TagSupport {
    /** logger */
    private static final Log log = LogFactory.getLog(ImageTag.class);

    /** image mapping */
    public final static String SERVLET_MAPPING = "/img";

    public ImageTag () {
	super();
    }

    public int doStartTag ()
    throws JspException {
	HttpServletRequest pageRequest = (HttpServletRequest) pageContext.getRequest();

	// content negotiation..  sort of.. TODO
	if (getOutputType() == null) {
	    try {
		ImageInfo inputInfo = (ImageInfo) InfoFactory.get(getRequest().getOutputFile());

		// images not jpeg or gif need special care
		if (! inputInfo.isJPEG() && ! inputInfo.isGIF()) {
		    String acceptHeader = pageRequest.getHeader("accept");

		    // handle PNGs
		    boolean acceptPNG = acceptHeader != null && acceptHeader.indexOf("image/png") != -1;
		    if (! acceptPNG && inputInfo.isPNG()) {
			// indexed or transparent images become gifs others jpegs
			setOutputType(inputInfo.isIndexed() || inputInfo.hasAlphaChannel() ? "gif" : "jpg");
		    }
		}
	    } catch (Exception ex) {
		throw new JspException("failed to identify input", ex);
	    }
	}

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
	try {
            pageContext.getOut().print(out.toString());
        } catch (Exception ex) {
            throw new JspException("IO problems");
        }

        return SKIP_BODY;
    }

    public void setSrc (String src) {
	this.src = src;
	absSrc = null;
    }
    public String getSrc ()
    throws JspException {
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
	String t = src;
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
    private String src = null;
    private String absSrc = null;

    public void setTransform (String v) {
	transform = v;
    }
    public String getTransform () {
	return transform;
    }
    private String transform = null;

    public void setOutputType (String v) {
	outputType = v;
    }
    public String getOutputType () {
	return outputType;
    }
    private String outputType = null;

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
        String type = MimeType.getTypeFromFilename(getSrc());
        if (type == null) {
            throw new ScillaException("unknow input type");
        }

        // conversion parameters from bean properties
        List pars = new ArrayList();

	if (transform != null) {
	    // translate property to request parameters
	    pars.addAll(getRequestParameters());
	}

	URL url = pageContext.getServletContext().getResource(getSrc());
        return new Request(url, type, pars);
    }

    /**
     * Create an URL to the scilla servlet from the current
     * source and transformations properties.
     */
    private String getImageUrl ()
    throws JspException {
	StringBuffer out = new StringBuffer();

	HttpServletRequest pageRequest = (HttpServletRequest) pageContext.getRequest();
	out.append(pageRequest.getContextPath());
	out.append(SERVLET_MAPPING);
	out.append(getSrc());
	if (transform != null) {
	    // translate property to query string
	    Iterator it = getRequestParameters().iterator();
	    for (int i = 0; it.hasNext(); i++) {
		RequestParameter rp = (RequestParameter) it.next();
		out.append(i > 0 ? '&' : '?');
		out.append(URLEncoder.encode(rp.key));
		out.append('=');
		out.append(URLEncoder.encode(rp.val));
	    }
	}
	return out.toString();
    }

    /**
     * Translate transform property to request parameters.
     */
    private List getRequestParameters () {
	List result = new ArrayList();

	if (outputType != null) {
	    result.add(new RequestParameter(Request.OUTPUT_TYPE_PARAMETER, outputType));
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
