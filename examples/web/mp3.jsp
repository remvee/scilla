<%@ page import="java.io.*,java.net.*,java.util.*,javax.servlet.*" %>
<%@ page import="org.scilla.*,org.scilla.util.*,org.scilla.util.mp3.*" %>
<%!
    private String formatTime (int length)
    {
	int hours = length / 3600;
	int minutes = (length / 60) % 60;
	int seconds = length % 60;
	return (hours > 0 ? hours + ":" : "")
		+ (hours > 0 && minutes < 10 ? "0" : "") + minutes + ":"
		+ (seconds > 9 ? "" : "0") + seconds;
    }

    void streamLinks (ServletRequest request, JspWriter out, String path)
    throws IOException
    {
	streamLinks (request, out, path, false);
    }

    void streamLinks (ServletRequest request, JspWriter out, String path, boolean recursive)
    throws IOException
    {
	String pathEncoded = URLEncoder.encode(path);
	String encoding = (request.getRemoteHost().equals("localhost")
		|| request.getRemoteHost().equals("127.0.0.1"))
		? ""
		: "&outputtype=mp3&mode=j&resample=16&vbr=1&vbrquality=6&maxbitrate=56";
	String imgSrc = "servlet/scilla/speaker.png?scale=14x14&outputtype=gif";
	out.println("<A href=\"servlet/playlist.m3u"+
		"?d="+pathEncoded+
		(recursive ? "&r=1" : "")+
		encoding+
		"\">"+
		"<IMG src=\""+imgSrc+"\" alt=\"play\" border=0>"+
		"</A>");
    }

    static List indexHtmls = new Vector();
    static
    {
	indexHtmls.add("index.html");
	indexHtmls.add("index.htm");
	indexHtmls.add("default.html");
	indexHtmls.add("default.htm");
	indexHtmls.add("main.html");
	indexHtmls.add("main.htm");
    }
