<%@ taglib uri="/WEB-INF/scilla.tld" prefix="scilla" %>
<%@ page import="java.io.*,java.net.*,java.util.*,javax.servlet.*" %>
<%@ page import="org.scilla.*,org.scilla.util.*" %>
<%@ page import="org.scilla.info.*" %>
<%
    Config scillaConfig = ConfigProvider.get();
    String source = scillaConfig.getString(Config.SOURCE_DIR_KEY);

    String path = "";
    if (request.getParameter("d") != null) {
	path = request.getParameter("d");
    }
    String urlHead = "scilla/" + path.replace(' ', '+') + "/";

    String background = null;
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
	    } else if (type != null && type.startsWith("audio/")) {
		audioList.add(new AudioFile(fname, source+"/"+path+"/"+fname));
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
	if (audioList.size() + imgVec.size() + m3uVec.size() + htmVec.size() == 0
		    && dirVec.size() == 1) {
	    String s = (path + "/"+dirVec.get(0)).replace(' ', '+');
	    response.sendRedirect("mp3.jsp?d="+s);
	}

	// redirect to index page if only html files here
	if (htmVec.size() > 0 && audioList.size() == 0 && dirVec.size() == 0) {
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
	if (audioList.count(AudioInfo.PERFORMER) != 0 && audioList.count(AudioInfo.ALBUM) != 0) {
%>
	    mp3: <%= audioList.getProp(AudioInfo.PERFORMER) %> - <%= audioList.getProp(AudioInfo.ALBUM) %>
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
	if (audioList.size() > 0) {
%>
	    <TR>
		<TD align="left" valign="top">
		    <TABLE>
<%
	    Object artist = audioList.getProp(AudioInfo.PERFORMER);
	    Object album = audioList.getProp(AudioInfo.ALBUM);
	    Object year = audioList.getProp(AudioInfo.RECORDING_DATE);
	    Object comment = audioList.getProp(AudioInfo.CONDUCTOR) != null
		    ?  audioList.getProp(AudioInfo.CONDUCTOR) : audioList.getProp(AudioInfo.COMMENT);

	    int artistCount = audioList.count(AudioInfo.PERFORMER);
	    int albumCount = audioList.count(AudioInfo.ALBUM);
	    int yearCount = audioList.count(AudioInfo.RECORDING_DATE);
	    int commentCount = audioList.count(AudioInfo.COMMENT);
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
		if (audioList.count(AudioInfo.ARTIST) == 1) {
%>
					    <BIG><EM><%= toHTML(audioList.getProp(AudioInfo.ARTIST)) %></EM></BIG>
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
		if (audioList.count(AudioInfo.PERFORMER) > 1) columnList.add(AudioInfo.PERFORMER);
		if (audioList.count(AudioInfo.ALBUM) > 1) columnList.add(AudioInfo.ALBUM);
		if (audioList.count(AudioInfo.ARTIST) > 1) columnList.add(AudioInfo.ARTIST);
		if (audioList.count(AudioInfo.SECTION) > 1) columnList.add(AudioInfo.SECTION);
		columnList.add(AudioInfo.TITLE);
		if (audioList.count(AudioInfo.SUBTITLE) > 1) columnList.add(AudioInfo.SUBTITLE);
		if (audioList.count(AudioInfo.RECORDING_DATE) > 1) columnList.add(AudioInfo.RECORDING_DATE);

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

		    int length = f.getLength();
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
	String encoding = "";
	if (! (remote.equals("localhost") || remote.equals("127.0.0.1"))) {
	    if (request.getParameter("ogg") != null) {
		encoding = "&t=ogg&bitrate=24&maxbitrate=56";
	    } else {
		encoding = "&t=mp3&resample=16&bitrate=24&maxbitrate=56";
	    }
	}
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

    class AudioFile {
	String name, fname;
	AudioInfo info;

	AudioFile (String name, String fname)
	throws Exception {
	    this.name = name;
	    this.fname = (new File(fname)).getCanonicalPath();
	    info = (AudioInfo) InfoFactory.get(this.fname);
	}

	String getName() {
	    return name;
	}

	Object getProp (String key) {
	    // HACK to pass multi value id3v2 values
	    String t = info.getString(key);
	    if (t != null && t.indexOf('/') != -1) {
		List l = new Vector();
		for (StringTokenizer st = new StringTokenizer(t, "/"); st.hasMoreTokens();) {
		    l.add(st.nextToken());
		}
		return l;
	    }

	    return info.getString(key);
	}

	int getLength () {
	    return info.getLength();
	}
    }

    class AudioList extends Vector {
	Map countMap = new HashMap();
	int count (String key) {
	    Integer cached = (Integer) countMap.get(key);
	    if (cached != null) return cached.intValue();

	    Set set = new HashSet();
	    Iterator it = iterator();
	    while (it.hasNext()) {
		AudioFile f = (AudioFile) it.next();
		Object o = f.getProp(key);
		if (o != null) {
		    set.add(o);
		}
	    }

	    countMap.put(key, new Integer(set.size()));
	    return set.size();
	}

	Map propMap = new HashMap();
	Object getProp (String key) {
	    Object cached = (Object) propMap.get(key);
	    if (cached != null) {
		return cached;
	    }

	    Object last = null;
	    Set set = new HashSet();
	    Iterator it = iterator();
	    while (it.hasNext()) {
		AudioFile f = (AudioFile) it.next();
		last = f.getProp(key);
		set.add(last);
	    }

	    Object r = set.size() > 1 ? set : last;
	    propMap.put(key, r);
	    return r;
	}
    }
%>
