<%@ page import="java.net.*" %>
<%
    String dir = request.getParameter("d");
    if (dir == null) dir = "";
    String dirEnc = "d="+URLEncoder.encode(dir);
%>
<HTML>
<HEAD>
    <TITLE>scilla examples: Image Browser</TITLE>
</HEAD>
<FRAMESET cols="190,*">
    <FRAME name="browser" src="imglist.jsp?<%= dirEnc %>">
    <FRAME name="viewer" src="imgview.jsp">
</FRAMESET>
<NOFRAMES>
    Oeps, sorry..  some <A HREF="img.jsp">redirections</A>..
</NOFRAMES>
</HTML>
