<%@ taglib uri="/WEB-INF/scilla.tld" prefix="scilla" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c_rt" %>

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
	    <c:if test="${dir.numOfArtists == 1}">
		<c:out value="${dir.artist}"/>
	    </c:if>
	    <c:if test="${dir.numOfPerformers == 1}">
		<c:out value="${dir.performer}"/>
	    </c:if>
	    <c:if test="${dir.numOfAlbums == 1}">
		<c:out value="${dir.album}"/>
	    </c:if>
	</title>
    </head>
    <body>
	<c:if test="${dir.numOfDirectories > 0}">
	    <ul>
		<c:forEach var="d" items="${dir.directories}">
		    <c:url var="url" value="audio.jsp">
			<c:param name="d" value="${d.path}"/>
		    </c:url>
		    <li>
			<a href='<c:out value="${url}"/>'><c:out value="${d.name}"/></a>
			<c:if test="${d.numOfTracks > 0}">
			    <scilla:playlist id="d" var="playlist"/>
			    <a href='<c:out value="${playlist}"/>'>
				<scilla:img src="images/speaker.png" border="0" alt="Play">
				    <scilla:par key="scale" value="14x14"/>
				</scilla:img>
			    </a>
			</c:if>
		    </li>
		</c:forEach>
	    </ul>
	</c:if>

	<c:if test="${dir.numOfTracks > 0}">
	    <div class="tracks">
		<c:if test="${dir.numOfArtists == 1}">
		    <div class="artist"><c:out value="${dir.artist}"/></div>
		</c:if>
		<c:if test="${dir.numOfPerformers == 1}">
		    <div class="performer"><c:out value="${dir.performer}"/></div>
		</c:if>
		<c:if test="${dir.numOfAlbums == 1}">
		    <div class="album"><c:out value="${dir.album}"/></div>
		</c:if>
		<scilla:playlist id="dir" var="playlist"/>
		<a href='<c:out value="${playlist}"/>'>
		    <scilla:img src="images/speaker.png" border="0" alt="Play">
			<scilla:par key="scale" value="14x14"/>
		    </scilla:img>
		</a>
		<table>
		    <c:forEach var="track" items="${dir.tracks}" varStatus="stat">
			<tr>
			    <td align="right"><c:out value="${stat.count}"/></td>
			    <c:if test="${dir.numOfArtists != 1}">
				<td>
				    <div class="artist"><c:out value="${track.artist}"/></div>
				</td>
			    </c:if>
			    <c:if test="${dir.numOfPerformers != 1}">
				<td>
				    <div class="performer"><c:out value="${track.performer}"/></div>
				</td>
			    </c:if>
			    <c:if test="${dir.numOfAlbums != 1}">
				<td>
				    <div class="album"><c:out value="${track.album}"/></div>
				</td>
			    </c:if>
			    <td>
				<div class="title"><c:out value="${track.title}"/></div>
			    </td>
			    <td align="right">
				<c:out value="${track.time}"/>
			    </td>
			    <td>
				<scilla:playlist id="track" var="playlist"/>
				<a href='<c:out value="${playlist}"/>'>
				    <scilla:img src="images/speaker.png" border="0" alt="Play">
					<scilla:par key="scale" value="14x14"/>
				    </scilla:img>
				</a>
			    </td>
			</tr>
		    </c:forEach>
		    <tr>
			<td></td>
			<c:if test="${dir.numOfArtists != 1}"><td></td></c:if>
			<c:if test="${dir.numOfPerformers != 1}"><td></td></c:if>
			<c:if test="${dir.numOfAlbums != 1}"><td></td></c:if>
			<td></td>
			<td align="right"><c:out value="${dir.totalTime}"/></td>
			<td>
			    <scilla:playlist id="dir" var="playlist"/>
			    <a href='<c:out value="${playlist}"/>'>
				<scilla:img src="images/speaker.png" border="0" alt="Play">
				    <scilla:par key="scale" value="14x14"/>
				</scilla:img>
			    </a>
			</td>
		    </tr>
		</table>
	    </div>
	</c:if>

	<%-- c:if test="${dir.numOfImages > 0}">
	    <div class="images">
		<c:forEach var="image" items="${dir.images}">
		    <a href='<c:out value="${playlist}"/>'>
			<scilla:img id="image" alt='<c:out value="${image.title}"'>
			    <scilla:par key="scale" value="50x50"/>
			</scilla:img>
		    </a>
		</c:forEach>
	    </div>
	</c:if --%>
    </body>
</html>
