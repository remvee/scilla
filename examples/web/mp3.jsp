<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<c:choose>
    <c:when test="${param.d != null}">
	<c:url var="url" value="audio.jsp">
	    <c:param name="d" value="${param.d}"/>
	</c:url>
	<c:redirect url="${url}"/>
    </c:when>
    <c:otherwise>
	<c:redirect url="audio.jsp"/>
    </c:otherwise>
</c:choose>

