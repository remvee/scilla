package test;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.scilla.RequestParameter;

public class ParameterTag extends TagSupport {
    /** logger */
    private static final Log log = LogFactory.getLog(ParameterTag.class);

    public ParameterTag () {
	super();
    }

    public int doStartTag ()
    throws JspException {
	ImageTag imageTag = (ImageTag) findAncestorWithClass(this, ImageTag.class);

	if (imageTag == null) {
	    throw new JspException("stray parameter tag");
	}

	imageTag.addParameter(new RequestParameter(key, value));

        return SKIP_BODY;
    }

    public void setKey (String v) {
	key = v;
    }
    public String getKey () {
	return key;
    }
    private String key = null;

    public void setValue (String v) {
	value = v;
    }
    public String getValue () {
	return value;
    }
    private String value = null;
}
