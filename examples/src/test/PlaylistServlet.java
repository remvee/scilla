package test;

import java.io.*;
import java.util.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.scilla.Config;
import org.scilla.ConfigProvider;
import org.scilla.info.*;

public class PlaylistServlet extends HttpServlet {
    private final static Config scillaConfig = ConfigProvider.get();
    private final static Log log = LogFactory.getLog(PlaylistServlet.class);

    public final static String PATH_PARAM = "d";
    public final static String RECURS_PARAM = "r";
    public final static String OUTPUT_TYPE_PARAM = "t";
    public final static String DEFAULT_OUTPUT_TYPE = "ogg";
    public final static String STREAM_SERVLET = "stream";

    public void doGet (HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
	// handle path parameter
	String path = "";
	if (request.getParameter(PATH_PARAM) != null) {
	    path = request.getParameter(PATH_PARAM);
	}

	// handle recurs parameter
	boolean recursive = false;
	if (request.getParameter(RECURS_PARAM) != null) {
	    recursive = true;
	}

	// output type parameter
	String outputType = DEFAULT_OUTPUT_TYPE;
	if (request.getParameter(OUTPUT_TYPE_PARAM) != null) {
	    outputType = request.getParameter(OUTPUT_TYPE_PARAM);
	}

	// determine output type (m3u, pls..)
	String type = request.getServletPath();
	type = type.substring(type.lastIndexOf('.')+1);

	// request from localhost does need a stream
	boolean isLocal = false;
	String urlPrefix = null;
	if (request.getServerName().equals("127.0.0.1")
		|| request.getServerName().equals("localhost")) {
	    isLocal = true;
	} else {
	    // determine stream servlet url
	    urlPrefix = "http://"
		    + request.getServerName() + ":" + request.getServerPort()
		    + request.getContextPath() + request.getServletPath();
	    // strip this servlet name
	    urlPrefix = urlPrefix.substring(0, urlPrefix.lastIndexOf('/'));
	    urlPrefix += "/" + STREAM_SERVLET + "/";
	}

	// collect files to list
	final String source = scillaConfig.getString(Config.SOURCE_DIR_KEY);
	Vector files = new Vector();
	File f = new File(source+File.separator+path);
	if (f.isDirectory() && recursive) {
	    String[] l = (String[]) find(source, path).toArray(new String[0]);
	    for (int i = 0; i < l.length; i++) {
		if (isPlayable(l[i])) files.add(l[i]);
	    }
	} else if (f.isDirectory()) {
	    String[] l = f.list();
	    for (int i = 0; i < l.length; i++) {
		if (isPlayable(l[i])) {
		    files.add(path+File.separator+l[i]);
		}
	    }
	} else if (f.exists() && isPlayable(path)) {
	    files.add(path);
	}
	Collections.sort(files);

	// write playlist
	PrintWriter out = response.getWriter();
	if (type.equals("m3u")) {
	    // create mpegurl data
	    response.setContentType("audio/mpegurl");
	    out.println("#EXTM3U");
	    Iterator it = files.iterator();
	    while (it.hasNext()) {
		String fn = (String) it.next();

		File t = new File(source+File.separator+fn);
		String url = isLocal
		    ? (source + fn)
		    : (urlPrefix + pathInfoEncode(fn) + "." + outputType);

		// track info
		out.println("#EXTINF:"+getTrackLength(t)+","+getTrackTitle(t));

		// url
		out.println(url);
	    }
	} else if (type.equals("pls")) {
	    // create pls data
	    response.setContentType("audio/scpls");
	    out.println("[playlist]");
	    out.println("NumberOfEntries=" + files.size());

	    Iterator it = files.iterator();
	    for (int num = 1; it.hasNext(); num++) {
		String fn = (String) it.next();

		File t = new File(source+File.separator+fn);
		String url = isLocal
		    ? (source + fn)
		    : (urlPrefix + pathInfoEncode(fn) + "." + outputType);

		out.println("File" + num + "=" + url);
		out.println("Title" + num + "=" + getTrackTitle(t));
		out.println("Length" + num + "=" + getTrackLength(t));
	    }
	} else {
	    response.setContentType("text/html");
	    out.println("<HTML><HEAD>");
	    out.println("<TITLE>Playlist of type: '"+type+"' NOT YET IMPLEMENTED</TITLE>");
	    out.println("</HEAD><BODY>");
	    out.println("<H1>Playlist of type: '"+type+"' NOT YET IMPLEMENTED</H1>");
	    out.println("</BODY></HTML>");
	}
    }

    String getTrackTitle (File track) {
	String title = "";
	try {
	    AudioInfo info = (AudioInfo) InfoFactory.get(track.getCanonicalPath());

	    if (info.getArtist() != null && info.getArtist().length() != 0) {
		title += info.getArtist() + " - ";
	    }
	    if (info.getPerformer() != null && info.getPerformer().length() != 0) {
		title += info.getPerformer() + " - ";
	    }
	    if (info.getAlbum() != null && info.getAlbum().length() != 0) {
		title += info.getAlbum() + " - ";
	    }
	    if (info.getTitle() != null && info.getTitle().length() != 0) {
		title += info.getTitle();
	    }
	    if (title.endsWith(" - ")) {
		title = title.substring(0, title.lastIndexOf(" - "));
	    }
	    if (title.length() == 0) {
		title = track.getName();
		title = title.substring(title.lastIndexOf(File.separator)+1);
		title = title.substring(0, title.lastIndexOf('.'));
	    }
	} catch (Throwable ex) {
	    // ignore
	}
	return title;
    }

    int getTrackLength (File track) {
	int length = -1;
	try {
	    AudioInfo info = (AudioInfo) InfoFactory.get(track.getCanonicalPath());
	    length = info.getLength();
	} catch (Throwable ex) {
	    // ignore
	}
	return length;
    }

    String stripParameter (String in, String param) {
	int a = in.indexOf(param+"=");
	if (a != -1) {
	    int b = in.indexOf('&', a);
	    in = b != -1
		    ? in.substring(0, a) + in.substring(b+1)
		    : in.substring(0, a);
	    if (in.endsWith("&")) {
		in = in.substring(0, in.length()-1);
	    }
	} else {
	    log.error("could not strip '"+param+"' from query");
	}
	return in;
    }

    boolean isPlayable (String fn) {
	return fn.toLowerCase().endsWith(".mp3")
		|| fn.toLowerCase().endsWith(".ogg")
		|| fn.toLowerCase().endsWith(".wav");
    }

    Vector find (String source, String dn) {
	File dir = new File(source+File.separator+dn);
	Vector vec = new Vector();
	String[] files = dir.list();
	for (int i = 0; i < files.length; i++) {
	    String fn = dn+File.separator+files[i];
	    File f = new File(source+File.separator+fn);
	    if (f.isDirectory()) {
		vec.addAll(find(source, fn));
	    } else if (isPlayable(fn)) {
		vec.add(fn);
	    }
	}

	return vec;
    }

    private String pathInfoEncode (String fname) {
	String t = fname.replace(File.separatorChar, '/');
	return t.replace(' ', '+');
    }
}
