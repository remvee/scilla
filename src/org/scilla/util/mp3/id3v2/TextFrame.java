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

package org.scilla.util.mp3;

import java.io.*;
import java.util.*;

/**
 * @author Remco van 't Veer
 * @version $Revision: 1.1 $
 */
class TextFrame extends Frame
{
    String encoding;
    String text;
    String language = null;
    String ident = null;

    int type;
    public final static int PLAIN = 0;
    public final static int LANGUAGE = 1;

    public TextFrame (byte[] data, int offset, int minor)
    throws Exception
    {
	this(data, offset, minor, PLAIN);
    }

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

	    i++;
	}

	// get text value
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	while (i < frameData.length && frameData[i] != 0) out.write(frameData[i++]);
	text = out.toString();
    }

    public String getText ()
    {
	return text;
    }

    public String getLanguage ()
    {
	return language;
    }

    public String getIdentifier ()
    {
	return ident;
    }

    public String toString ()
    {
	if (type == LANGUAGE)
	{
	    return super.toString() + ": \"" + ident + "\"("+ language + ") \"" + text + "\"";
	}
	else
	{
	    return super.toString() + ": \"" + text + "\"";
	}
    }
}
