package test;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple tag to format time from seconds to HH:MM:SS.
 *
 * @author R.W. van 't Veer
 * @version $Revision: 1.1 $
 */
public class TimeTag extends TagSupport {
    /** logger */
    private static final Log log = LogFactory.getLog(TimeTag.class);

    public int doEndTag ()
    throws JspException {
	StringBuffer out = new StringBuffer();

	Object secondsObj = pageContext.findAttribute(var);
	int seconds = -1;
	if (secondsObj instanceof String) {
	    seconds = Integer.parseInt((String) secondsObj);
	} else if (secondsObj instanceof Integer) {
	    seconds = ((Integer) secondsObj).intValue();
	} else {
	    throw new JspException("var of wrong type: " + secondsObj);
	}

	int hour = seconds / 3600;
	int minute = (seconds / 60) % 60;
	int second = seconds % 60;

	if (hour > 0) {
	    out.append(hour + ":");
	}

	if (minute > 0 || hour > 0) {
	    if (minute < 10 && hour > 0) {
		out.append('0');
	    }
	    out.append(minute + "");
	}

	out.append(':');
	if (second < 10 && (minute > 0 || hour > 0)) {
	    out.append('0');
	}
	out.append(second + "");

	try {
	    pageContext.getOut().print(out.toString());
	} catch (IOException ex) {
	    throw new JspException("failed to write", ex);
	}

	return EVAL_PAGE;
    }

    public void setVar (String v) {
	var = v;
    }
    public String getVar () {
	return var;
    }
    private String var;
}
