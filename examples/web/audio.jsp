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
	<link rel="stylesheet" type="text/css" href="audio.css"/>
    </head>
    <body>
	<div class="content">
	    <c:if test="${dir.numOfDirectories > 0}">
		<div class="directories">
		    <table>
			<c:forEach var="d" items="${dir.directories}">
			    <scilla:playlist name="d" var="playlist" recursive="true"/>
			    <c:url var="url" value="audio.jsp">
				<c:param name="d" value="${d.path}"/>
			    </c:url>
			    <tr>
				<td align="right">
				    <a href='<c:out value="${url}"/>'><c:out value="${d.name}"/></a>
				</td>
				<td>
				    <a href='<c:out value="${playlist}"/>'>
					<scilla:img src="images/speaker.png" border="0" alt="Play">
					    <scilla:par key="scale" value="14x14"/>
					</scilla:img>
				    </a>
				</td>
			    </tr>
			</c:forEach>
		    </table>
		</div>
	    </c:if>

	    <c:if test="${dir.numOfTracks > 0}">
		<div class="tracks">
		    <table>
			<tr>
			    <td>
				<table width="100%" class="trackheader">
				    <tr>
					<td rowspan="2" align="left">
					    <c:if test="${dir.numOfArtists == 1}">
						<div class="artist"><c:out value="${dir.artist}"/></div>
					    </c:if>
					    <c:if test="${dir.numOfPerformers == 1}">
						<div class="performer"><c:out value="${dir.performer}"/></div>
					    </c:if>
					    <c:if test="${dir.numOfAlbums == 1}">
						<div class="album"><c:out value="${dir.album}"/></div>
					    </c:if>
					</td>
					<td align="right">
					    <scilla:playlist name="dir" var="playlist"/>
					    <a href='<c:out value="${playlist}"/>'>
						<scilla:img src="images/speaker.png" border="0" alt="Play">
						    <scilla:par key="scale" value="14x14"/>
						</scilla:img>
					    </a>
					</td>
				    </tr>
				    <tr>
					<td align="right">
					    <c:if test="${dir.numOfRecordingDates == 1}">
						<div class="year"><c:out value="${dir.recordingDate}"/></div>
					    </c:if>
					</td>
				    </tr>
				</table>
			    </td>
			</tr>
			<tr>
			    <td>
				<table width="100%" class="tracklist">
				    <c:forEach var="track" items="${dir.tracks}" varStatus="stat">
					<scilla:playlist name="track" var="playlist"/>
					<tr>
					    <td align="right"><c:out value="${stat.count}"/></td>
					    <c:if test="${dir.numOfArtists > 1}">
						<td>
						    <div class="artist"><c:out value="${track.artist}"/></div>
						</td>
					    </c:if>
					    <c:if test="${dir.numOfPerformers > 1}">
						<td>
						    <div class="performer"><c:out value="${track.performer}"/></div>
						</td>
					    </c:if>
					    <c:if test="${dir.numOfAlbums > 1}">
						<td>
						    <div class="album"><c:out value="${track.album}"/></div>
						</td>
					    </c:if>
					    <td>
						<a class="title" href='<c:out value="${playlist}"/>'>
						    <c:out value="${track.title}"/>
						</a>
					    </td>
					    <c:if test="${dir.numOfRecordingDates > 1}">
						<td>
						    <div class="year"><c:out value="${track.recordingDate}"/></div>
						</td>
					    </c:if>
					    <td align="right">
						<c:out value="${track.time}"/>
					    </td>
					    <td>
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
					<c:if test="${dir.numOfArtists > 1}"><td></td></c:if>
					<c:if test="${dir.numOfPerformers > 1}"><td></td></c:if>
					<c:if test="${dir.numOfAlbums > 1}"><td></td></c:if>
					<td></td>
					<c:if test="${dir.numOfRecordingDates > 1}"><td></td></c:if>
					<td align="right"><c:out value="${dir.totalTime}"/></td>
					<td>
					    <scilla:playlist name="dir" var="playlist"/>
					    <a href='<c:out value="${playlist}"/>'>
						<scilla:img src="images/speaker.png" border="0" alt="Play">
						    <scilla:par key="scale" value="14x14"/>
						</scilla:img>
					    </a>
					</td>
				    </tr>
				</table>
			    </td>
			</tr>
		    </table>
		</div>
	    </c:if>

	    <c:if test="${dir.numOfImages > 0}">
		<div class="images">
		    <c:forEach var="image" items="${dir.images}">
			<scilla:img name="image" var="url" outputtype="jpeg">
			    <scilla:par key="scale" value="1024x768>"/>
			</scilla:img>
			<a href='<c:out value="${url}"/>'>
			    <scilla:img name="image" border="2">
				<scilla:par key="scale" value="75x75"/>
			    </scilla:img>
			</a>
		    </c:forEach>
		</div>
	    </c:if>
	</div>
    </body>
</html>
