package test;

import java.net.URLEncoder;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.scilla.info.*;

public class PlaylistTag extends TagSupport {
    /** logger */
    private static final Log log = LogFactory.getLog(PlaylistTag.class);

    public PlaylistTag () {
	super();
    }

    public int doStartTag ()
    throws JspException {
	Object obj = pageContext.findAttribute(id);

	String url = "playlist.m3u?d=";
	if (obj instanceof DirectoryBean) {
	    url += URLEncoder.encode(((DirectoryBean)obj).getPath());
	} else if (obj instanceof TrackBean) {
	    url += URLEncoder.encode(((TrackBean)obj).getFilename());
	} else {
	    throw new JspException("oeps, unknown stream");
	}
	pageContext.setAttribute(var, url);

        return SKIP_BODY;
    }

    public void setId (String v) {
	id = v;
    }
    public String getId () {
	return id;
    }
    private String id;

    public void setVar (String v) {
	var = v;
    }
    public String getVar () {
	return var;
    }
    private String var;
}
