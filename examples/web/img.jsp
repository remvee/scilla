<%@ page import="java.net.*" %>
<%
    String dir = request.getParameter("d");
    if (dir == null) {
	dir = "";
    }
    boolean negative = request.getParameter("n") != null;
    String dirEnc = "d="+URLEncoder.encode(dir)+(negative ? "&n=1" : "");
%>
<html>
    <head>
	<title>Scilla Examples: Image Browser</title>
    </head>
    <frameset cols="150px,*">
	<frame name="browser" src="imglist.jsp?<%= dirEnc %>"/>
	<frame name="viewer" src="imgview.jsp"/>
    </frameset>
    <noframes>
	Oeps, sorry..  this demo depends on frames..
    </noframes>
</html>
