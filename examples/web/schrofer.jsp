<%!
    static String escapeHtml (String in)
    {
	StringBuffer sb = new StringBuffer();
	char[] data = in.toCharArray();
	for (int i = 0; i < data.length; i++)
	{
	    switch (data[i])
	    {
		case '<':
		    sb.append("&lt;");
		    break;
		case '>':
		    sb.append("&gt;");
		    break;
		case '&':
		    sb.append("&amp;");
		    break;
		default:
		    sb.append(data[i]);
	    }
	}

	return sb.toString();
    }
%>
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
	<FONT size="<%= Integer.parseInt(scaleData) / 100 %>">
<%
    char[] data = textData.toCharArray();
    for (int i = 0; i < data.length; i++)
    {
	String s = scaleData;
	int y = -1;

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
	else if (data[i] >= 'A' && data[i] <= 'Z')
	{
	    y = (int) (data[i] - 'A');
	    y += 11;
	    y *= 6;
	    s = "" + ((Integer.parseInt(s) * 125) / 100);
	}
	else if (data[i] == ' ')
	{
	    y = 60;
	}

	if (data[i] == '\n')
	{
%><BR><%
	}
	else if (y != -1)
	{
	    String crop = java.net.URLEncoder.encode("6x6+0+"+y);
	    String scale = java.net.URLEncoder.encode(s+"%x"+s+"%");
	    String params = "outputtype=gif&crop="+crop+"&scale="+scale;
%><IMG src="scilla/schrofer.xpm?<%=params%>" alt="<%=data[i]%>"><%
	}
	else
	{
%><%= escapeHtml(data[i]+"") %><%
	}
    }
%>
	</FONT>
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
