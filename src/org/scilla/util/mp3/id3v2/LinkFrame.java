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
 * @version $Revision: 1.5 $
 */
public class LinkFrame extends Frame
{
    String url;
    /** for <TT>WXXX</TT> frames */
    String descr = null;
    String enc;

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
	    boolean isUnicode = false;
	    switch (frameData[i++])
	    {
		case 0: enc = "ISO-8859-1"; break;
		case 1:
		case 2: enc = "UTF-16"; isUnicode = true; break;
		case 3: enc = "UTF-8"; break;
		default: throw new RuntimeException("text encoding not supported");
	    }

	    // get description
	    int j = TextFrame.nextStringTerminator(frameData, i, isUnicode);
	    descr = new String(frameData, i, j - i, enc);
	    i = j + (isUnicode ? 2 : 1);
	}

	// get url
	int j = TextFrame.nextStringTerminator(frameData, i, false);
	url = new String(frameData, i, j - i);
    }

    /**
     * Constructor for creating a link frame.
     * @param id frame identifier
     * @param url URL for this frame
     */
    public LinkFrame (String id, String url)
    {
	frameId = id;
	this.descr = null;
	this.url = url;
    }

    /**
     * Contructor for <TT>WXXX</TT> links.
     * @param id typically <TT>WXXX</TT>
     * @param descr link description
     * @param enc description encoding
     * @param url URL for this frame
     */
    public LinkFrame (String id, String descr, String enc, String url)
    {
	frameId = id;
	this.descr = descr;
	this.enc = enc;
	this.url = url;
    }

    /** @return URL of this frame */
    public String getUrl () { return url; }
    /** @return description for this link or <TT>null</TT> */
    public String getDescription () { return descr; }

    public byte[] getBytes ()
    throws UnsupportedEncodingException
    {
	int i = 0;
	byte[] result;
	byte[] urlData = url.getBytes();

	if (descr != null)
	{
	    int encId = 0;
	    if (enc.equals("ISO-8859-1")) encId = 0;
	    else if (enc.equals("UTF-16"))
	    {
		enc = "ISO-8859-1";
		encId = 0; 
		System.err.println("WARNING: converted unicode to latin1");
	    }
	    else if (enc.equals("UTF-8")) encId = 3;
	    else enc = "ISO-8859-1";

	    byte[] descrData = descr.getBytes(enc);
	    int resultLen = 1+descrData.length+1+urlData.length;
	    result = new byte[resultLen];
	    result[i++] = (byte) encId;
	    System.arraycopy(descrData, 0, result, i, descrData.length);
	    i += descrData.length;
	    result[i++] = 0;
	}
	else
	{
	    result = new byte[urlData.length];
	}
	System.arraycopy(urlData, 0, result, i, urlData.length);

	return result;
    }

    public String toString ()
    {
	return frameId.equals("WXXX")
		?  super.toString() + ": \"" + descr + "\" \"" + url + "\""
		: super.toString() + ": \"" + url + "\"";
    }
}
