<%!
    String warp (String image, int x, int y, int size)
    {
	String conv = "";
	if (size != 0)
	{
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

<%
    String imagePar = request.getParameter("f");
    imagePar = imagePar != null ? imagePar : "spam.jpg";
    String image = imagePar;
    image = "servlet/sservlet/" + (image != null ? image : "spam.jpg");
    image = image + "?outputtype=jpg";

    int scale = 75;
    String scalePar = request.getParameter("s");
    scalePar = scalePar != null ? scalePar : "75";
    if (scalePar != null)
    {
	scale = Integer.parseInt(scalePar);
	scale = scale > 0 ? scale : 75;
    }

    String tmp = image + "&sample=";
    String image1 = tmp + scale + "x" + scale;
    String image2 = tmp + scale*2 + "x" + scale*2;
    String image3 = tmp + scale*3 + "x" + scale*3;
    String image9 = tmp + scale*9 + "x" + scale*9;
%>
<HTML>
<BODY BACKGROUND="<%=warp(image9, 0, 0, 0)%>">

<TABLE CELLSPACING=0 CELLPADDING=0>

    <TR>
    <TD> <TD> <TD> <TD> <TD> <TD> <TD> <TD> <TD></TD>
    </TR>

    <TR>
	<TD><IMG SRC="<%=warp(image1, 0, 0, 1)%>"></TD>
	<TD><IMG SRC="<%=warp(image1, 1, 0, 1)%>"></TD>
	<TD><IMG SRC="<%=warp(image1, 2, 0, 1)%>"></TD>
	<TD ROWSPAN=3><IMG SRC="<%=warp(image3, 3, 0, 3)%>">
	<TD><IMG SRC="<%=warp(image1, 6, 0, 1)%>"></TD>
	<TD><IMG SRC="<%=warp(image1, 7, 0, 1)%>"></TD>
	<TD><IMG SRC="<%=warp(image1, 8, 0, 1)%>"></TD>
    </TR>

    <TR>
	<TD><IMG SRC="<%=warp(image1, 0, 1, 1)%>"></TD>
	<TD ROWSPAN=2 COLSPAN=2><IMG SRC="<%=warp(image2, 1, 1, 2)%>">
	<TD ROWSPAN=2 COLSPAN=2><IMG SRC="<%=warp(image2, 1, 6, 2)%>">
	<TD><IMG SRC="<%=warp(image1, 8, 1, 1)%>"></TD>
    </TR>

    <TR>
	<TD><IMG SRC="<%=warp(image1, 0, 2, 1)%>"></TD>
	<TD><IMG SRC="<%=warp(image1, 8, 2, 1)%>"></TD>
    </TR>

    <TR>
	<TD COLSPAN=3><IMG SRC="<%=warp(image3, 0, 3, 3)%>">
	<TD><IMG SRC="<%=warp(image3, 3, 3, 3)%>"></TD>
	<TD COLSPAN=3><IMG SRC="<%=warp(image3, 6, 3, 3)%>">
    </TR>

    <TR>
	<TD><IMG SRC="<%=warp(image1, 0, 6, 1)%>"></TD>
	<TD ROWSPAN=2 COLSPAN=2><IMG SRC="<%=warp(image2, 1, 6, 2)%>">
	<TD ROWSPAN=3><IMG SRC="<%=warp(image3, 3, 6, 3)%>">
	<TD ROWSPAN=2 COLSPAN=2><IMG SRC="<%=warp(image2, 6, 6, 2)%>">
	<TD><IMG SRC="<%=warp(image1, 8, 6, 1)%>"></TD>
    </TR>

    <TR>
	<TD><IMG SRC="<%=warp(image1, 0, 7, 1)%>"></TD>
	<TD><IMG SRC="<%=warp(image1, 8, 7, 1)%>"></TD>
    </TR>

    <TR>
	<TD><IMG SRC="<%=warp(image1, 0, 8, 1)%>"></TD>
	<TD><IMG SRC="<%=warp(image1, 1, 8, 1)%>"></TD>
	<TD><IMG SRC="<%=warp(image1, 2, 8, 1)%>"></TD>
	<TD><IMG SRC="<%=warp(image1, 6, 8, 1)%>"></TD>
	<TD><IMG SRC="<%=warp(image1, 7, 8, 1)%>"></TD>
	<TD><IMG SRC="<%=warp(image1, 8, 8, 1)%>"></TD>
    </TR>

</TABLE>

Zoom:
<A HREF="spam.jsp?f=<%=java.net.URLEncoder.encode(imagePar)%>&s=<%=scale-10%>">OUT</A>,
<A HREF="spam.jsp?f=<%=java.net.URLEncoder.encode(imagePar)%>&s=<%=scale+10%>">IN</A>

</BODY></HTML>
