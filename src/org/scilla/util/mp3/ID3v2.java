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

import org.scilla.util.mp3.id3v2.*;

/**
 * Representation of a ID3v2 tag.  Currently supports only
 * readonly access to v2.3 tags and a subset of the allowed
 * frames.  V2.2 tags are not supported since the format of the
 * frames differs from later versions of the ID3v2 specs.
 *
 * @see <a href="http://www.id3.org/id3v2.3.0.html">ID3 made easy</a>
 * @author Remco van 't Veer
 * @version $Revision: 1.7 $
 */
public class ID3v2
{
    boolean tagAvailable = false;

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
	readTag(new FileInputStream(f));
    }

    private void readTag (InputStream in)
    throws IOException, Exception
    {
	// read header
	header = new byte[10];
	in.read(header);
	if (header[0] != 'I' || header[1] != 'D' || header[2] != '3') return;

	// extract header info
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
	if (unsyncFlag)
	{
	    for (int last = 0, i = 0, n = 0; n < tagSize; n++)
	    {
		int b = in.read();
		if (b == -1) throw new Exception("file truncated");

		if (! (last == 0xff && b == 0)) tagData[i++] = (byte) b;
		last = b;
	    }
	}
	else
	{
	    in.read(tagData);
	}
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
	    else if (id.equals("COMM") || id.equals("USLT"))
	    {
		frame = new TextFrame(tagData, bytesRead, minor, TextFrame.LANGUAGE);
	    }
	    else if (id.startsWith("W"))
	    {
		frame = new LinkFrame(tagData, bytesRead, minor);
	    }
	    else if (id.startsWith("APIC"))
	    {
		frame = new PictureFrame(tagData, bytesRead, minor);
	    }
	    else
	    {
		frame = new Frame(tagData, bytesRead, minor);
	    }

	    frames.add(frame);
	    bytesRead += frame.getLength();
	}

	// tag successfully read
	tagAvailable = true;
    }

    public boolean hasTag () { return tagAvailable; }
    public int getMinorVersion () { return minor; }
    public int getRevision () { return revis; }
    public boolean isUnsynchronized () { return unsyncFlag; }
    public boolean hasExtended () { return extFlag; }
    public boolean isExperimental () { return expFlag; }
    public boolean hasFooter () { return extFlag; }

    /** @return list of frames */
    public List getFrames () { return frames; }
    /** @return first frame by given id or <TT>null</TT> */
    public Frame getFrame (String id)
    {
	Iterator it = frames.iterator();
	while (it.hasNext())
	{
	    Frame f = (Frame) it.next();
	    if (f.getID().equals(id)) return f;
	}
	return null;
    }
    /** @return list of frames by given id */
    public List getFrames (String id)
    {
	List result = new Vector();
	Iterator it = frames.iterator();
	while (it.hasNext())
	{
	    Frame f = (Frame) it.next();
	    if (f.getID().equals(id)) result.add(f);
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

    /** debugging */
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

