<%@ taglib uri="/WEB-INF/scilla.tld" prefix="scilla" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<jsp:useBean id="image" scope="request" class="test.ImageBean"/>
<c:if test="${param.f != null}">
    <jsp:setProperty name="image" property="fileName" value='<%= request.getParameter("f") %>'/>
</c:if>

<html>
    <body bgcolor="white">
	<table width="100%" height="100%">
	    <tr><td align="center" valign="center"><div align="center">
		<c:choose>
		    <c:when test="${param.f != null}">
			<code><c:out value="${image.fileName}"/></code>
			<p>
			    <scilla:img name="image" transform="scale(600x600)" border="1"/>
			</p>
			<p>
			    <scilla:img name="image" var="url" transform="scale(640x480)"/>
			    <a href="<c:out value="${url}"/>">640x480</a> |
			    <scilla:img name="image" var="url" transform="scale(800x600)"/>
			    <a href="<c:out value="${url}"/>">800x600</a> |
			    <scilla:img name="image" var="url" transform="scale(1024x768)"/>
			    <a href="<c:out value="${url}"/>">1024x768</a> |
			    <scilla:img name="image" var="url" transform="scale(1280x1024)"/>
			    <a href="<c:out value="${url}"/>">1280x1024</a> |
			    <scilla:img name="image" var="url" transform="scale(1600x1200)"/>
			    <a href="<c:out value="${url}"/>">1600x1200</a> |
			    <scilla:img name="image" var="url" transform="scale(1800x1440)"/>
			    <a href="<c:out value="${url}"/>">1800x1440</a> |
			    <scilla:img name="image" var="url"/>
			    <a href="<c:out value="${url}"/>">Original
			    <small><c:out value="${image.width}x${image.height}"/></small></a>
			</p>
		    </c:when>
		    <c:otherwise>
			<strong>Image Browser</strong>
		    </c:otherwise>
		</c:choose>
	    </div></td></tr>
	</table>
    </body>
</html>
