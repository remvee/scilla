<%@ page import="java.io.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.scilla.info.*" %>
<%
    String path = "";
    if (request.getParameter("d") != null) path = request.getParameter("d");
    String pathEncoded = "d="+URLEncoder.encode(path);

    String imgScaleEncoded = "scale=66x100";
    if (! request.getRemoteHost().equals("localhost"))
    {
	imgScaleEncoded += "&negate=1";
    }
%>
<HTML>
    <HEAD>
	<TITLE>
	    img: <%=path%>
	</TITLE>
    </HEAD>
    <BODY bgcolor=white>
<%
    Vector vec = new Vector();
    Vector dirVec = new Vector();
    org.scilla.Config scillaConfig = org.scilla.ConfigProvider.get();
    String sourceDir = scillaConfig.getString(org.scilla.Config.SOURCE_DIR_KEY);
    File dir = new File(sourceDir+"/"+path);
    if (dir.isDirectory())
    {
	String[] files = dir.list();
	Arrays.sort(files);
	for (int i = 0; i < files.length; i++)
	{
	    String s = files[i];
	    if (s.startsWith(".")) continue;

	    String type = org.scilla.util.MimeType.getTypeFromFilename(s);
	    if (type != null && type.startsWith("image/"))
	    {
		vec.add(s);
	    }
	    else
	    {
		String n = sourceDir+"/"+path+"/"+s;
		File f = new File(n);
		if (f.isDirectory())
		{
		    dirVec.add(s);
		}
	    }
	}

	if (path.lastIndexOf('/') != -1)
	{
	    String s = path.substring(0, path.lastIndexOf('/'));
	    String sEncoded = "d="+URLEncoder.encode(s);
%>
	<BR><A target="_top" href="img.jsp?<%=sEncoded%>">..</A>
<%
	}
	Enumeration e = dirVec.elements();
	while (e.hasMoreElements())
	{
	    String s = (String) e.nextElement();
	    String sEncoded = "d="+URLEncoder.encode(path+"/"+s);
%>
	<BR><A target="_top" href="img.jsp?<%=sEncoded%>"><%=s%></A>
<%
	}

	if (vec.size() > 0)
	{
%>
	<HR>
	<TABLE bgcolor=black cellspacing=0 cellpadding=0><%
	    // loop the image list
	    e = vec.elements();
	    while (e.hasMoreElements())
	    {
		String fn = (String) e.nextElement();
		String filepath = path+"/"+fn;
		String fname = sourceDir+File.separator+path+File.separator+fn;
		String s = imgScaleEncoded;

		Info info = InfoFactory.get(fname);
		if (info != null && info.getInt(ImageInfo.WIDTH) > info.getInt(ImageInfo.HEIGHT)) {
		    s = "rotate=270&"+s;
		}
%>
	    <TR>
		<TD><IMG src="images/film-left.gif"></TD><TD width=68 height=100 bgcolor=black align=center valign=center><A target="viewer" href="imgview.jsp?f=<%=filepath%>"><IMG src="servlet/scilla/<%=filepath%>?outputtype=jpg&<%=s%>" border=0></A></TD><TD><IMG src="images/film-right.gif"></TD>
	    </TR>
<%
	    }
%>
	</TABLE>
<%
	}
    }
    else
    {
%>
	<BR><BIG><STRONG>Oeps: not a directory: <%=path%></STRONG></BIG>
<%
    }
%>
</BODY></HTML>
