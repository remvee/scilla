import java.io.*;
import java.util.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.scilla.*;
import org.scilla.util.mp3.ID3v1;

public class M3UServlet extends HttpServlet
{
    final static Config scillaConfig = ConfigFactory.get();
    final static String PROPERTY_FILE = "M3UServlet.properties";
    static Properties config = null;

    Properties getConfig ()
    throws IOException
    {
	if (config == null)
	{
	    config = new Properties();
	    InputStream in = null;
	    try
	    {
		in = this.getClass().getClassLoader().getResourceAsStream(
			PROPERTY_FILE);
		if (in != null) config.load(in);
	    }
	    finally
	    {
		if (in != null)
		{
		    try { in.close(); }
		    catch (IOException e) { e.printStackTrace(); }
		}
	    }
	}

	return config;
    }

    /** TODO make W32 friendly by translating File.separator */
    public void doGet (HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
	String path = "";
	if (request.getParameter("d") != null) path = request.getParameter("d");

	String preset = "stream";
	if (request.getParameter("f") != null) preset = request.getParameter("f");
	boolean recursive = request.getParameter("r") != null;

	String urlPrefix = "";
	String source = scillaConfig.getString(Config.SOURCE_DIR_KEY);
	if (preset.equals("file"))
	{
	    urlPrefix = source+File.separator;
	}
	else
	{
	    urlPrefix = "http://"
		+request.getServerName()+":"+request.getServerPort()
		+"/scilla/servlet/scilla/";
	}

	String fn = source+File.separator+path;
	File f = new File(fn);
	PrintWriter out = response.getWriter();
	if (f.isDirectory() && recursive)
	{
	    response.setContentType("audio/mpegurl");

	    Vector files = find(source, path);
	    Collections.sort(files);
	    Iterator it = files.iterator();
	    while (it.hasNext())
	    {
		String filename = (String) it.next();
		if (filename.toLowerCase().endsWith(".mp3")
			|| filename.toLowerCase().endsWith(".wav"))
		{
		    println(out, urlPrefix, filename, preset);
		}
	    }
	}
	else if (f.isDirectory())
	{
	    String[] files = f.list();
	    Arrays.sort(files);
 
	    response.setContentType("audio/mpegurl");
	    for (int i = 0; i < files.length; i++)
	    {
		String filename = files[i];
		if (filename.toLowerCase().endsWith(".mp3")
			|| filename.toLowerCase().endsWith(".wav"))
		{
		    println(out, urlPrefix, path+'/'+filename, preset);
		}
	    }
	}
	else if (f.exists() && fn.toLowerCase().endsWith(".mp3")
		|| fn.toLowerCase().endsWith(".wav"))
	{
	    response.setContentType("audio/mpegurl");
	    println(out, urlPrefix, path, preset);
	}
	else if (f.exists() && fn.toLowerCase().endsWith(".m3u"))
	{
	    response.setContentType("audio/mpegurl");
	    BufferedReader in = new BufferedReader(new FileReader(f));
	    String line;
	    path = path.substring(0, path.lastIndexOf('/'));
	    while ((line = in.readLine()) != null)
	    {
		println(out, urlPrefix, path+'/'+line, preset);
	    }
	    in.close();
	}
    }

    void println (PrintWriter out, String prefix, String path, String preset)
    throws IOException
    {
	if (preset.equals("file"))
	{
	    out.println(prefix + path);
	}
	else
	{
	    out.print(prefix + path.replace(' ', '+'));
	    String encoding = getConfig().getProperty(preset+".encoding");
	    if (encoding != null) out.println(encoding);
	}
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
