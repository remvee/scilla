/*
 * scilla
 *
 * Copyright (C) 2001  R.W. van 't Veer
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 */

package org.scilla.util.mp3.id3v2;

import java.io.*;
import java.util.*;

/**
 * Representation of link frames (<TT>W000</TT> - <TT>WZZZ</TT>).
 *
 * @author Remco van 't Veer
 * @version $Revision: 1.1 $
 */
public class LinkFrame extends Frame
{
    String url;
    /** for <TT>WXXX</TT> frames */
    String descr = null;

    /**
     * Constructor for a link frame.
     * @param data buffer of tag data
     * @param offset offset in buffer
     * @param minor minor version of tag
     */
    public LinkFrame (byte[] data, int offset, int minor)
    throws Exception
    {
	super(data, offset, minor);

	// optionally get description
	int i = 0;
	if (frameId.equals("WXXX"))
	{
	    String encoding;
	    switch (frameData[i++])
	    {
		case 0: encoding = "ISO-8859-1"; break;
		case 1:
		case 2: encoding = "UTF-16"; break;
		case 3: encoding = "UTF-8"; break;
		default: throw new Exception("text encoding not supported");
	    }

	    // get description
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    while (i < frameData.length && frameData[i] != 0) out.write(frameData[i++]);
	    descr = out.toString(encoding);

	    // skip over zero
	    i++;
	}

	// get url
	StringBuffer sb = new StringBuffer();
	while (i < frameData.length && frameData[i] != 0) sb.append((char) frameData[i++]);
	url = sb.toString();
    }

    /** @return URL of this frame */
    public String getUrl () { return url; }
    /** @return description for this link or <TT>null</TT> */
    public String getDescription () { return descr; }

    public String toString ()
    {
	return frameId.equals("WXXX")
		?  super.toString() + ": \"" + descr + "\" \"" + url + "\""
		: super.toString() + ": \"" + url + "\"";
    }
}
