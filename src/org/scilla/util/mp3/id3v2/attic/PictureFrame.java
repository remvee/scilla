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
 * @version $Revision: 1.1 $
 */
public class PictureFrame extends Frame
{
    String mimeType;
    int picType;
    String descr;
    byte[] picData;
/*
     Text encoding      $xx
     MIME type          <text string> $00
     Picture type       $xx
     Description        <text string according to encoding> $00 (00)
     Picture data       <binary data>
*/

static int num = 0;
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

	// optionally get description
	int i = 0;
	ByteArrayOutputStream out;
	String encoding;
	switch (frameData[i++])
	{
	    case 0: encoding = "ISO-8859-1"; break;
	    case 1:
	    case 2: encoding = "UTF-16"; break;
	    case 3: encoding = "UTF-8"; break;
	    default: throw new Exception("text encoding not supported");
	}

	// get mime type
	out = new ByteArrayOutputStream();
	while (i < frameData.length && frameData[i] != 0) out.write(frameData[i++]);
	mimeType = out.toString(encoding);

	// skip over zero
	i++;

	// picture type
	picType = (int) frameData[i++] & 0xff;

	// get description
	out = new ByteArrayOutputStream();
	while (i < frameData.length && frameData[i] != 0) out.write(frameData[i++]);
	descr = out.toString(encoding);

	// skip over zero
	i++;

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
