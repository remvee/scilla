<%@ page import="java.net.URLEncoder" %>
<%
    String imagePar = request.getParameter("f");
    imagePar = imagePar != null ? imagePar : "spam.jpg";

    String scalePar = request.getParameter("s");
    scalePar = scalePar != null ? scalePar : "75";
    int scale = Integer.parseInt(scalePar);
    scale = scale > 0 ? scale : 75;

    String tmp ="scilla/" + imagePar + "?outputtype=jpg&sample=";
    String image1 = tmp + scale + "x" + scale;
    String image2 = tmp + scale*2 + "x" + scale*2;
    String image3 = tmp + scale*3 + "x" + scale*3;
    String image9 = tmp + scale*9 + "x" + scale*9;
%>
<html>
    <head>
	<title>Spam transformer</title>
    </head>
    <body background="<%= warp(image9, 0, 0, 0) %>">
	<table cellspacing="0" cellpadding="0">
	    <tr>
		<td><img src="<%= warp(image1, 0, 0, 1) %>"></td>
		<td><img src="<%= warp(image1, 1, 0, 1) %>"></td>
		<td><img src="<%= warp(image1, 2, 0, 1) %>"></td>
		<td rowspan="3"><img src="<%= warp(image3, 3, 0, 3) %>">
		<td><img src="<%= warp(image1, 6, 0, 1) %>"></td>
		<td><img src="<%= warp(image1, 7, 0, 1) %>"></td>
		<td><img src="<%= warp(image1, 8, 0, 1) %>"></td>
	    </tr>

	    <tr>
		<td><img src="<%= warp(image1, 0, 1, 1) %>"></td>
		<td rowspan="2" colspan="2"><img src="<%= warp(image2, 1, 1, 2) %>">
		<td rowspan="2" colspan="2"><img src="<%= warp(image2, 1, 6, 2) %>">
		<td><img src="<%= warp(image1, 8, 1, 1) %>"></td>
	    </tr>

	    <tr>
		<td><img src="<%= warp(image1, 0, 2, 1) %>"></td>
		<td><img src="<%= warp(image1, 8, 2, 1) %>"></td>
	    </tr>

	    <tr>
		<td colspan="3"><img src="<%= warp(image3, 0, 3, 3) %>">
		<td><img src="<%= warp(image3, 3, 3, 3) %>"></td>
		<td colspan="3"><img src="<%= warp(image3, 6, 3, 3) %>">
	    </tr>

	    <tr>
		<td><img src="<%= warp(image1, 0, 6, 1) %>"></td>
		<td rowspan="2" colspan="2"><img src="<%= warp(image2, 1, 6, 2) %>">
		<td rowspan="3"><img src="<%= warp(image3, 3, 6, 3) %>">
		<td rowspan="2" colspan="2"><img src="<%= warp(image2, 6, 6, 2) %>">
		<td><img src="<%= warp(image1, 8, 6, 1) %>"></td>
	    </tr>

	    <tr>
		<td><img src="<%= warp(image1, 0, 7, 1) %>"></td>
		<td><img src="<%= warp(image1, 8, 7, 1) %>"></td>
	    </tr>

	    <tr>
		<td><img src="<%= warp(image1, 0, 8, 1) %>"></td>
		<td><img src="<%= warp(image1, 1, 8, 1) %>"></td>
		<td><img src="<%= warp(image1, 2, 8, 1) %>"></td>
		<td><img src="<%= warp(image1, 6, 8, 1) %>"></td>
		<td><img src="<%= warp(image1, 7, 8, 1) %>"></td>
		<td><img src="<%= warp(image1, 8, 8, 1) %>"></td>
	    </tr>

	    <tr>
		<td colspan="7" style="background-color:white;color:black;text-align:right">
		    Zoom:
		    <a href="spam.jsp?f=<%= URLEncoder.encode(imagePar) %>&s=<%= scale-10 %>">OUT</a>,
		    <a href="spam.jsp?f=<%= URLEncoder.encode(imagePar) %>&s=<%= scale+10 %>">IN</a>
		</td>
	    </tr>
	</table>
    </body>
</html>
<%!
    String warp (String image, int x, int y, int size) {
	String conv = "";
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
	    conv += val != 100 ? "&modulate="+val : "";

	    // swirl
	    val = (int) (360. * (distance / 7));
	    conv += val != 0 ? "&swirl="+val : "";
	}
	return image + conv;
    }
%>
