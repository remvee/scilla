<%!
    final static String modemEncoding =
	    "?mode=m&resample=16&vbr=1&vbrquality=6&maxbitrate=56";
    final static String isdnEncoding =
	    "?preset=voice";

    void println (JspWriter out, String prefix, String path, String preset)
    throws java.io.IOException
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
    
%><%
    org.scilla.Config scillaConfig = org.scilla.Config.getInstance();

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
	    +"/scilla/servlet/scilla/";
    }

    String fname = scillaConfig.getSourceDir()+"/"+path;
    java.io.File f = new java.io.File(fname);
    if (f.isDirectory())
    {
	String[] files = f.list();
	java.util.Arrays.sort(files);

	response.setContentType("audio/mpegurl");
	for (int i = 0; i < files.length; i++)
	{
	    String filename = files[i];
	    if (filename.toLowerCase().endsWith(".mp3"))
	    {
		println(out, urlPrefix, path+'/'+filename, preset);
	    }
	}
	out.close();
    }
    else if (f.exists() && fname.toLowerCase().endsWith(".mp3"))
    {
	response.setContentType("audio/mpegurl");
	println(out, urlPrefix, path, preset);
	out.close();
    }
    else if (f.exists() && fname.toLowerCase().endsWith(".m3u"))
    {
	response.setContentType("audio/mpegurl");
	java.io.BufferedReader in = new java.io.BufferedReader(
		new java.io.FileReader(f));
	String line;
	path = path.substring(0, path.lastIndexOf('/'));
	while ((line = in.readLine()) != null)
	{
	    println(out, urlPrefix, path+'/'+line, preset);
	}
	in.close();
	out.close();
    }
%>
