<%@ taglib uri="/WEB-INF/scilla.tld" prefix="scilla" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<jsp:useBean id="dir" scope="request" class="test.DirectoryBean"/>
<c:choose>
    <c:when test="${param.d != null}">
	<jsp:setProperty name="dir" property="path" value='<%= request.getParameter("d") %>'/>
    </c:when>
    <c:otherwise>
	<jsp:setProperty name="dir" property="path" value=""/>
    </c:otherwise>
</c:choose>

<html>
    <head>
	<title>
	    img: <c:out value="${dir.path}"/>
	</title>
    </head>
    <body bgcolor="white">

	<div class="directories">
	    <c:if test="${dir.parentPath != null}">
		<c:url var="url" value="img.jsp">
		    <c:param name="d" value="${dir.parentPath}"/>
		</c:url>
		<br />
		<a target="_top" href='<c:out value="${url}"/>'>..</a>
	    </c:if>
	    <c:forEach var="d" items="${dir.list.directory}">
		<c:url var="url" value="img.jsp">
		    <c:param name="d" value="${d.path}"/>
		</c:url>
		<br />
		<a target="_top" href='<c:out value="${url}"/>'><c:out value="${d.name}"/></a>
	    </c:forEach>
	</div>

	<hr />
	<c:if test="${dir.list.count.image > 0}">
	    <table class="images" cellspacing="0" cellpadding="0">
		<c:forEach var="image" items="${dir.list.image}">
		    <scilla:img name="image" var="imgurl" outputtype="jpeg">
			<c:if test="${image.width > image.height}">
			    <scilla:par key="rotate" value="270"/>
			</c:if>
			<scilla:par key="scale" value="66x100"/>
			<scilla:par key="negate" value="1"/>
		    </scilla:img>
		    <c:url var="viewurl" value="imgview.jsp">
			<c:param name="f" value="${image.directoryBeanLocation}"/>
		    </c:url>
		    <tr>
			<td><scilla:img src="images/film-left.gif"/></td>
			<td width="68" height="100" bgcolor="black" align="center" valign="center"><a target="viewer" href="<c:out value="${viewurl}"/>"><img src="<c:out value="${imgurl}"/>" border="0"></a></td>
			<td><scilla:img src="images/film-right.gif"/></td>
		    </tr>
		</c:forEach>
	    </table>
	</c:if>

    </body>
</html>
