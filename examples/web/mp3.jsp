<%@ page import="java.io.*,java.net.*,java.util.*,javax.servlet.*" %>
<%@ page import="org.scilla.*,org.scilla.util.*,org.scilla.util.mp3.*" %>
<%!

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
	    ? "file" : "stream";
    String imgSrc = "servlet/sservlet/speaker.png?scale=14x14&outputtype=gif";
    out.println("<A href=\"servlet/playlist.m3u"+
	    "?f="+encoding+
	    "&d="+pathEncoded+
	    (recursive ? "&r=1" : "")+
	    "\">"+
	    "<IMG src=\""+imgSrc+"\" alt=\"play\" border=0>"+
	    "</A>");
}

%>
<%
    Config scillaConfig = Config.getInstance();
    String path = "";
    if (request.getParameter("d") != null) path = request.getParameter("d");
    String urlHead = "servlet/sservlet/" + path.replace(' ', '+') + "/";

    String background = null;
    Hashtable tagHash = new Hashtable();
    Hashtable fhHash = new Hashtable();
    Hashtable xingHash = new Hashtable();
    Vector vec = new Vector();
    Vector imgVec = new Vector();
    Vector m3uVec = new Vector();
    Vector dirVec = new Vector();
    String source = scillaConfig.getSourceDir();
    File dir = new File(source+"/"+path);
    if (dir.isDirectory())
    {
	String[] files = dir.list();
	Arrays.sort(files);
	for (int i = 0; i < files.length; i++)
	{
	    String fname = files[i];
	    String type = MimeTypeFactory.getTypeFromFilename(fname);

	    if (fname.endsWith(".mp3"))
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

		    if (tag != null) tagHash.put(f, tag);
		    if (fh != null) fhHash.put(f, fh);
		    if (xing != null) xingHash.put(f, xing);

		    vec.add(f);
		}
		catch (Exception e)
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
		    background = urlHead+s+"?scale=640x480&outputtype=jpg";
		}
		imgVec.add(fname);
	    }
	    else if (fname.endsWith(".m3u"))
	    {
		m3uVec.add(fname);
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

	// count artist, albums and comments
	String artist = null, album = null, comment = null, year = null;
	Hashtable artistHash = new Hashtable();
	Hashtable albumHash = new Hashtable();
	Hashtable commentHash = new Hashtable();
	Hashtable yearHash = new Hashtable();
	Enumeration e = vec.elements();
	while (e.hasMoreElements())
	{
	    File f = (File) e.nextElement();
	    ID3v1 tag = (ID3v1) tagHash.get(f);

	    if (tag != null)
	    {
		artist = tag.getArtist();
		album = tag.getAlbum();
		comment = tag.getComment();
		year = tag.getYear();
		if (artist != null && artist.trim().length() != 0) artistHash.put(artist, "X");
		if (album != null && album.trim().length() != 0) albumHash.put(album, "X");
		if (comment != null && comment.trim().length() != 0) commentHash.put(comment, "X");
		if (year != null && year.trim().length() != 0) yearHash.put(year, "X");
	    }
	}

%>
<HTML>
    <HEAD>
	<TITLE>
	    mp3:
<%
	int artistCount = artistHash.size();
	int albumCount = albumHash.size();
	int commentCount = commentHash.size();
	int yearCount = yearHash.size();

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

	Enumeration e1 = dirVec.elements();
	Enumeration e2 = m3uVec.elements();
	if (e1.hasMoreElements() || e2.hasMoreElements())
	{
%>
		<TD align="left" valign="top">
		    <TABLE>
<%
	    while (e1.hasMoreElements())
	    {
		String s = (String) e1.nextElement();
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
	    while (e2.hasMoreElements())
	    {
		String s = (String) e2.nextElement();
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
%>
		    </TABLE>
		</TD>
<%
	    colWidth++;
	}
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
				<TABLE width="100%" bgcolor="#EEEEEE" cellspacing=5 cellpadding=5>
<%
	    // loop through list
	    int tlength = 0;
	    e = vec.elements();
	    for (int num = 1; e.hasMoreElements(); num++)
	    {
		File f = (File) e.nextElement();
		String filepath = path+"/"+f.getName();

		ID3v1 tag = (ID3v1) tagHash.get(f);
		FrameHeader fh = (FrameHeader) fhHash.get(f);
		XingInfo xing = (XingInfo) xingHash.get(f);
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
		if (artistHash.size() > 1) out.write("<TD> "+artistT+"</TD>");
		if (albumHash.size() > 1) out.write("<TD> "+albumT+"</TD>");
%>
					<TD>
					    <STRONG>
						<%=titleT%>
					    </STRONG>
					</TD>
<%
		if (commentHash.size() > 1) out.write("<TD>"+commentT+"</TD>");
		if (yearHash.size() > 1) out.write("<TD> "+yearT+"</TD>");
%>
					<TD align=right>
					    <TT>
						<%=length/60+":"+(length%60>9?"":"0")+length%60%>
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
						<%=tlength/60+":"+(tlength%60>9?"":"0")+tlength%60%>
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
%>
	    </TR>
<%

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
				<A href="<%=urlHead+s%>"><IMG SRC="<%=urlHead+s%>?scale=75x75"></A>
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
	<BR><BIG><STRONG>Oeps: not a directory: <%=path%></STRONG></BIG>
<%
    }
%>
    </BODY>
</HTML>
