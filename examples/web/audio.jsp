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
	    <c:if test="${dir.count.audio.artist == 1}">
		<c:out value="${dir.list.audio[0].artist}"/>
	    </c:if>
	    <c:if test="${dir.count.audio.performer == 1}">
		<c:out value="${dir.list.audio[0].performer}"/>
	    </c:if>
	    <c:if test="${dir.count.audio.album == 1}">
		<c:out value="${dir.list.audio[0].album}"/>
	    </c:if>
	</title>
	<link rel="stylesheet" type="text/css" href="audio.css"/>
    </head>
    <body>

	<div class="content">
	    <c:if test="${dir.list.count.directory > 0}">
		<div class="directories">
		    <table>
			<c:forEach var="d" items="${dir.list.directory}">
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

	    <c:if test="${dir.list.count.audio > 0}">
		<div class="tracks">
		    <table>
			<tr>
			    <td>
				<table width="100%" class="trackheader">
				    <tr>
					<td rowspan="2" align="left">
					    <c:if test="${dir.count.audio.artist == 1}">
						<div class="artist"><c:out value="${dir.list.audio[0].artist}"/></div>
					    </c:if>
					    <c:if test="${dir.count.audio.performer == 1}">
						<c:forTokens var="perf" items="${dir.list.audio[0].performer}" delims="/">
						    <div class="performer"><c:out value="${perf}"/></div>
						</c:forTokens>
					    </c:if>
					    <c:if test="${dir.count.audio.album == 1}">
						<div class="album"><c:out value="${dir.list.audio[0].album}"/></div>
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
					    <c:if test="${dir.count.audio.recdate == 1}">
						<div class="year"><c:out value="${dir.list.audio[0].recdate}"/></div>
					    </c:if>
					</td>
				    </tr>
				</table>
			    </td>
			</tr>
			<tr>
			    <td>
			    <table width="100%" class="tracklist">
				    <c:forEach var="track" items="${dir.list.audio}" varStatus="stat">
					<scilla:playlist name="track" var="playlist"/>
					<tr>
					    <td align="right"><c:out value="${stat.count}"/></td>
					    <c:if test="${dir.count.audio.artist > 1}">
						<td>
						    <div class="artist"><c:out value="${track.artist}"/></div>
						</td>
					    </c:if>
					    <c:if test="${dir.count.audio.performer > 1}">
						<td>
						    <div class="performer"><c:out value="${track.performer}"/></div>
						</td>
					    </c:if>
					    <c:if test="${dir.count.audio.album > 1}">
						<td>
						    <div class="album"><c:out value="${track.album}"/></div>
						</td>
					    </c:if>
					    <td>
						<a class="title" href='<c:out value="${playlist}"/>'>
						    <c:out value="${track.title}"/>
						</a>
					    </td>
					    <c:if test="${dir.count.audio.recdate > 1}">
						<td>
						    <div class="year"><c:out value="${track.recdate}"/></div>
						</td>
					    </c:if>
					    <td align="right">
						<c:set var="len" value="${track.length}"/>
						<scilla:time var="len"/>
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
					<c:if test="${dir.count.audio.artist > 1}"><td></td></c:if>
					<c:if test="${dir.count.audio.performer > 1}"><td></td></c:if>
					<c:if test="${dir.count.audio.album > 1}"><td></td></c:if>
					<td></td>
					<c:if test="${dir.count.audio.recdate > 1}"><td></td></c:if>
					<td align="right">
					    <c:set var="len" value="${dir.sum.audio.length}"/>
					    <scilla:time var="len"/>
					</td>
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

	    <c:if test="${dir.list.count.image > 0}">
		<div class="images">
		    <c:forEach var="image" items="${dir.list.image}">
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
