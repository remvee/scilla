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
 * Representation of two types of text frames.  The simple text
 * frames like in the <TT>T???</TT> series and the frames with a
 * language and identifier like <TT>COMM</TT> and <TT>USLT</TT>.
 *
 * @author Remco van 't Veer
 * @version $Revision: 1.5 $
 */
public class TextFrame extends Frame
{
    String encoding;
    String text;
    String language = null;
    String ident = null;

    int type;
    /** type constant for the <TT>T???</TT> series */
    public final static int PLAIN = 0;
    /** type constant for <TT>COMM</TT> and others */
    public final static int LANGUAGE = 1;

    /**
     * Constructor for plain text frame.
     * @param data buffer of tag data
     * @param offset offset in buffer
     * @param minor minor version of tag
     */
    public TextFrame (byte[] data, int offset, int minor)
    throws Exception
    {
	this(data, offset, minor, PLAIN);
    }

    /**
     * Constructor for a text frame.
     * @param data buffer of tag data
     * @param offset offset in buffer
     * @param minor minor version of tag
     * @param type {@link #PLAIN} or {@link #LANGUAGE}
     */
    public TextFrame (byte[] data, int offset, int minor, int type)
    throws Exception
    {
	super(data, offset, minor);
	this.type = type;

	// determine encoding
	int i = 0;
	switch (frameData[i++])
	{
	    case 0: encoding = "ISO-8859-1"; break;
	    case 1:
	    case 2: encoding = "UTF-16"; break;
	    case 3: encoding = "UTF-8"; break;
	    default: throw new Exception("text encoding not supported");
	}

	// optionally get language code and identifier
	if (type == LANGUAGE)
	{
	    StringBuffer sb;

	    sb = new StringBuffer();
	    sb.append((char) frameData[i++]);
	    sb.append((char) frameData[i++]);
	    sb.append((char) frameData[i++]);
	    language = sb.toString();

	    sb = new StringBuffer();
	    while (frameData[i] != 0) sb.append((char) frameData[i++]);
	    ident = sb.toString();

	    // skip over zero
	    i++;
	}

	// get text value
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	while (i < frameData.length && frameData[i] != 0) out.write(frameData[i++]);
	text = out.toString();
    }

    /** @return text of this frame */
    public String getText () { return text; }
    /** @return language of this frame or <TT>null</TT> */
    public String getLanguage () { return language; }
    /** @return identifier of this frame or <TT>null</TT> */
    public String getIdentifier () { return ident; }

    public String toString ()
    {
	return type == LANGUAGE
		?  super.toString() + ": \"" + ident + "\"("+ language + ") \"" + text + "\""
		: super.toString() + ": \"" + text + "\"";
    }
}
