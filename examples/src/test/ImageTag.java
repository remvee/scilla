package test;

import java.util.*;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import org.scilla.*;
import org.scilla.util.*;

public class ImageTag extends TagSupport {

    public ImageTag () {
	super();
    }

    public int doStartTag ()
    throws JspException {
	StringBuffer out = new StringBuffer();
	out.append("<img");

	out.append(" src=\"");
	out.append(HTMLUtil.escape(getImageUrl()));
	out.append('"');

	try {
	    String fn = getRequest().getOutputFile();
	    ImageDim dim = ImageDim.measure(fn);
	    out.append(" width=\"");
	    out.append(dim.getWidth()+"");
	    out.append("\" height=\"");
	    out.append(dim.getHeight()+"");
	    out.append('"');
	} catch (Throwable ex) {
	    // ignore..
	}

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

	out.append("/>");

	try {
            pageContext.getOut().print(out.toString());
        } catch (Exception ex) {
            throw new JspException("IO problems");
        }
        return SKIP_BODY;
    }

    public void setSrc (String src) {
	this.src = src;
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

    public void setScale (String v) {
	this.scale = v;
    }
    public String getScale () {
	return scale;
    }
    private String scale = null;

    public void setAlt (String v) {
	this.alt = v;
    }
    public String getAlt () {
	return alt;
    }
    private String alt = "";

    public void setBorder (String v) {
	this.border = v;
    }
    public String getBorder () {
	return border;
    }
    private String border = null;

    public void setStyle (String v) {
	this.style = v;
    }
    public String getStyle () {
	return style;
    }
    private String style = null;

    public void setStyleClass (String v) {
	this.styleClass = v;
    }
    public String getStyleClass () {
	return styleClass;
    }
    private String styleClass = null;

    private Request getRequest ()
    throws Exception {
        // source mime type
        String type = MimeType.getTypeFromFilename(getSrc());
        if (type == null) {
            throw new ScillaException("unknow input type");
        }

        // conversion parameters from bean properties
        List pars = new Vector();

	if (scale != null) {
	    pars.add(new RequestParameter("scale", scale));
	}

	URL url = pageContext.getServletContext().getResource(getSrc());
        return new Request(url, type, pars);
    }

    private String getImageUrl ()
    throws JspException {
	StringBuffer out = new StringBuffer();

	HttpServletRequest pageRequest = (HttpServletRequest) pageContext.getRequest();
	out.append(pageRequest.getContextPath());
	out.append("/img");
	out.append(getSrc());
	if (scale != null) {
	    out.append("?scale=");
	    out.append(URLEncoder.encode(scale));
	}
	return out.toString();
    }
}