%>
<%
    Config scillaConfig = ConfigProvider.get();
    String source = scillaConfig.getString(Config.SOURCE_DIR_KEY);

    String path = "";
    if (request.getParameter("d") != null) path = request.getParameter("d");
    String urlHead = "servlet/scilla/" + path.replace(' ', '+') + "/";

    String background = null;
    Map tagMap = new HashMap();
    Map fhMap = new HashMap();
    Map xingMap = new HashMap();
    List vec = new Vector();
    List imgVec = new Vector();
    List m3uVec = new Vector();
    List htmVec = new Vector();
    List dirVec = new Vector();
    File dir = new File(source+"/"+path);
    if (dir.isDirectory())
    {
	String[] files = dir.list();
	Arrays.sort(files);
	for (int i = 0; i < files.length; i++)
	{
	    String fname = files[i];
	    String type = MimeType.getTypeFromFilename(fname);

	    if (fname.startsWith(".") || fname.equals("CVS"))
	    {
		continue;
	    }
	    else if (fname.endsWith(".mp3"))
	    {
		ID3v1 tag = null;
		FrameHeader fh = null;
		XingInfo xing = null;
		String n = source+"/"+path+"/"+fname;
		File f = new File(n);

		try
		{
		    tag = new ID3v1(f);
		    fh = new FrameHeader(f);
		    fh.close();
		    try
		    {
			xing = new XingInfo(f);
			xing.close();
		    }
		    catch (Mp3Exception e1) { }

		    if (tag != null) tagMap.put(f, tag);
		    if (fh != null) fhMap.put(f, fh);
		    if (xing != null) xingMap.put(f, xing);

		    vec.add(f);
		}
		catch (Exception it)
		{
		    // ignore this file..
		}
	    }
	    else if (fname.endsWith(".wav"))
	    {
		File f = new File(source+"/"+path+"/"+fname);
		vec.add(f);
	    }
	    else if (type != null && type.startsWith("image/"))
	    {
		String s = fname;
		if (s.toLowerCase().indexOf("front") != -1
		    || s.toLowerCase().indexOf("cover") != -1)
		{
		    s = s.replace(' ', '+');
		    background = urlHead+s+"?scale=300x200&outputtype=jpg";
		}
		imgVec.add(fname);
	    }
	    else if (fname.endsWith(".m3u"))
	    {
		m3uVec.add(fname);
	    }
	    else if (fname.endsWith(".htm") || fname.endsWith(".html"))
	    {
		htmVec.add(fname);
	    }
	    else
	    {
		String n = source+"/"+path+"/"+fname;
		File f = new File(n);
		if (f.isDirectory())
		{
		    dirVec.add(fname);
		}
	    }
	}

	// skip to dir if current only contains 1 dir
	if (vec.size() + imgVec.size() + m3uVec.size() + htmVec.size() == 0
		    && dirVec.size() == 1)
	{
	    String s = (path + "/" + dirVec.get(0)).replace(' ', '+');
	    response.sendRedirect("mp3.jsp?d="+s);
	}

	// redirect to index page if only html files here
	if (htmVec.size() > 0 && vec.size() == 0 && dirVec.size() == 0)
	{
	    String url = "servlet/scilla/"+path+"/";
	    Iterator it = indexHtmls.iterator();
	    while (it.hasNext())
	    {
		String s = (String) it.next();
		if (htmVec.contains(s))
		{
		    response.sendRedirect(url+s);
		    return;
		}
	    }
	}

	// determine table layout
	// count artist, albums and comments
	String artist = null, album = null, comment = null, year = null;
	Set artistSet = new HashSet();
	Set albumSet = new HashSet();
	Set commentSet = new HashSet();
	Set yearSet = new HashSet();
	{
	    Iterator it = vec.iterator();
	    while (it.hasNext())
	    {
		File f = (File) it.next();
		ID3v1 tag = (ID3v1) tagMap.get(f);

		if (tag != null)
		{
		    artist = tag.getArtist();
		    album = tag.getAlbum();
		    comment = tag.getComment();
		    year = tag.getYear();
		    if (artist != null && artist.trim().length() != 0) artistSet.add(artist);
		    if (album != null && album.trim().length() != 0) albumSet.add(album);
		    if (comment != null && comment.trim().length() != 0) commentSet.add(comment);
		    if (year != null && year.trim().length() != 0) yearSet.add(year);
		}
	    }
	}
%>
<HTML>
    <HEAD>
	<TITLE>
	    mp3:
<%
	int artistCount = artistSet.size();
	int albumCount = albumSet.size();
	int commentCount = commentSet.size();
	int yearCount = yearSet.size();

	if (artistCount == 1 && albumCount == 1)
	{
	    if (artist.equals(album))
	    {
%>
	    <%= artist %>
<%
	    }
	    else
	    {
%>
	    <%= artist %> - <%= album %>
<%
	    }
	}
	else if (artistCount > 1 && (albumCount > 1 || albumCount == 0)
		|| artistCount == 0 && albumCount == 0)
	{
%>
	    Miscellaneous
<%
	}
	else if (artistCount == 1)
	{
%>
	    <%= artist %>
<%
	}
	else if (albumCount == 1)
	{
%>
	    <%= album %>
<%
	}
	else
	{
%>
	    Huh?
<%
	}
%>
	</TITLE>
    </HEAD>
<%
	out.print("<BODY" + (background == null ? " bgcolor=\"#EEEEEE\">"
		: " background=\""+background+"\">"));
%>
	<TABLE bgcolor="#FFFFFF" cellpadding=10>
	    <TR>
<%
	int colWidth = 0;

	// subdirectories, htmls, playlists in this directory
	Iterator e1 = dirVec.iterator();
	Iterator e2 = m3uVec.iterator();
	Iterator e3 = htmVec.iterator();
	if (e1.hasNext() || e2.hasNext() || e3.hasNext())
	{
%>
		<TD align="left" valign="top">
		    <TABLE>
<%
	    // subdirectories in this directory
	    while (e1.hasNext())
	    {
		String s = (String) e1.next();
		String sEncoded = (path + "/" + s).replace(' ', '+');
%>
			<TR>
			    <TD>
				<A href="mp3.jsp?d=<%= sEncoded %>"><%= s %></A>/
			    </TD>
			    <TD>
<%
		streamLinks(request, out, path+"/"+s, true);
%>
			    </TD>
			</TR>
<%
	    }

	    // playlists in this directory
	    while (e2.hasNext())
	    {
		String s = (String) e2.next();
%>
			<TR>
			    <TD>
				<%=s.substring(0, s.lastIndexOf('.'))%>
			    </TD>
			    <TD>
<%
		streamLinks(request, out, path+"/"+s);
%>
			    </TD>
			</TR>
<%
	    }

	    // htmls in this directory
	    while (e3.hasNext())
	    {
		String s = (String) e3.next();
		String url = "servlet/scilla/"+path+"/"+s;
%>
			<TR>
			    <TD colspan="2">
				<A href="<%= url %>"><%= s %></A>
			    </TD>
			</TR>
<%
	    }
%>
		    </TABLE>
		</TD>
<%
	    colWidth++;
	}

	// audio tracks in this directory
	if (vec.size() > 0)
	{
%>
		<TD align="left" valign="top">
		    <TABLE>
<%
	    if (artistCount > 0 || albumCount == 1 || yearCount == 1 || commentCount == 1)
	    {
%>
			<TR>
			    <TD>
				<TABLE width="100%">
				    <TR>
					<TD align=left>
					    <BIG><STRONG>
						<%=(artistCount == 1 ?  artist : (artistCount != 0 ? "Various" : ""))%>
					    </STRONG></BIG>
					</TD>
					<TD>&nbsp;</TD>
					<TD align=right rowspan=2>
					    <SMALL>
						<%=(yearCount == 1 ? year : "")%>
						<BR>
						<%=(commentCount == 1 ? comment : "")%>
					    </SMALL>
					</TD>
				    </TR>
				    <TR>
					<TD align=left>
					    <BIG><STRONG>
						<%=(albumCount == 1 && ! album.equals(artist) ? album : "")%>
					    </STRONG></BIG>
					</TD>
					<TD>&nbsp;</TD>
				    </TR>
				</TABLE>
			    </TD>
			</TR>
<%
	    }
%>
			<TR>
			    <TD>
				<TABLE width="100%" bgcolor="#EEEEEE" cellspacing=4 cellpadding=3>
<%
	    // loop through list
	    {
		int tlength = 0;
		Iterator it = vec.iterator();
		for (int num = 1; it.hasNext(); num++)
		{
		    File f = (File) it.next();
		    String filepath = path+"/"+f.getName();

		    ID3v1 tag = (ID3v1) tagMap.get(f);
		    FrameHeader fh = (FrameHeader) fhMap.get(f);
		    XingInfo xing = (XingInfo) xingMap.get(f);
		    if (xing != null) fh = xing;
		    int length = 0;
		    if (fh != null)
		    {
			length = fh.getLength();
			tlength += length;
		    }

		    String artistT = "";
		    String albumT = "";
		    String titleT = "";
		    String commentT = "";
		    String yearT = "";
		    if (tag != null)
		    {
			artistT = tag.getArtist();
			albumT = tag.getAlbum();
			titleT = tag.getTitle();
			commentT = tag.getComment();
			yearT = tag.getYear();

			if ((artistT+albumT+titleT).length() == 0)
			{
			    titleT = f.getName();
			    titleT = titleT.substring(0, titleT.lastIndexOf('.'));
			}
		    }
		    else
		    {
			titleT = f.getName();
			titleT = titleT.substring(0, titleT.lastIndexOf('.'));
		    }

%>
				    <TR>
					<TD align="right"><%=num%></TD>
<%
		    if (artistSet.size() > 1) out.write("<TD> "+artistT+"</TD>");
		    if (albumSet.size() > 1) out.write("<TD> "+albumT+"</TD>");
%>
					<TD>
					    <STRONG>
						<%=titleT%>
					    </STRONG>
					</TD>
<%
		    if (commentSet.size() > 1) out.write("<TD>"+commentT+"</TD>");
		    if (yearSet.size() > 1) out.write("<TD> "+yearT+"</TD>");
%>
					<TD align=right>
					    <TT>
						<%= formatTime(length) %>
					    </TT>
					</TD>
					<TD>
					    <FONT size=-2>
<%
		    streamLinks(request, out, filepath);
%>
					    </FONT>
					</TD>
<%
		    if (fh != null)
		    {
%>
<!--
					<TD align=right>
					    <FONT size=-5>
						<%="MPEG"+fh.mpegVersionToString()%>
						<%=fh.layerToInt()==1?"I":fh.layerToInt()==2?"II":"III"%>
						<%=fh.getBitRate()%>Kbit/s
						<%=fh.getSampleRate()/1000%>Khz
						<%=fh.channelModeToString()%>
						<BR>
						<%=f.length()<1024?f.length():f.length()<1024*1024?f.length()/1024:f.length()/(1024*1024)%>
						<%=f.length()<1024?"bytes":f.length()<1024*1024?"Kbytes":"Mbytes"%>
						<A href="<%=urlHead+f.getName().replace(' ', '+')%>">Download</A>
					    </FONT>
					</TD>
-->
<%
		    }
%>
				    </TR>
<%
		}
%>
				    <TR>
<%
		// padding
		out.write("<TD></TD>");
		if (artistCount > 1) out.write("<TD></TD>");
		if (albumCount > 1) out.write("<TD></TD>");
		out.write("<TD></TD>");
		if (commentCount > 1) out.write("<TD></TD>");
		if (yearCount > 1) out.write("<TD></TD>");
%>
					<TD align=right>
					    <TT>
						<%= formatTime(tlength) %>
					    </TT>
					</TD>
					<TD>
					    <FONT size=-2>
<%
		streamLinks(request, out, path);
%>
					    </FONT>
					</TD>
				    </TR>
				</TABLE>
			    </TD>
			</TR>
		    </TABLE>
		</TD>
<%
		colWidth++;
	    }
	}
%>
	    </TR>
<%
	// images in this directory
	{
	    Iterator it = imgVec.iterator();
	    if (it.hasNext())
	    {
%>
	    <TR>
		<TD colspan="<%= colWidth %>">
		    <TABLE>
<%
		for (int i = 0; it.hasNext(); i++)
		{
		    if (i % 5 == 0)
		    {
			if (i > 0)
			{
%>
			</TR>
<%
			}
%>
			<TR>
<%
		    }
		    String s = (String) it.next();
		    s = s.replace(' ', '+');
%>
			    <TD>
				<A href="<%=urlHead+s%>"><IMG src="<%=urlHead+s%>?scale=75x75&outputtype=jpg" border=0></A>
			    </TD>
<%
		}
%>
		    </TABLE>
		</TD>
	    </TR>
<%
	    }
	}
%>
	</TABLE>
<%
    }
    else
    {
%>
<HTML>
    <HEAD>
	<META http-equiv="refresh" content="5; url=mp3.jsp">
    </HEAD>
    <BODY>
	<H1>Oeps: not a directory: <%=path%></H1>
	<P>
	    will
	    <A href="mp3.jsp">reload to top directory</A>
	    in 10 seconds
	</P>
<%
    }
%>
    </BODY>
</HTML>
