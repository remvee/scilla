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
	    out.append(dim.width+"");
	    out.append("\" height=\"");
	    out.append(dim.height+"");
	    out.append('"');
	} catch (Throwable ex) {
	    ex.printStackTrace();
	    // ignore..
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

    public void setScale (String scale) {
	this.scale = scale;
    }
    public String getScale () {
	return scale;
    }
    private String scale = null;

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
