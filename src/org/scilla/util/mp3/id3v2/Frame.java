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

import org.scilla.util.mp3.ID3v2;

/**
 * Basic representation of a frame.
 *
 * @author Remco van 't Veer
 * @version $Revision: 1.5 $
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
    int frameFlags = 0;

    boolean tagAlterPreserv; // TODO implement support for this!
    boolean fileAlterPreserv; // TODO implement support for this!
    boolean readOnly; // TODO implement support for this!
    boolean groupingIdentity; // TODO implement support for this!
    boolean compression; // TODO implement support for this!
    boolean encryption; // TODO implement support for this!
    boolean unsynchronisation; // TODO implement support for this!
    boolean dataLengthIndicator; // TODO v2.4.0 support

    public byte[] frameData;

    /**
     * Empty constructor for creating a frame.
     */
    public Frame ()
    {
    }

    /**
     * Constructor for reading an existing frame.
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
		? ID3v2.readUnsyncInt(data, i)
		: ID3v2.readPlainInt(data, i);
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
	frameLength = frameSize + 4 + 4 + 2;

	// copy data
	frameData = new byte[frameSize];
	System.arraycopy(data, i, frameData, 0, frameSize);
    }

    /**
     * @throws RuntimeException only specific frame
     * implementations can create frame data.
     */
    public byte[] getBytes ()
    throws UnsupportedEncodingException
    {
	throw new RuntimeException("OPERATION NOT SUPPORTED");
    }

    public byte[] getByteArray ()
    throws UnsupportedEncodingException
    {
	ByteArrayOutputStream out = new ByteArrayOutputStream();

	// ID
	out.write(frameId.charAt(0));
	out.write(frameId.charAt(1));
	out.write(frameId.charAt(2));
	out.write(frameId.charAt(3));

	// length
	byte[] length = new byte[4];
	byte[] data = getBytes();
	ID3v2.writeUnsyncInt(data.length, length, 0);
	try { out.write(length); }
	catch (IOException ex) { /* will never happen */ }

	// flags
	frameFlags = 0; // TODO TODO
	out.write(frameFlags >> 8);
	out.write(frameFlags);

	// data
	try { out.write(data); }
	catch (IOException ex) { /* will never happen */ }

	return out.toByteArray();
    }

    /** @return frame identifier */
    public String getID () { return frameId; }
    /** @return full length of this frame */
    public int getLength () { return frameLength; }

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
}
