<%@ taglib uri="/WEB-INF/scilla.tld" prefix="scilla" %>
<%
    String scalePar = request.getParameter("s");
    scalePar = scalePar != null ? scalePar : "75";
    int scale = Integer.parseInt(scalePar);
    scale = scale > 0 ? scale : 75;
%>
<html>
    <head>
	<title>Spam transformer</title>
    </head>
    <body> <!--  background="<-= warp(image9, 0, 0, 0) %>"> -->
	<table cellspacing="0" cellpadding="0">
	    <tr>
		<td><scilla:img src="images/spam.jpg" transform="<%= warp(scale, 0, 0, 1) %>"/></td>
		<td><scilla:img src="images/spam.jpg" transform="<%= warp(scale, 1, 0, 1) %>"/></td>
		<td><scilla:img src="images/spam.jpg" transform="<%= warp(scale, 2, 0, 1) %>"/></td>
		<td rowspan="3"><scilla:img src="images/spam.jpg" transform="<%= warp(scale, 3, 0, 3) %>"/></td>
		<td><scilla:img src="images/spam.jpg" transform="<%= warp(scale, 6, 0, 1) %>"/></td>
		<td><scilla:img src="images/spam.jpg" transform="<%= warp(scale, 7, 0, 1) %>"/></td>
		<td><scilla:img src="images/spam.jpg" transform="<%= warp(scale, 8, 0, 1) %>"/></td>
	    </tr>

	    <tr>
		<td><scilla:img src="images/spam.jpg" transform="<%= warp(scale, 0, 1, 1) %>"/></td>
		<td rowspan="2" colspan="2"><scilla:img src="images/spam.jpg" transform="<%= warp(scale, 1, 1, 2) %>"/></td>
		<td rowspan="2" colspan="2"><scilla:img src="images/spam.jpg" transform="<%= warp(scale, 1, 6, 2) %>"/></td>
		<td><scilla:img src="images/spam.jpg" transform="<%= warp(scale, 8, 1, 1) %>"/></td>
	    </tr>

	    <tr>
		<td><scilla:img src="images/spam.jpg" transform="<%= warp(scale, 0, 2, 1) %>"/></td>
		<td><scilla:img src="images/spam.jpg" transform="<%= warp(scale, 8, 2, 1) %>"/></td>
	    </tr>

	    <tr>
		<td colspan="3"><scilla:img src="images/spam.jpg" transform="<%= warp(scale, 0, 3, 3) %>"/></td>
		<td><scilla:img src="images/spam.jpg" transform="<%= warp(scale, 3, 3, 3) %>"/></td>
		<td colspan="3"><scilla:img src="images/spam.jpg" transform="<%= warp(scale, 6, 3, 3) %>"/></td>
	    </tr>

	    <tr>
		<td><scilla:img src="images/spam.jpg" transform="<%= warp(scale, 0, 6, 1) %>"/></td>
		<td rowspan="2" colspan="2"><scilla:img src="images/spam.jpg" transform="<%= warp(scale, 1, 6, 2) %>"/></td>
		<td rowspan="3"><scilla:img src="images/spam.jpg" transform="<%= warp(scale, 3, 6, 3) %>"/></td>
		<td rowspan="2" colspan="2"><scilla:img src="images/spam.jpg" transform="<%= warp(scale, 6, 6, 2) %>"/></td>
		<td><scilla:img src="images/spam.jpg" transform="<%= warp(scale, 8, 6, 1) %>"/></td>
	    </tr>

	    <tr>
		<td><scilla:img src="images/spam.jpg" transform="<%= warp(scale, 0, 7, 1) %>"/></td>
		<td><scilla:img src="images/spam.jpg" transform="<%= warp(scale, 8, 7, 1) %>"/></td>
	    </tr>

	    <tr>
		<td><scilla:img src="images/spam.jpg" transform="<%= warp(scale, 0, 8, 1) %>"/></td>
		<td><scilla:img src="images/spam.jpg" transform="<%= warp(scale, 1, 8, 1) %>"/></td>
		<td><scilla:img src="images/spam.jpg" transform="<%= warp(scale, 2, 8, 1) %>"/></td>
		<td><scilla:img src="images/spam.jpg" transform="<%= warp(scale, 6, 8, 1) %>"/></td>
		<td><scilla:img src="images/spam.jpg" transform="<%= warp(scale, 7, 8, 1) %>"/></td>
		<td><scilla:img src="images/spam.jpg" transform="<%= warp(scale, 8, 8, 1) %>"/></td>
	    </tr>

	    <tr>
		<td colspan="7" style="background-color:white;color:black;text-align:right">
		    Zoom:
		    <a href="spam.jsp?s=<%= scale-10 %>">OUT</a>,
		    <a href="spam.jsp?s=<%= scale+10 %>">IN</a>
		</td>
	    </tr>
	</table>
    </body>
</html>
<%!
    String warp (int scale, int x, int y, int size) {
	String conv = "scale(" + (size * scale) + "x" + (size * scale) + ")";
	if (size != 0) {
	    double xpos = (double) x + (double) size / 2;
	    double ypos = (double) y + (double) size / 2;
	    xpos -= 4.5;
	    ypos -= 4.5;

	    // a^2 = b^2 + c^2;
	    double distance = Math.sqrt(Math.pow(xpos, 2) + Math.pow(ypos, 2));

	    int val;

	    // modulate
	    val = 50 + (int) (100. * (1 - (distance / 7)));
	    conv += val != 100 ? ";modulate("+val+")" : "";

	    // swirl
	    val = (int) (360. * (distance / 7));
	    conv += val != 0 ? ";swirl("+val+")" : "";
	}
	return conv;
    }
%>
