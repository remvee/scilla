<%
    String path = "";
    if (request.getParameter("d") != null) path = request.getParameter("d");
    String pathEncoded = "d="+java.net.URLEncoder.encode(path);

    int scale = 100;
    if (request.getParameter("s") != null)
    {
	String s = request.getParameter("s");
	try { scale = Integer.parseInt(s); } catch (Exception e) { }
    }
    String scaleEncoded = "s="+scale;
    String imgScaleEncoded = "scale=" + scale + "x" + scale;
    if (! request.getRemoteHost().equals("localhost"))
    {
	imgScaleEncoded += "&negate=1";
    }

    int pagenum = 0;
    if (request.getParameter("p") != null)
    {
	String s = request.getParameter("p");
	try { pagenum = Integer.parseInt(s); } catch (Exception e) { }
    }
    String pageEncoded = "p="+pagenum;

    int pagesize = 10;
    if (request.getParameter("P") != null)
    {
	String s = request.getParameter("P");
	try { pagesize = Integer.parseInt(s); } catch (Exception e) { }
    }
    String pagesizeEncoded = "P="+pagesize;
%>
<HTML>
    <HEAD>
	<TITLE>
	    img: <%=path%> (<%=scale%>, <%=pagenum%>, <%=pagesize%>)
	</TITLE>
    </HEAD>
    <BODY bgcolor=white>
<%
    java.util.Vector vec = new java.util.Vector();
    java.util.Vector dirVec = new java.util.Vector();
    org.scilla.Config scillaConfig = org.scilla.ConfigProvider.get();
    String sourceDir = scillaConfig.getString(org.scilla.Config.SOURCE_DIR_KEY);
    java.io.File dir = new java.io.File(sourceDir+"/"+path);
    if (dir.isDirectory())
    {
	String[] files = dir.list();
	java.util.Arrays.sort(files);
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
		java.io.File f = new java.io.File(n);
		if (f.isDirectory())
		{
		    dirVec.add(s);
		}
	    }
	}

	if (path.lastIndexOf('/') != -1)
	{
	    String s = path.substring(0, path.lastIndexOf('/'));
	    String sEncoded = "d="+java.net.URLEncoder.encode(s);
%>
	<BR><A href="img.jsp?<%=sEncoded%>&<%=scaleEncoded%>&p=0&<%=pagesizeEncoded%>">..</A>
<%
	}
	java.util.Enumeration e = dirVec.elements();
	while (e.hasMoreElements())
	{
	    String s = (String) e.nextElement();
	    String sEncoded = "d="+java.net.URLEncoder.encode(path+"/"+s);
%>
	<BR><A href="img.jsp?<%=sEncoded%>&<%=scaleEncoded%>&p=0&<%=pagesizeEncoded%>"><%=s%></A>
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
%>
	    <TR>
		<TD><IMG src="images/film-left.gif"></TD><TD width=100 height=100 bgcolor=black align=center valign=center><A target="viewer" href="imgview.jsp?f=<%=filepath%>"><IMG src="servlet/scilla/<%=filepath%>?outputtype=jpg&<%=imgScaleEncoded%>" border=0></A></TD><TD><IMG src="images/film-right.gif"></TD>
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
