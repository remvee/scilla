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
    Config scillaConfig = Config.getInstance();

    public void doGet (HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
	String path = "";
	if (request.getParameter("d") != null) path = request.getParameter("d");

	String preset = "isdn";
	if (request.getParameter("f") != null) preset = request.getParameter("f");

	String urlPrefix = "";
	if (preset.equals("file"))
	{
	    urlPrefix = scillaConfig.getSourceDir()+"/";
	}
	else
	{
	    urlPrefix = "http://"
		+request.getServerName()+":"+request.getServerPort()
		+"/scilla/servlet/sservlet/";
	}

	String fname = scillaConfig.getSourceDir()+"/"+path;
	File f = new File(fname);
	PrintWriter out = response.getWriter();
	if (f.isDirectory())
	{
	    String[] files = f.list();
	    Arrays.sort(files);

	    response.setContentType("audio/mpegurl");
	    for (int i = 0; i < files.length; i++)
	    {
		String filename = files[i];
		if (filename.toLowerCase().endsWith(".mp3"))
		{
		    println(out, urlPrefix, path+'/'+filename, preset);
		}
	    }
	}
	else if (f.exists() && fname.toLowerCase().endsWith(".mp3"))
	{
	    response.setContentType("audio/mpegurl");
	    println(out, urlPrefix, path, preset);
	}
	else if (f.exists() && fname.toLowerCase().endsWith(".m3u"))
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

    final static String modemEncoding =
	    "?mode=m&resample=16&vbr=1&vbrquality=6&maxbitrate=56";
    final static String isdnEncoding =
	    "?preset=voice";

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

	    String encoding = "";
	    if (preset.equals("modem"))
	    {
		encoding = modemEncoding;
	    }
	    else if (preset.equals("isdn"))
	    {
		encoding = isdnEncoding;
	    }
	    out.println(encoding);
	}
    }
}
