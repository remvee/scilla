<%@ taglib uri="/WEB-INF/scilla.tld" prefix="scilla" %>
<%
    String textData = request.getParameter("text");
    if (textData == null || textData.trim().length() == 0) {
	textData =  "scilla\n"+
		    "c    l\n"+
		    "i    l\n"+
		    "l    i\n"+
		    "l    c\n"+
		    "allics\n";
    }
    String scaleData = request.getParameter("scale");
    if (scaleData == null || scaleData.trim().length() == 0) {
	scaleData = "400";
    }
%>
<html>
    <head>
	<title>Schrofer font</title>
    </head>
    <body>
	<font size="<%= Integer.parseInt(scaleData) / 100 %>">
<%
    char[] data = textData.toCharArray();
    for (int i = 0; i < data.length; i++) {
	String s = scaleData;
	int y = -1;

	if (data[i] >= '0' && data[i] <= '9') {
	    y = ((int) (data[i] - '0')) * 6;
	} else if (data[i] >= 'a' && data[i] <= 'z') {
	    y = (((int) (data[i] - 'a')) + 11) * 6;
	} else if (data[i] >= 'A' && data[i] <= 'Z') {
	    y = (((int) (data[i] - 'A')) + 11) * 6;
	    s = "" + ((Integer.parseInt(s) * 125) / 100);
	} else if (data[i] == ' ') {
	    y = 60;
	}

	if (data[i] == '\n') {
%><br /><%
	} else if (y != -1) {
	    // use jpeg output because mozilla doesn't handle imagick cropped gif
	    String trans = "crop(6x6+0+"+y+");scale("+s+"%x"+s+"%);outputtype(jpg)";
	    String alt = data[i]+"";
%><scilla:img src="images/schrofer.gif" transform="<%= trans %>" alt="<%= alt %>"/><%
	} else {
%><%= escapeHtml(data[i]+"") %><%
	}
    }
%>
	</font>
	<hr />
	<form method="post">
	    <textarea name="text" rows="20" cols="80"><%= textData %></textarea>
	    <br />
	    <select name="scale">
<%
    String[] scaleOptions = {
	"100", "200", "300", "400", "500", "600", "700", "800", "900", "1000"
    };
    for (int i = 0; i < scaleOptions.length; i++) {
	String value = scaleOptions[i];
	String label = value + "%";
	String selected = value.equals(scaleData) ?  "selected" : "";
%>
		<option value="<%= value %>" <%= selected %>>
		    <%= label %>
		</option>

<%
    }
%>
	    </select>
	    <br /><input type="submit">
	</form>
    </body>
</html>
<%!
    String escapeHtml (String in) {
	StringBuffer sb = new StringBuffer();
	char[] data = in.toCharArray();
	for (int i = 0; i < data.length; i++) {
	    switch (data[i]) {
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
