<%@ page import="java.io.File" %>
<%@ page import="org.scilla.*,org.scilla.info.*" %>
<%
    String file = "";
    if (request.getParameter("f") != null) {
	file = request.getParameter("f");
    }
%>
<html>
    <head>
	<title>
	    img: <%= file %>
	</title>
    </head>
    <body bgcolor="white">
	<table width="100%" height="100%">
	    <tr>
		<td align="center" valign="center">
<%
    if (! file.equals(""))
    {
	String fname = ConfigProvider.get().getString(Config.SOURCE_DIR_KEY)+File.separator+file;
	ImageInfo info = (ImageInfo) InfoFactory.get(fname);
	String spec = "";
	if (info != null) {
	    spec = " ("+info.getWidth()+"x"+info.getHeight()
		    +" "+info.getString(ImageInfo.CODEC)+")";
	}

	String imgUrl = "scilla/"+file+"?outputtype=jpg&scale=600x600";
	String img640x480 = "scilla/"+file+"?outputtype=jpg&scale=640x480";
	String img800x600 = "scilla/"+file+"?outputtype=jpg&scale=800x600";
	String img1024x768 = "scilla/"+file+"?outputtype=jpg&scale=1024x768";
	String img1280x1024 = "scilla/"+file+"?outputtype=jpg&scale=1280x1024";
	String img1600x1200 = "scilla/"+file+"?outputtype=jpg&scale=1600x1200";
	String img1800x1440 = "scilla/"+file+"?outputtype=jpg&scale=1800x1440";
	String imgOrig = "scilla/"+file;
%>
		    <div align="center">
			<code><%=file%></code>
			<p>
			    <img src="<%= imgUrl %>" border="1" />
			<p>
			<a href="<%= img640x480 %>">640x480</a>
			|
			<a href="<%= img800x600 %>">800x600</a>
			|
			<a href="<%= img1024x768 %>">1024x768</a>
			|
			<a href="<%= img1280x1024 %>">1280x1024</a>
			|
			<a href="<%= img1600x1200 %>">1600x1200</a>
			|
			<a href="<%= img1800x1440 %>">1800x1440</a>
			|
			<a href="<%= imgOrig %>">Original<small><%=spec%></small></a>
		    </div>
<%
    }
    else
    {
%>
		    <strong>Image Browser</strong>
<%
    }
%>
		</td>
	    </tr>
	</table>
    </body>
</html>
