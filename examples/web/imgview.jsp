<%@ taglib uri="/WEB-INF/scilla.tld" prefix="scilla" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<%@ page import="test.AppConfig,java.io.File" %>
<%@ page import="org.scilla.info.*" %>
<c:if test="${param.f != null}">
<%
    ImageInfo ii = (ImageInfo) InfoFactory.get(AppConfig.getSourceDir() + File.separator + request.getParameter("f"));
    session.setAttribute("viewimage", ii);
%>
</c:if>

<html>
    <body bgcolor="white">
	<table width="100%" height="100%">
	    <tr><td align="center" valign="center"><div align="center">
		<c:choose>
		    <c:when test="${viewimage != null}">
			<code><c:out value="${viewimage.directoryBeanLocation}"/></code>
			<p>
			    <scilla:img name="viewimage" transform="scale(600x600)" border="1"/>
			</p>
			<p>
			    <scilla:img name="viewimage" var="url" transform="scale(640x480)"/>
			    <a href="<c:out value="${url}"/>">640x480</a> |
			    <scilla:img name="viewimage" var="url" transform="scale(800x600)"/>
			    <a href="<c:out value="${url}"/>">800x600</a> |
			    <scilla:img name="viewimage" var="url" transform="scale(1024x768)"/>
			    <a href="<c:out value="${url}"/>">1024x768</a> |
			    <scilla:img name="viewimage" var="url" transform="scale(1280x1024)"/>
			    <a href="<c:out value="${url}"/>">1280x1024</a> |
			    <scilla:img name="viewimage" var="url" transform="scale(1600x1200)"/>
			    <a href="<c:out value="${url}"/>">1600x1200</a> |
			    <scilla:img name="viewimage" var="url" transform="scale(1800x1440)"/>
			    <a href="<c:out value="${url}"/>">1800x1440</a> |
			    <scilla:img name="viewimage" var="url"/>
			    <a href="<c:out value="${url}"/>">Original
			    <small><c:out value="${viewimage.width}x${viewimage.height}"/></small></a>
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
