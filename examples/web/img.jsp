<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<c:url var="url" value="imglist.jsp">
    <c:param name="d" value="${param.d}"/>
</c:url>
<html>
    <head>
	<title>Scilla Examples: Image Browser</title>
    </head>
    <frameset cols="150px,*">
	<frame name="browser" src="<c:out value="${url}"/>"/>
	<frame name="viewer" src="imgview.jsp"/>
    </frameset>
    <noframes>
	Oeps, sorry..  this demo depends on frames..
    </noframes>
</html>
