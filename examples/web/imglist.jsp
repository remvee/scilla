<%@ page import="java.io.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.scilla.info.*" %>
<%@ taglib uri="/WEB-INF/scilla.tld" prefix="scilla" %>
<%
    String path = "";
    if (request.getParameter("d") != null) {
	path = request.getParameter("d");
    }
    String pathEncoded = "d="+URLEncoder.encode(path);

    boolean negative = request.getParameter("n") != null;
%>
<html>
    <head>
	<title>
	    img: <%=path%>
	</title>
    </head>
    <body bgcolor="white">
<%
    Vector vec = new Vector();
    Vector dirVec = new Vector();
    org.scilla.Config scillaConfig = org.scilla.ConfigProvider.get();
    String sourceDir = scillaConfig.getString(org.scilla.Config.SOURCE_DIR_KEY);
    File dir = new File(sourceDir+"/"+path);
    if (dir.isDirectory()) {
	String[] files = dir.list();
	Arrays.sort(files);
	for (int i = 0; i < files.length; i++) {
	    String s = files[i];

	    // hidden file?
	    if (s.startsWith(".")) {
		continue;
	    }

	    // directory?
	    File f = new File(sourceDir+"/"+path+"/"+s);
	    if (f.isDirectory()) {
		dirVec.add(s);
		continue;
	    }

	    // image?
	    String type = org.scilla.util.MimeType.getTypeFromFilename(s);
	    if (type != null && type.startsWith("image/")) {
		vec.add(s);
		continue;
	    }
	}
	{
	    String sEncoded = "d="+URLEncoder.encode(path)+(negative ? "" : "&n=1");
%>
	<div align="right">
	    <small>
		<a target="_top" href="img.jsp?<%=sEncoded%>">(<%= negative ? "P" : "N" %>)</a>
	    </small>
	</div>
<%
	}
	if (path.lastIndexOf('/') != -1)
	{
	    String s = path.substring(0, path.lastIndexOf('/'));
	    String sEncoded = "d="+URLEncoder.encode(s)+(negative ? "&n=1" : "");
%>
	<br><a target="_top" href="img.jsp?<%=sEncoded%>">..</a>
<%
	}
	Enumeration e = dirVec.elements();
	while (e.hasMoreElements()) {
	    String s = (String) e.nextElement();
	    String sEncoded = "d="+URLEncoder.encode(path+"/"+s)+(negative ? "&n=1" : "");
%>
	<br><a target="_top" href="img.jsp?<%=sEncoded%>"><%=s%></a>
<%
	}

	if (vec.size() > 0) {
%>
	<hr>
	<table cellspacing="0" cellpadding="0">
<%
	    // loop the image list
	    e = vec.elements();
	    while (e.hasMoreElements()) {
		String fn = (String) e.nextElement();

		Info info = InfoFactory.get(sourceDir+File.separator+path+File.separator+fn);

		String s = null;
		if (negative) {
		    s = "scale=66x100!&negate=1";
		    if (info != null && info.getInt(ImageInfo.WIDTH) > info.getInt(ImageInfo.HEIGHT)) {
			s = "rotate=270&"+s;
		    }
		} else {
		    s = "scale=83x54!";
		    if (info != null && info.getInt(ImageInfo.WIDTH) < info.getInt(ImageInfo.HEIGHT)) {
			s += "&rotate=270";
		    }
		}

		String filepath = path+"/"+fn;
		String viewUrl = "imgview.jsp?f="+filepath;
		String imgUrl = "scilla/"+filepath+"?outputtype=jpg&"+s;

		if (negative) {
%>
	    <tr>
		<td><scilla:img src="images/film-left.gif"/></td>
		<td width="68" height="100" bgcolor="black" align="center" valign="center"><a target="viewer" href="<%= viewUrl %>"><img src="<%= imgUrl %>" border="0"></a></td>
		<td><scilla:img src="images/film-right.gif"/></td>
	    </TR -->
<%
		} else {
%>
	    <tr>
		<td colspan="3"><scilla:img src="images/diaframe-n.png" scale="50%"/></td>
	    </tr>
	    <tr>
		<td><scilla:img src="images/diaframe-w.png" scale="50%"/></td>
		<td><a href="<%= viewUrl %>" target="viewer"><img src="<%= imgUrl %>" width="83" height="54" border="0"/></a></td>
		<td><scilla:img src="images/diaframe-e.png" scale="50%"/></td>
	    </tr>
	    <tr>
		<td colspan="3"><scilla:img src="images/diaframe-s.png" scale="50%"/></td>
	    </tr>
	    <tr>
		<td colspan="3" height="3px"></td>
	    </tr>
<%
		}
	    }
%>
	</table>
<%
	}
    }
    else {
%>
	<br><big><strong>Oeps: not a directory: <%=path%></strong></big>
<%
    }
%>
    </body>
</html>
