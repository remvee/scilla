<%@ page import="java.io.*,java.net.*,java.util.*,javax.servlet.*" %>
<%!

void streamLinks (ServletRequest request, JspWriter out, String path)
throws IOException
{
    String pathEncoded = URLEncoder.encode(path);
    if (request.getRemoteHost().equals("localhost"))
    {
	out.println("<A href=\"m3u.jsp?f=file&d="+pathEncoded+"\">Play</A>");
    }
    else
    {
	out.println("<A href=\"m3u.jsp?f=modem&d="+pathEncoded+"\">Play</A>");
    }
}

%>
<%
    org.scilla.Config scillaConfig = org.scilla.Config.getInstance();
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
    File dir = new File(scillaConfig.getSourceDir()+"/"+path);
    if (dir.isDirectory())
    {
	String[] files = dir.list();
	Arrays.sort(files);
	for (int i = 0; i < files.length; i++)
	{
	    if (files[i].endsWith(".mp3"))
	    {
		org.scilla.util.mp3.ID3v1 tag = null;
		org.scilla.util.mp3.FrameHeader fh = null;
		org.scilla.util.mp3.XingInfo xing = null;
		String n = scillaConfig.getSourceDir()+"/"+path+"/"+files[i];
		File f = new File(n);

		try
		{
		    tag = new org.scilla.util.mp3.ID3v1(f);
		    fh = new org.scilla.util.mp3.FrameHeader(f);
		    fh.close();
		    try
		    {
			xing = new org.scilla.util.mp3.XingInfo(f);
			xing.close();
		    }
		    catch (org.scilla.util.mp3.Mp3Exception e1) { }

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
	    else if (files[i].endsWith(".jpg") || files[i].endsWith(".gif") || files[i].endsWith(".png"))
	    {
		String s = files[i];
		if (s.toLowerCase().indexOf("front") != -1
		    || s.toLowerCase().indexOf("cover") != -1)
		{
		    s = s.replace(' ', '+');
		    background = urlHead+s+"?scale=640x480&modulate=400x400x400";
		}
		imgVec.add(files[i]);
	    }
	    else if (files[i].endsWith(".m3u"))
	    {
		String s = files[i];
		m3uVec.add(files[i]);
	    }
	    else
	    {
		String n = scillaConfig.getSourceDir()+"/"+path+"/"+files[i];
		File f = new File(n);
		if (f.isDirectory())
		{
		    dirVec.add(files[i]);
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
	    org.scilla.util.mp3.ID3v1 tag =
		    (org.scilla.util.mp3.ID3v1) tagHash.get(f);

	    if (tag != null)
	    {
		artist = tag.getArtist();
		album = tag.getAlbum();
		comment = tag.getComment();
		year = tag.getYear();
		if (artist != null) artistHash.put(artist, "X");
		if (album != null) albumHash.put(album, "X");
		if (comment != null) commentHash.put(comment, "X");
		if (year != null) yearHash.put(year, "X");
	    }
	}

%>
	<HTML>
	    <HEAD>
		<TITLE>
		    <%=(artistHash.size() == 1 ? artist : "Various")%>
		    -
		    <%=(albumHash.size() == 1 ? album : "Various")%>
		</TITLE>
	    </HEAD>
<%
	out.print("<BODY" + (background == null ? " BGCOLOR=WHITE>"
		: " BACKGROUND=\""+background+"\">"));
%>

	<UL>
<%
	e = dirVec.elements();
	for (int num = 1; e.hasMoreElements(); num++)
	{
	    String s = (String) e.nextElement();
	    String sEncoded = (path + "/" + s).replace(' ', '+');
%>
	    <LI>
		<A href="mp3.jsp?d=<%=sEncoded%>"> <%=s%> </A>
	    </LI>
<%
	}
%>
	</UL>

	<UL>
<%
	e = m3uVec.elements();
	for (int num = 1; e.hasMoreElements(); num++)
	{
	    String s = (String) e.nextElement();
%>
	    <LI>
		<%=s%>
<%
		streamLinks(request, out, path+"/"+s);
%>
	    </LI>
<%
	}
%>
	</UL>
<%
	if (vec.size() > 0 && dirVec.size() > 0) out.write("<HR>");
	if (vec.size() > 0)
	{
%>
	    <TABLE>
	    <TR><TD> <TABLE WIDTH="100%">
		<TR>
		    <TD align=left>
			<BIG><STRONG>
			    <%=(artistHash.size() == 1 ? artist : "Various")%>
			</STRONG></BIG>
		    </TD>
		    <TD>&nbsp;</TD>
		    <TD align=right rowspan=2>
			<SMALL>
			    <%=(yearHash.size() == 1 ? year : "")%>
			    <BR>
			    <%=(commentHash.size() == 1 ? comment : "")%>
			</SMALL>
		    </TD>
		</TR>
		<TR>
		    <TD align=left>
			<BIG><STRONG>
			    <%=(albumHash.size() == 1 ? album : "Various")%>
			</STRONG></BIG>
		    </TD>
		    <TD>&nbsp;</TD>
		</TR>
	    </TABLE>
	    </TD>

	    <TR><TD> <TABLE WIDTH="100%" BGCOLOR="#EEEEEE" CELLSPACING=5 CELLPADDING=5>
<%
	    // loop to list
	    int tlength = 0;
	    e = vec.elements();
	    for (int num = 1; e.hasMoreElements(); num++)
	    {
		File f = (File) e.nextElement();
		String filepath = path+"/"+f.getName();

		org.scilla.util.mp3.ID3v1 tag =
			(org.scilla.util.mp3.ID3v1) tagHash.get(f);
		org.scilla.util.mp3.FrameHeader fh =
			(org.scilla.util.mp3.FrameHeader) fhHash.get(f);
		org.scilla.util.mp3.XingInfo xing =
			(org.scilla.util.mp3.XingInfo) xingHash.get(f);
		if (xing != null) fh = xing;
		int length = fh.getLength();
		tlength += length;
%>
		<TR><TD><%=num%></TD>
<%
		if (artistHash.size() > 1) out.write("<TD> "+tag.getArtist()+"</TD>");
		if (albumHash.size() > 1) out.write("<TD> "+tag.getAlbum()+"</TD>");
%>
		    <TD>
			<STRONG>
			    <%=tag!=null?tag.getTitle():"null"%>
			</STRONG>
		    </TD>
<%
		if (commentHash.size() > 1) out.write("<TD>"+tag.getComment()+"</TD>");
		if (yearHash.size() > 1) out.write("<TD> "+tag.getYear()+"</TD>");
%>
		    <TD align=right>
			<TT>
			    <%=length/60+":"+(length%60>9?"":"0")+length%60%>
			</TT>
		    </TD>
		    <TD> <FONT size=-2>
<%
		streamLinks(request, out, filepath);
%>
		    </FONT>
		    </TD>
<!--
		    <TD align=right> <FONT size=-5>
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
		</TR>
<%
	    }
%>
	    <TR><TD></TD>
<%
	    if (artistHash.size() > 1) out.write("<TD></TD>");
	    if (albumHash.size() > 1) out.write("<TD></TD>");
	    out.write("<TD></TD>");
	    if (commentHash.size() > 1) out.write("<TD></TD>");
	    if (yearHash.size() > 1) out.write("<TD></TD>");

%>
		    <TD align=right>
			<TT>
			    <%=tlength/60+":"+(tlength%60>9?"":"0")+tlength%60%>
			</TT>
		    </TD>
		    <TD> <FONT size=-2>
<%
	    streamLinks(request, out, path);
%>
		    </FONT> </TD>
		</TR>
	    </TABLE>
	</TABLE>

<%
	}

	if (imgVec.size() > 0) out.print("<HR>");
	e = imgVec.elements();
	for (int num = 1; e.hasMoreElements(); num++)
	{
	    String s = (String) e.nextElement();
	    s = s.replace(' ', '+');
%>
	    <A href="<%=urlHead+s%>"><IMG SRC="<%=urlHead+s%>?scale=75x75"></A>
<%
	}
    }
    else
    {
%>
	<BR><BIG><STRONG>Oeps: not a directory: <%=path%></STRONG></BIG>
<%
    }
%>
</BODY></HTML>
