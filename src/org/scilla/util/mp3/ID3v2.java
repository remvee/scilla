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
public class ID3v2
{
    // header data
    byte[] header;
    int tagLength;
    int minor;
    int revis;
    int bits;
    boolean unsyncFlag;
    boolean extFlag;
    boolean expFlag;
    boolean footerFlag;

    // full tag data
    byte[] tagData;

    // list of frames
    List frames;

    public ID3v2 (File f)
    throws IOException, Exception
    {
	InputStream in = new FileInputStream(f);

	// read header
	header = new byte[10];
	in.read(header);
	if (header[0] != 'I' || header[1] != 'D' || header[2] != '3')
	{
	    throw new Exception("tag not available");
	}
	minor = (int) header[3];
	revis = (int) header[4];
	bits = (int) header[5];
	unsyncFlag = (bits & 0x80) != 0;
	extFlag = (bits & 0x40) != 0; // TODO handle extended headers
	expFlag = (bits & 0x20) != 0;
	footerFlag = (bits & 0x10) != 0; // TODO handle footer
	tagLength = Frame.unsynchInteger(header, 6);

	// read tag data
	int tagSize = tagLength; // - (footerFlag ? 20 : 10);
	byte[] tagData = new byte[tagSize];
	in.read(tagData);
	in.close();

	// collect frames
	int bytesRead = 0;
	frames = new Vector();
	while (bytesRead < tagSize)
	{
	    if (! Character.isLetterOrDigit((char)tagData[bytesRead])) break;

	    // read frame identifier
	    StringBuffer sb = new StringBuffer();
	    sb.append((char)tagData[bytesRead]);
	    sb.append((char)tagData[bytesRead+1]);
	    sb.append((char)tagData[bytesRead+2]);
	    sb.append((char)tagData[bytesRead+3]);
	    String id = sb.toString();

	    // determine frame type
	    Frame frame;
	    if (! id.equals("TXXX") && id.startsWith("T"))
	    {
		frame = new TextFrame(tagData, bytesRead, minor);
	    }
	    else if (! id.equals("WXXX") && id.startsWith("W"))
	    {
		frame = new TextFrame(tagData, bytesRead, minor);
	    }
	    else if (id.equals("COMM") || id.equals("USLT"))
	    {
		frame = new TextFrame(tagData, bytesRead, minor, TextFrame.LANGUAGE);
	    }
	    else
	    {
		frame = new Frame(tagData, bytesRead, minor);
	    }

	    frames.add(frame);
	    bytesRead += frame.frameLength;
	}
    }

    public int getMinorVersion () { return minor; }
    public int getRevision () { return revis; }
    public boolean isUnsynchronized () { return unsyncFlag; }
    public boolean hasExtended () { return extFlag; }
    public boolean isExperimental () { return expFlag; }
    public boolean hasFooter () { return extFlag; }

    public List getFrames () { return frames; }
    public Frame getFrame (String id)
    {
	Iterator it = frames.iterator();
	while (it.hasNext())
	{
	    Frame f = (Frame) it.next();
	    if (f.frameId.equals(id)) return f;
	}
	return null;
    }
    public List getFrames (String id)
    {
	List result = new Vector();
	Iterator it = frames.iterator();
	while (it.hasNext())
	{
	    Frame f = (Frame) it.next();
	    if (f.frameId.equals(id)) result.add(f);
	}
	return result;
    }

    public String toString ()
    {
	StringBuffer sb = new StringBuffer();
	sb.append("v2."+minor+"."+revis);
    	if (unsyncFlag) sb.append(" unsync");
    	if (extFlag) sb.append(" extended");
    	if (expFlag) sb.append(" experimental");
    	if (footerFlag) sb.append(" footer");

	sb.append(" (");

	Iterator it = frames.iterator();
	while (it.hasNext())
	{
	    Frame f = (Frame) it.next();
	    sb.append(f+"");

	    if (it.hasNext()) sb.append(',');
	}

	sb.append(')');

	return sb.toString();
    }

    public static void main (String[] args)
    throws Exception
    {
	for (int i = 0; i < args.length; i++)
	{
	    System.out.print(args[i]+": ");
	    try
	    {
		ID3v2 tag = new ID3v2(new File(args[i]));
		System.out.println(""+tag);
	    }
	    catch (Throwable ex)
	    {
		ex.printStackTrace(System.out);
	    }
	}
    }
}

