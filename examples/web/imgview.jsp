<%@ page import="java.io.*,java.net.*,org.scilla.*,org.scilla.info.*" %>
<%
    String file = "";
    if (request.getParameter("f") != null) file = request.getParameter("f");

    int scale = 600;
    if (request.getParameter("s") != null)
    {
	String s = request.getParameter("s");
	try { scale = Integer.parseInt(s); } catch (Exception e) { }
    }
    String scaleEncoded = "s="+scale;
    String imgScaleEncoded = "scale=" + scale + "x" + scale;

    String copyrightEncoded = "";
    String copyrightFile =
	    ConfigProvider.get().getString(Config.SOURCE_DIR_KEY)
	    +'/'+file;
    copyrightFile = copyrightFile.substring(0, copyrightFile.lastIndexOf('/'));
    copyrightFile += "/copyright.txt";
    File f = new File(copyrightFile);
    if (f.isFile())
    {
	StringBuffer r = new StringBuffer();
	FileInputStream in = new FileInputStream(f);
	byte[] b = new byte[4096];
	for (int n; (n = in.read(b)) > 0; )
	{
	    r.append(new String(b, 0, n));
	}

	if (r.length() > 0)
	{
	    String msg = r.toString().replace('"', '\'');

	    String fontEncoded = "font="+java.net.URLEncoder.encode(
		    "-*-helvetica-medium-r-*-*-12-*-*-*-*-*-iso8859-*");
	    String drawEncoded = "draw="+java.net.URLEncoder.encode(
		    "text 0,15 \""+msg+"\"");
	    copyrightEncoded += "&gravity=SouthEast&fill=white&box=black&"+fontEncoded+"&"+drawEncoded;
	}
    }
%>
<HTML>
    <HEAD>
	<TITLE>
	    img: <%=file%> (<%=scale%>)
	</TITLE>
    </HEAD>
    <BODY bgcolor=white>
<%
    if (! file.equals(""))
    {
	Info info = InfoFactory.get(ConfigProvider.get().getString(Config.SOURCE_DIR_KEY)+'/'+file);
	String spec = "";
	if (info != null) {
	    spec = " ("+info.getInt(ImageInfo.WIDTH)+"x"+info.getInt(ImageInfo.HEIGHT)+" "+info.getString(ImageInfo.CODEC)+")";
	}

%>
	<TABLE width="100%" height="100%">
	    <TR>
		<TD align=center valign=center>
		    <DIV align=center>
			<CODE><%=file%></CODE>
			<P>
			    <IMG src="servlet/scilla/<%=file%>?outputtype=jpg&<%=imgScaleEncoded%><%=copyrightEncoded%>" border=1>
			<P>
			<A href="servlet/scilla/<%=file%>?outputtype=jpg&scale=640x480<%=copyrightEncoded%>">640x480</A>
			|
			<A href="servlet/scilla/<%=file%>?outputtype=jpg&scale=800x600<%=copyrightEncoded%>">800x600</A>
			|
			<A href="servlet/scilla/<%=file%>?outputtype=jpg&scale=1024x768<%=copyrightEncoded%>">1024x768</A>
			|
			<A href="servlet/scilla/<%=file%>?outputtype=jpg&scale=1280x1024<%=copyrightEncoded%>">1280x1024</A>
			|
			<A href="servlet/scilla/<%=file%>?outputtype=jpg&scale=1600x1200<%=copyrightEncoded%>">1600x1200</A>
			|
			<A href="servlet/scilla/<%=file%>?outputtype=jpg&scale=1800x1440<%=copyrightEncoded%>">1800x1440</A>
			|
			<A href="servlet/scilla/<%=file%>">Original<SMALL><%=spec%></SMALL></A>
		    </DIV>
		</TD>
	    </TR>
	</TABLE>
<%
    }
    else
    {
%>
	<TABLE width=100% height=100%>
	    <TR>
		<TD align=center valign=center>
		    <STRONG>Image Browser</STRONG>
		</TD>
	    </TR>
	</TABLE>
<%
    }
%>
    </BODY>
</HTML>
