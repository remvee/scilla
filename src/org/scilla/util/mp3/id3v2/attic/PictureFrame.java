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
 * Representation of picture frame (<TT>APIC</TT>).
 *
 * @author Remco van 't Veer
 * @version $Revision: 1.4 $
 */
public class PictureFrame extends Frame
{
    String mimeType;
    int picType;
    String descr;
    byte[] picData;

    /**
     * Constructor for a picture frame.
     * @param data buffer of tag data
     * @param offset offset in buffer
     * @param minor minor version of tag
     */
    public PictureFrame (byte[] data, int offset, int minor)
    throws Exception
    {
	super(data, offset, minor);

	// encoding for description
	int i = 0, j = 0;
	ByteArrayOutputStream out;
	String enc;
	boolean isUnicode = false;
	switch (frameData[i++])
	{
	    case 0: enc = "ISO-8859-1"; break;
	    case 1:
	    case 2: enc = "UTF-16"; isUnicode = true; break;
	    case 3: enc = "UTF-8"; break;
	    default: throw new RuntimeException("text encoding not supported");
	}

	// get mime type
	j = TextFrame.nextStringTerminator(frameData, i, false);
	mimeType = new String(frameData, i, j - i);
	i = j + 1;

	// picture type
	picType = (int) frameData[i++] & 0xff;

	// get description
	j = TextFrame.nextStringTerminator(frameData, i, isUnicode);
	descr = new String(frameData, i, j - i, enc);
	i = j + 1;

	// copy picture data
	int picSize = frameData.length - i;
	picData = new byte[picSize];
	System.arraycopy(frameData, i, picData, 0, picSize);
    }

    public String toString ()
    {
	return super.toString() + ": \"" + mimeType + "\" (" + picType + ") \""
		+ descr + "\" " + picData.length + " bytes";
    }
}
