import java.io.*;
import java.util.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.scilla.Config;
import org.scilla.ConfigProvider;
import org.scilla.Logger;
import org.scilla.LoggerFactory;
import org.scilla.util.mp3.*;

public class PlaylistServlet extends HttpServlet
{
    final static Config scillaConfig = ConfigProvider.get();
    final static Logger log = LoggerFactory.get(PlaylistServlet.class);

    final static String PATH_PARAM = "d"; // MUST NOT BE IN USE BY SCILLA!!
    final static String RECURS_PARAM = "r"; // MUST NOT BE IN USE BY SCILLA!!
    final static String SCILLA_SERVLET = "scilla";

    public void doGet (HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
	// handle encoding parameters
	String encoding = "?"+request.getQueryString();

	// handle path parameter
	String path = "";
	if (request.getParameter(PATH_PARAM) != null)
	{
	    path = request.getParameter(PATH_PARAM);
	    encoding = stripParameter(encoding, PATH_PARAM);
	}

	// handle recurs parameter
	boolean recursive = false;
	if (request.getParameter(RECURS_PARAM) != null)
	{
	    recursive = true;
	    encoding = stripParameter(encoding, RECURS_PARAM);
	}

	// determine output type (m3u, pls..)
	String type = request.getServletPath();
	type = type.substring(type.lastIndexOf('.')+1);

	// determine scilla url
	String urlPrefix = "http://"
		+request.getServerName()+":"+request.getServerPort()
		+request.getContextPath()+request.getServletPath();
	// strip this servlet name
	urlPrefix = urlPrefix.substring(0, urlPrefix.lastIndexOf('/'));
	urlPrefix += "/"+SCILLA_SERVLET+"/";

	// collect files to list
	final String source = scillaConfig.getString(Config.SOURCE_DIR_KEY);
	Vector files = new Vector();
	File f = new File(source+File.separator+path);
	if (f.isDirectory() && recursive)
	{
	    String[] l = (String[]) find(source, path).toArray(new String[0]);
	    for (int i = 0; i < l.length; i++)
	    {
		if (isPlayable(l[i])) files.add(l[i]);
	    }
	}
	else if (f.isDirectory())
	{
	    String[] l = f.list();
	    for (int i = 0; i < l.length; i++)
	    {
		if (isPlayable(l[i])) files.add(path+File.separator+l[i]);
	    }
	}
	else if (f.exists() && isPlayable(path))
	{
	    files.add(path);
	}
	Collections.sort(files);

	// write playlist
	PrintWriter out = response.getWriter();
	if (type.equals("m3u"))
	{
	    // create mpegurl data
	    response.setContentType("audio/mpegurl");
	    out.println("#EXTM3U");
	    Iterator it = files.iterator();
	    while (it.hasNext())
	    {
		String fn = (String) it.next();

		// track info
		File t = new File(source+File.separator+fn);
		out.println("#EXTINF:"+getTrackLength(t)+","+getTrackTitle(t));

		// url
		fn = fn.replace(File.separatorChar, '/');
		fn = fn.replace(' ', '+');
		out.println(urlPrefix+fn+encoding);
	    }
	}
	else
	{
	    response.setContentType("text/html");
	    out.println("<HTML><HEAD>");
	    out.println("<TITLE>Playlist of type: '"+type+"' NOT YET IMPLEMENTED</TITLE>");
	    out.println("</HEAD><BODY>");
	    out.println("<H1>Playlist of type: '"+type+"' NOT YET IMPLEMENTED</H1>");
	    out.println("</BODY></HTML>");
	}
    }

    String getTrackTitle (File track)
    {
	String title = "";
	try
	{
	    ID3v1 id3 = new ID3v1(track);
	    if (id3.getArtist() != null && id3.getArtist().length() != 0)
	    {
		title += id3.getArtist() + " - ";
	    }
	    if (id3.getAlbum() != null && id3.getAlbum().length() != 0)
	    {
		title += id3.getAlbum() + " - ";
	    }
	    if (id3.getTitle() != null && id3.getTitle().length() != 0)
	    {
		title += id3.getTitle();
	    }
	    if (title.endsWith(" - "))
	    {
		title = title.substring(0, title.lastIndexOf(" - "));
	    }
	    if (title.length() == 0)
	    {
		title = track.getName();
		title = title.substring(title.lastIndexOf(File.separator)+1);
		title = title.substring(0, title.lastIndexOf('.'));
	    }
	}
	catch (Throwable ex) { }
	return title;
    }

    int getTrackLength (File track)
    {
	int length = -1;
	try
	{
	    FrameHeader fh = new FrameHeader(track);
	    length = fh.getLength();
	}
	catch (Throwable ex) { }
	return length;
    }

    String stripParameter (String in, String param)
    {
	int a = in.indexOf(param+"=");
	if (a != -1)
	{
	    int b = in.indexOf('&', a);
	    in = b != -1
		    ? in.substring(0, a) + in.substring(b+1)
		    : in.substring(0, a);
	    if (in.endsWith("&"))
	    {
		in = in.substring(0, in.length()-1);
	    }
	}
	else
	{
	    log.error("could not strip '"+param+"' from query");
	}
	return in;
    }

    boolean isPlayable (String fn)
    {
	return fn.toLowerCase().endsWith(".mp3")
		|| fn.toLowerCase().endsWith(".wav");
    }

    Vector find (String source, String dn)
    {
	File dir = new File(source+File.separator+dn);
	Vector vec = new Vector();
	String[] files = dir.list();
	for (int i = 0; i < files.length; i++)
	{
	    String fn = dn+File.separator+files[i];
	    File f = new File(source+File.separator+fn);
	    if (f.isDirectory())
	    {
		vec.addAll(find(source, fn));
	    }
	    else if (files[i].toLowerCase().endsWith(".mp3"))
	    {
		vec.add(fn);
	    }
	}

	return vec;
    }
}
