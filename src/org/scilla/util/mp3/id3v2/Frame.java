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
 * Basic representation of a frame.
 *
 * @author Remco van 't Veer
 * @version $Revision: 1.3 $
 */
public class Frame
{
    /* length of frame */
    int frameLength = 0;
    /* size of frame data */
    int frameSize = 0;
    /* ID of frame */
    String frameId = null;
    /* frame flags */
    int frameFlags = -1; // %0abc0000 %0h00kmnp

    boolean tagAlterPreserv;
    boolean fileAlterPreserv;
    boolean readOnly;
    boolean groupingIdentity;
    boolean compression;
    boolean encryption;
    boolean unsynchronisation;
    boolean dataLengthIndicator;

    byte[] frameData;

    /**
     * @param data buffer of tag data
     * @param offset offset in buffer
     * @param minor minor version of tag
     */
    public Frame (byte[] data, int offset, int minor)
    {
	int i = offset;

	// read frame identifier
	StringBuffer sb = new StringBuffer();
	sb.append((char)data[i++]);
	sb.append((char)data[i++]);
	sb.append((char)data[i++]);
	sb.append((char)data[i++]);
	frameId = sb.toString();

	// read frame size
	frameSize = minor >= 4
		? unsynchInteger(data, i)
		: plainInteger(data, i);
	i += 4;

	// read frame flags
	frameFlags = ((int) data[i++] << 8) + (int) data[i++];
	tagAlterPreserv = (frameFlags & 0x4000) != 0;
	fileAlterPreserv = (frameFlags & 0x2000) != 0;
	readOnly = (frameFlags & 0x1000) != 0;
	groupingIdentity = (frameFlags & 0x0040) != 0;
	compression = (frameFlags & 0x0008) != 0;
	encryption = (frameFlags & 0x0004) != 0;
	unsynchronisation = (frameFlags & 0x0002) != 0;
	dataLengthIndicator = (frameFlags & 0x0001) != 0;

	// calculate frame length
	frameLength = frameSize + (4 + 4 + 2);

	// copy data
	frameData = new byte[frameSize];
	System.arraycopy(data, i, frameData, 0, frameSize);
    }

    public int getLength () { return frameLength; }
    public String getID () { return frameId; }

    public String toString ()
    {
	String result = frameId;
	result += " size="+frameSize;
	result += tagAlterPreserv ? " tagAlterPreserv" : "";
	result += fileAlterPreserv ? " fileAlterPreserv" : "";
	result += readOnly ? " readOnly" : "";
	result += groupingIdentity ? " groupingIdentity" : "";
	result += compression ? " compression" : "";
	result += unsynchronisation ? " unsynchronisation" : "";
	result += dataLengthIndicator ? " dataLengthIndicator" : "";
	return result;
    }

    public final static int unsynchInteger (byte[] data, int offset)
    {
	return (((int) data[offset++]) << 21) +
		(((int) data[offset++]) << 14) +
		(((int) data[offset++]) << 7) +
		((int) data[offset++]);
    }
    public final static int plainInteger (byte[] data, int offset)
    {
	return (((int) (data[offset++]) & 0xff) << 24) +
		((((int) data[offset++]) & 0xff) << 16) +
		((((int) data[offset++]) & 0xff) << 8) +
		(((int) data[offset++]) & 0xff);
    }
}
