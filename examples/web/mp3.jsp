<%@ taglib uri="/WEB-INF/scilla.tld" prefix="scilla" %>
<%@ page import="java.io.*,java.net.*,java.util.*,javax.servlet.*" %>
<%@ page import="org.scilla.*,org.scilla.util.*" %>
<%@ page import="org.scilla.util.mp3.*,org.scilla.util.mp3.id3v2.*" %>
<%
    Config scillaConfig = ConfigProvider.get();
    String source = scillaConfig.getString(Config.SOURCE_DIR_KEY);

    String path = "";
    if (request.getParameter("d") != null) {
	path = request.getParameter("d");
    }
    String urlHead = "scilla/" + path.replace(' ', '+') + "/";

    String background = null;
    List vec = new Vector();
    AudioList audioList = new AudioList();
    List imgVec = new Vector();
    List m3uVec = new Vector();
    List htmVec = new Vector();
    List dirVec = new Vector();
    File dir = new File(source+"/"+path);
    if (dir.isDirectory()) {
	String[] files = dir.list();
	Arrays.sort(files);
	for (int i = 0; i < files.length; i++) {
	    String fname = files[i];
	    String type = MimeType.getTypeFromFilename(fname);

	    if (fname.startsWith(".") || fname.equals("CVS")) {
		continue;
	    } else if (fname.endsWith(".mp3")) {
		File f = new File(source+"/"+path+"/"+fname);
		audioList.add(new AudioFile(f));
		vec.add(f);
	    } else if (fname.endsWith(".wav")) {
		File f = new File(source+"/"+path+"/"+fname);
		vec.add(f);
	    } else if (type != null && type.startsWith("image/")) {
		String s = fname;
		if (s.toLowerCase().indexOf("front") != -1
		    || s.toLowerCase().indexOf("cover") != -1) {
		    s = s.replace(' ', '+');
		    background = urlHead+s+"?scale=300x200&outputtype=jpg";
		}
		imgVec.add(fname);
	    } else if (fname.endsWith(".m3u")) {
		m3uVec.add(fname);
	    } else if (fname.endsWith(".htm") || fname.endsWith(".html")) {
		htmVec.add(fname);
	    } else {
		File f = new File(source+"/"+path+"/"+fname);
		if (f.isDirectory()) {
		    dirVec.add(fname);
		}
	    }
	}

	// no background picture select add only one found; use it
	if (background == null && imgVec.size() == 1) {
	    String s = ((String)imgVec.get(0)).replace(' ', '+');
	    background = urlHead+s+"?scale=300x200&outputtype=jpg";
	}

	// skip to dir if current only contains 1 dir
	if (vec.size() + imgVec.size() + m3uVec.size() + htmVec.size() == 0
		    && dirVec.size() == 1) {
	    String s = (path + "/"+dirVec.get(0)).replace(' ', '+');
	    response.sendRedirect("mp3.jsp?d="+s);
	}

	// redirect to index page if only html files here
	if (htmVec.size() > 0 && vec.size() == 0 && dirVec.size() == 0) {
	    String url = "scilla/"+path+"/";
	    Iterator it = indexHtmls.iterator();
	    while (it.hasNext()) {
		String s = (String) it.next();
		if (htmVec.contains(s)) {
		    response.sendRedirect(url+s);
		    return;
		}
	    }
	}

%>
<HTML>
    <HEAD>
	<TITLE>
<%
	if (audioList.count("TPE1") != 0 && audioList.count("TALB") != 0) {
%>
	    mp3: <%= audioList.getProp("TPE1") %> - <%= audioList.getProp("TALB") %>
<%
	} else {
%>
	    mp3: <%= path %>/
<%
	}
%>
	</TITLE>
	<LINK rel="stylesheet" type="text/css" href="mp3.css"/>
    </HEAD>
    <%= "<BODY "+(background == null?"bgcolor=\"#EEEEEE":"background=\""+background)+"\">" %>
        <TABLE bgcolor="#FFFFFF" cellpadding=10>
<%
	int colWidth = 0;

	// subdirectories, htmls, playlists in this directory
	{
	    if (dirVec.size() + htmVec.size() + m3uVec.size() > 0) {
%>
	    <TR>
		<TD>
		    <TABLE>
<%
		// subdirectories
		{
		    int cols = 3;
		    int rows = (dirVec.size() + cols - 1) / cols;
		    int len = dirVec.size();
		    for (int y = 0; y < rows; y++) {
%>
			<TR>
<%
			for (int x = 0; x < cols; x++) {
			    int i = (x * rows) + y;
			    if (i >= len) {
				continue;
			    }

			    String p = (String) dirVec.get(i);
			    String s = p;
			    String sEnc = (path + "/" + p).replace(' ', '+');
			    if (s.length() > 15) {
				s = s.substring(0, 15)+"..";
			    }
%>
			    <TD align="left">
				<A href="mp3.jsp?d=<%= sEnc %>"><%= s %></A>/&nbsp;
			    </TD>
			    <TD align="right">
				<A href="<%= streamLink(request, path+"/"+p, true) %>"><scilla:img src="images/speaker.png" transform="scale(14x14)" border="0" alt="Play"/></A>
				&nbsp;&nbsp;&nbsp;
			    </TD>
<%
			}
%>
			</TR>
<%
		    }
		}
		// html files
		{
		    int cols = 4;
		    int rows = (htmVec.size() + cols - 1) / cols;
		    int len = htmVec.size();
		    for (int y = 0; y < rows; y++) {
%>
			<TR>
<%
			for (int x = 0; x < cols; x++) {
			    int i = (x * rows) + y;
			    if (i >= len) {
				continue;
			    }

			    String s = (String) htmVec.get(i);
			    String sEnc = s.replace(' ', '+');
%>
                           <TD colspan="2">
				<A href="<%= urlHead+sEnc %>"><%= s %></A>&nbsp;&nbsp;&nbsp;
                           </TD>
<%
			}
%>
			</TR>
<%
		    }
		}
%>
		    </TABLE>
		</TD>
	    </TR>
<%
	    }
	}

	// audio tracks in this directory
	if (vec.size() > 0) {
%>
	    <TR>
		<TD align="left" valign="top">
		    <TABLE>
<%
	    Object artist = audioList.getProp("TPE1");
	    Object album = audioList.getProp("TALB");
	    Object year = audioList.getProp("TYER");
	    Object comment = audioList.getProp("TPE3") != null
		    ?  audioList.getProp("TPE3") : audioList.getProp("XCOM");

	    int artistCount = audioList.count("TPE1");
	    int albumCount = audioList.count("TALB");
	    int yearCount = audioList.count("TYER");
	    int commentCount = audioList.count("XCOM");
	    if (artistCount > 0 || albumCount == 1 || yearCount == 1 || commentCount == 1) {
		Object o = null;
		String txt = null;
%>
			<TR>
			    <TD>
				<TABLE width="100%">
				    <TR>
					<TD valign=top align=left>
<%
		if (audioList.count("TCOM") == 1) {
%>
					    <BIG><EM> <%= toHTML(audioList.getProp("TCOM")) %> </EM></BIG>
<%
		}
		o = albumCount == 1 && ! album.equals(artist) ? album : "";
%>
					    <BIG><STRONG> <%= toHTML(o) %> </STRONG></BIG>
					    <A href="<%= streamLink(request, path, false) %>"><scilla:img src="images/speaker.png" transform="scale(14x14)" border="0" alt="Play"/></A>
					</TD>
					<TD valign=top align=right>
<%
		o = yearCount == 1 ? year : "";
%>
					    <SMALL> <%= toHTML(o) %> </SMALL>
					</TD>
				    </TR>
				    <TR>
					<TD valign=bottom align=left>
<%
		o = artistCount == 1 ? artist : (artistCount != 0 ? "Various" : "");
%>
					    <SMALL> <%= toHTML(o) %> </SMALL>
					</TD>
					<TD valign=bottom align=right>
<%
		o = commentCount == 1 ? comment : "";
%>
					    <SMALL> <%= toHTML(o) %> </SMALL>
					</TD>
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
		List columnList = new Vector();
		if (audioList.count("TPE1") > 1) columnList.add("TPE1");
		if (audioList.count("TALB") > 1) columnList.add("TALB");
		if (audioList.count("TCOM") > 1) columnList.add("TCOM");
		if (audioList.count("TIT1") > 1) columnList.add("TIT1");
		columnList.add("TIT2");
		if (audioList.count("TIT3") > 1) columnList.add("TIT3");
		if (audioList.count("TYER") > 1) columnList.add("TYER");

		int tlength = 0;
		Iterator it = audioList.iterator();
		Map lastMap = new HashMap();
		for (int num = 1; it.hasNext(); num++) {
		    AudioFile f = (AudioFile) it.next();
		    String filepath = path+"/"+f.getName();
%>
				    <TR>
					<TD valign="top" align="right" class="num"><%=num%></TD>
<%
		    Iterator cit = columnList.iterator();
		    while (cit.hasNext()) {
			String key = (String) cit.next();
			Object prop = f.getProp(key);
			Object last = lastMap.get(key);
			lastMap.put(key, prop);
			boolean isSameAsLast = last != null && last.equals(prop);
			boolean isEmpty = prop == null || prop.toString().length() == 0;
			if (isEmpty) {
%>
					<TD valign="top" class="<%= key %>"></TD>
<%
			} else if (isSameAsLast) {
%>
					<TD valign="top" class="<%= key %>"><SMALL>,,</SMALL></TD>
<%
			} else {
%>
					<TD valign="top" class="<%= key %>"><%= toHTML(prop) %></TD>
<%
			}
		    }

		    int length = ((Integer)f.getProp("playlength")).intValue();
		    tlength += length;
%>
					<TD valign="top" align="right" class="playlength">
					    <TT>
						<%= formatTime(length) %>
					    </TT>
					</TD>
					<TD valign="top">
					    <FONT size=-2>
					    <A href="<%= streamLink(request, filepath, false) %>"><scilla:img src="images/speaker.png" transform="scale(14x14)" border="0" alt="Play"/></A>
					    </FONT>
					</TD>
				    </TR>
<%
		}
%>
				    <TR>
<%
		// padding
		out.write("<TD></TD>");
		for (int i = 0; i < columnList.size(); i++) out.write("<TD></TD>");
%>
					<TD align=right>
					    <TT>
						<%= formatTime(tlength) %>
					    </TT>
					</TD>
					<TD>
					    <FONT size=-2>
						<A href="<%= streamLink(request, path, false) %>"><scilla:img src="images/speaker.png" transform="scale(14x14)" border="0" alt="Play"/></A>
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
	}
	// images in this directory
	{
	    Iterator it = imgVec.iterator();
	    if (it.hasNext()) {
%>
	    <TR>
		<TD>
		    <TABLE>
<%
		for (int i = 0; it.hasNext(); i++) {
		    if (i % 5 == 0) {
			if (i > 0) {
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

    String streamLink (ServletRequest request, String path, boolean recursive) {
	String remote = request.getRemoteHost();
	String encoding = (remote.equals("localhost") || remote.equals("127.0.0.1"))
		? "" : "&outputtype=mp3&mode=j&resample=16&vbr=1&vbrquality=6&maxbitrate=56";
	return "playlist.m3u?d="+URLEncoder.encode(path)+(recursive ? "&r=1" : "")+encoding;
    }

    String toHTML (Object in)
    {
	if (in == null) return "";

	StringBuffer out = new StringBuffer();
	if (in instanceof List) {
	    Iterator it = ((List) in).iterator();
	    while (it.hasNext()) {
		out.append(it.next()+"");
		if (it.hasNext()) out.append("<BR>");
	    }
	} else {
	    out.append(in);
	}
	// TODO XML escape!!
	return out.toString();
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

    class AudioFile
    {
	ID3v1 tag1;
	ID3v2 tag2;
	FrameHeader fh;
	XingInfo xing;
	Map props = new HashMap();
	String name;

	AudioFile (File f)
	throws Exception {
	    name = f.getName();

	    // get MP3 frameheader
	    try { fh = new FrameHeader(f); }
	    catch (Exception ex) { throw new Exception("NOT A MP3 FILE!"); }
	    finally { if (fh != null) fh.close(); }

	    // "XING" frameheader
	    try { xing = new XingInfo(f); }
	    catch (Exception ex) { /* ignore */ }
	    finally { if (xing != null) xing.close(); }
	    if (xing != null) fh = xing;

	    // get ID3 tags
	    tag1 = new ID3v1(f);
	    tag2 = new ID3v2(f);

	    // add length property
	    props.put("playlength", new Integer(fh.getLength()));

	    // get properties from ID3v1 tag
	    props.put("TPE1", tag1.getArtist());
	    props.put("TALB", tag1.getAlbum());
	    props.put("XCOM", tag1.getComment());
	    props.put("TIT2", tag1.getTitle());
	    props.put("TYER", tag1.getYear());
	    if (tag2.hasTag()) {
		// get properties from ID3v2 tag
		Object o = null;
		o = getTextFrame("TPE1"); if (o != null) props.put("TPE1", o);
		o = getTextFrame("TPE2"); if (o != null) props.put("TPE2", o);
		o = getTextFrame("TPE3"); if (o != null) props.put("TPE3", o);
		o = getTextFrame("TALB"); if (o != null) props.put("TALB", o);
		o = getTextFrame("TIT1"); if (o != null) props.put("TIT1", o);
		o = getTextFrame("TIT2"); if (o != null) props.put("TIT2", o);
		o = getTextFrame("TIT3"); if (o != null) props.put("TIT3", o);
		o = getTextFrame("TYER"); if (o != null) props.put("TYER", o);
		o = getTextFrame("TCOM"); if (o != null) props.put("TCOM", o);
	    }
	}

	String getName() { return name; }

	Object getTextFrame (String id) {
	    TextFrame f = (TextFrame) tag2.getFrame(id);
	    if (f == null) return null;

	    String t = f.getText();
	    if (t.indexOf('/') == -1) return t;

	    List v = new Vector();
	    StringTokenizer st = new StringTokenizer(t, "/");
	    while (st.hasMoreTokens()) v.add(st.nextToken().trim());
	    return v;
	}

	Object getProp (String key) { return props.get(key); }
	Set getKeySet () { return props.keySet(); }
    }

    class AudioList extends Vector {
	AudioList () { super(); }

	Set keySet = null;
	Set getKeySet () {
	    if (keySet == null)
	    {
		keySet = new HashSet();

		Iterator it = iterator();
		while (it.hasNext())
		{
		    AudioFile f = (AudioFile) it.next();
		    keySet.addAll(f.getKeySet());
		}
	    }
	    return keySet;
	}

	Map countMap = new HashMap();
	int count (String key) {
	    if (! getKeySet().contains(key)) return 0;

	    Integer cached = (Integer) countMap.get(key);
	    if (cached != null) return cached.intValue();

	    Set set = new HashSet();
	    Iterator it = iterator();
	    while (it.hasNext())
	    {
		AudioFile f = (AudioFile) it.next();
		set.add(f.getProp(key));
	    }

	    countMap.put(key, new Integer(set.size()));
	    return set.size();
	}

	Map propMap = new HashMap();
	Object getProp (String key) {
	    if (! getKeySet().contains(key)) return null;

	    Object cached = (Object) propMap.get(key);
	    if (cached != null) return cached;

	    Object last = null;
	    Set set = new HashSet();
	    Iterator it = iterator();
	    while (it.hasNext())
	    {
		AudioFile f = (AudioFile) it.next();
		last = (Object) f.getProp(key);
		set.add(last);
	    }

	    Object r = set.size() > 1 ? set : last;
	    propMap.put(key, r);
	    return r;
	}
    }
%>
