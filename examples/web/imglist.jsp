<%@ page import="java.io.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.scilla.*" %>
<%@ page import="org.scilla.info.*" %>
<%@ page import="org.scilla.util.*" %>
<%@ taglib uri="/WEB-INF/scilla.tld" prefix="scilla" %>
<%
    String path = "";
    if (request.getParameter("d") != null) {
	path = request.getParameter("d");
    }

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
    // positive/negative toggle
    {
	String url = topUrl(path, ! negative);
	String label = "(" + (negative ? "P" : "N") + ")";
%>
	<div align="right">
	    <small>
		<a target="_top" href="<%= url %>"><%= label %></a>
	    </small>
	</div>
<%
    }

    // parent directory link
    if (path.lastIndexOf('/') != -1) {
	String parent = path.substring(0, path.lastIndexOf('/'));
	String url = topUrl(parent, negative);
%>
	<br /><a target="_top" href="<%= url %>">..</a>
<%
    }

    // sub directory links
    for (Iterator it = getDirectoryNames(path).iterator(); it.hasNext();) {
	String fname = (String) it.next();
	String url = topUrl(path+"/"+fname, negative);
%>
	<br /><a target="_top" href="<%= url %>"><%= fname %></a>
<%
    }

    // images
    {
%>
	<hr>
	<table cellspacing="0" cellpadding="0">
<%
	for (Iterator it = getImageNames(path).iterator(); it.hasNext();) {
	    String fname = (String) it.next();

	    // portret or landscape
	    Info info = InfoFactory.get(getFileName(path, fname));
	    String trans = null;
	    if (negative) {
		trans = "scale=66x100!&negate=1";
		if (info != null && info.getInt(ImageInfo.WIDTH) > info.getInt(ImageInfo.HEIGHT)) {
		    trans = "rotate=270&" + trans;
		}
	    } else {
		trans = "scale=83x54!";
		if (info != null && info.getInt(ImageInfo.WIDTH) < info.getInt(ImageInfo.HEIGHT)) {
		    trans = "rotate=270&" + trans;
		}
	    }

	    // view link
	    String viewUrl = viewUrl(path, fname);
	    String imgUrl = imgUrl(path, fname, trans);

	    // framed image
	    if (negative) {
%>
	    <tr>
		<td><scilla:img src="images/film-left.gif"/></td>
		<td width="68" height="100" bgcolor="black" align="center" valign="center"><a target="viewer" href="<%= viewUrl %>"><img src="<%= imgUrl %>" border="0"></a></td>
		<td><scilla:img src="images/film-right.gif"/></td>
	    </tr>
<%
	    } else {
%>
	    <tr>
		<td colspan="3"><scilla:img src="images/diaframe-n.png" transform="scale(50%)"/></td>
	    </tr>
	    <tr>
		<td><scilla:img src="images/diaframe-w.png" transform="scale(50%)"/></td>
		<td><a href="<%= viewUrl %>" target="viewer"><img src="<%= imgUrl %>" width="83" height="54" border="0"/></a></td>
		<td><scilla:img src="images/diaframe-e.png" transform="scale(50%)"/></td>
	    </tr>
	    <tr>
		<td colspan="3"><scilla:img src="images/diaframe-s.png" transform="scale(50%)"/></td>
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
%>
    </body>
</html>
<%!
    String topUrl (String path, boolean negative) {
	return "img.jsp?d=" + URLEncoder.encode(path) + (negative ? "&n=1" : "");
    }
    String viewUrl (String path, String fname) {
	return "imgview.jsp?f=" + URLEncoder.encode(path + "/" + fname);
    }
    String imgUrl (String path, String fname, String trans) {
	return "scilla/" + path + "/" + fname + "?outputtype=jpg&" + trans;
    }

    List getDirectoryNames (String path) {
	String src = ConfigProvider.get().getString(Config.SOURCE_DIR_KEY);
	String pathdir = src+File.separator+path;
	File dir = new File(pathdir);

	List result = new ArrayList();
	if (! dir.isDirectory()) {
	    return result;
	}

	String[] files = dir.list();
	Arrays.sort(files);
	for (int i = 0; i < files.length; i++) {
	    String fname = files[i];
	    if (fname.startsWith(".")) {
		continue;
	    }
	    if ((new File(pathdir+File.separator+fname)).isDirectory()) {
		result.add(fname);
	    }
	}

	return result;
    }

    List getImageNames (String path) {
	String src = ConfigProvider.get().getString(Config.SOURCE_DIR_KEY);
	String pathdir = src+File.separator+path;
	File dir = new File(pathdir);

	List result = new ArrayList();
	if (! dir.isDirectory()) {
	    return result;
	}

	String[] files = dir.list();
	Arrays.sort(files);
	for (int i = 0; i < files.length; i++) {
	    String fname = files[i];
	    if (fname.startsWith(".")) {
		continue;
	    }
	    if (! (new File(pathdir+File.separator+fname)).isDirectory()) {
		String type = MimeType.getTypeFromFilename(fname);
		if (type != null && type.startsWith("image/")) {
		    result.add(fname);
		}
	    }
	}

	return result;
    }

    String getFileName (String path, String fname) {
	String src = ConfigProvider.get().getString(Config.SOURCE_DIR_KEY);
	return src+File.separator+path+File.separator+fname;
    }
%>
