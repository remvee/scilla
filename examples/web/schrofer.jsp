<%
    String textData = request.getParameter("text");
    if (textData == null || textData.trim().length() == 0)
    {
	textData =  "scilla\n"+
		    "c    l\n"+
		    "i    l\n"+
		    "l    i\n"+
		    "l    c\n"+
		    "allics\n";
    }
    String scaleData = request.getParameter("scale");
    if (scaleData == null || scaleData.trim().length() == 0)
    {
	scaleData = "400";
    }
%>
<HTML>
    <HEAD>
	<TITLE>Schrofer font</TITLE>
    </HEAD>
    <BODY>
<%
    char[] data = textData.toLowerCase().toCharArray();
    for (int i = 0; i < data.length; i++)
    {
	int y = 60; // space character

	if (data[i] >= '0' && data[i] <= '9')
	{
	    y = (int) (data[i] - '0');
	    y *= 6;
	}
	else if (data[i] >= 'a' && data[i] <= 'z')
	{
	    y = (int) (data[i] - 'a');
	    y += 11;
	    y *= 6;
	}

	if (data[i] == '\n')
	{
%>
	<BR>
<%
	}
	else
	{
	    String crop = java.net.URLEncoder.encode("6x6+0+"+y);
	    String scale = java.net.URLEncoder.encode(scaleData+"%x"+scaleData+"%");
	    String params = "outputtype=gif&crop="+crop+"&scale="+scale;
%><IMG src="servlet/scilla/schrofer.xpm?<%=params%>" alt="<%=data[i]%>"><%
	}
    }
%>
	<HR>
	<FORM method="POST">
	    <TEXTAREA name="text" rows="20" cols="80"><%=textData%></TEXTAREA>
	    <BR><SELECT name="scale">
<%
    String[] scaleOptions = {
	"100", "200", "300", "400", "500", "600", "700", "800", "900", "1000"
    };
    for (int i = 0; i < scaleOptions.length; i++)
    {
%>
		<OPTION value="<%=scaleOptions[i]%>" <%=scaleOptions[i].equals(scaleData) ? "selected" : ""%>><%=scaleOptions[i]%>%</OPTION>

<%
    }
%>
	    </SELECT>
	    <BR><INPUT type="submit">
	</FORM>
    </BODY>
</HTML>
