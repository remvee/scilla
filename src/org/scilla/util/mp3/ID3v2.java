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
 * access to v2.3 tags and a subset of the allowed frames.  V2.2
 * tags are not supported since the format of the frames differs
 * from later versions of the ID3v2 specs.
 *
 * @see <a href="http://www.id3.org/id3v2.3.0.html">ID3 made easy</a>
 * @author Remco van 't Veer
 * @version $Revision: 1.16 $
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
    throws Exception
    {
	InputStream in = new FileInputStream(f);
	readTag(in);
	in.close();
    }

    private void readTag (InputStream in)
    throws Exception
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
	tagLength = readUnsyncInt(header, 6);

	// read tag data
	int tagSize = tagLength; // - (footerFlag ? 20 : 10);
	byte[] tagData = new byte[tagSize];
	if (unsyncFlag)
	{
	    for (int last = 0, i = 0, n = 0; n < tagSize; n++)
	    {
		int b = in.read();
		if (b == -1) throw new IOException("file truncated");

		if (! (last == 0xff && b == 0)) tagData[i++] = (byte) b;
		last = b;
	    }
	}
	else
	{
	    in.read(tagData);
	}

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
	    if (id.startsWith("T"))
	    {
		int type = id.equals("TXXX") ? TextFrame.TXXX : TextFrame.PLAIN;
		frame = new TextFrame(tagData, bytesRead, minor, type);
	    }
	    else if (id.equals("COMM") || id.equals("USLT"))
	    {
		frame = new TextFrame(tagData, bytesRead, minor, TextFrame.LANGUAGE);
	    }
	    else if (id.startsWith("W"))
	    {
		frame = new LinkFrame(tagData, bytesRead, minor);
	    }
	    else if (id.equals("APIC") || id.equals("GEOB"))
	    {
		frame = new AttachmentFrame(tagData, bytesRead, minor);
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

    final static int padding = 0;
    private void writeTag (OutputStream out)
    throws IOException, Exception
    {
	// prepare tag properties
	minor = 3;
	revis = 0;
	unsyncFlag = false;
	extFlag = false;
	expFlag = false;
	footerFlag = false;

	// prepare header
	header = new byte[10];
	header[0] = 'I'; header[1] = 'D'; header[2] = '3';
	header[3] = (byte) minor; header[4] = (byte) revis;

	// prepare frames
	ByteArrayOutputStream bout = new ByteArrayOutputStream();
	Iterator it = frames.iterator();
	while (it.hasNext())
	{
	    Frame f = (Frame) it.next();
	    byte[] b = f.getByteArray();
	    if (b != null) bout.write(b);
	}
	byte[] data = bout.toByteArray();

	// see if we need unsyncing
	for (int i = 0; i < data.length-1; i++)
	{
	    if (data[i] == 0xFF && (data[i+1] & 0xE0) == 0xE0)
	    {
		unsyncFlag = true;
		break;
	    }
	}
	if (unsyncFlag) data = unsyncArray(data);

	// add padding
	if (padding > 0) data = addPadding(data, padding);

	// prepare bits for header
	if (unsyncFlag) bits |= 0x80;
	if (extFlag) bits |= 0x40;
	if (expFlag) bits |= 0x20;
	if (footerFlag) bits |= 0x10;
	header[5] = (byte) bits;

	// prepare length for header
	writeUnsyncInt(data.length, header, 6);

	// write tag data
	out.write(header);
	out.write(data);
    }

    public boolean hasTag () { return tagAvailable; }
    public int getMinorVersion () { return minor; }
    public int getRevision () { return revis; }
    public boolean isUnsynchronized () { return unsyncFlag; }
    public boolean hasExtended () { return extFlag; }
    public boolean isExperimental () { return expFlag; }
    public boolean hasFooter () { return extFlag; }

    /*
	TODO implement following frame(list) constraints:

	simple uniq:
	    UFID uniq
	    T??? uniq (not TXXX)
	    W??? uniq (not WXXX)

	complex uniq:
	    TXXX uniq per ident
	    WXXX uniq per ident
	    USLT uniq per lang/ident
	    COMM uniq per lang/ident
	    APIC uniq per ident and for type 1 and 2
	    GEOB uniq per ident

	lists:
	    TCOM /-separated
	    TCOM /-separated
	    TOLY /-separated
	    TOPE /-separated
	    TPE1 /-separated
	    TPE2 /-separated
	    TPE4 /-separated
	    TPOS /-separated-tuple
	    TRCK /-separated-tuple

	strict formats:
	    TDAT DDMM-format
	    TIME HHMM-format
	    TKEY max3-char
     */

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
	if (! tagAvailable) return "no ID3v2 tag";

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

    public final static int readUnsyncInt (byte[] data, int offset)
    {
	return (((int) data[offset++]) << 21) +
		(((int) data[offset++]) << 14) +
		(((int) data[offset++]) << 7) +
		((int) data[offset++]);
    }
    public final static int readUnsyncInt3 (byte[] data, int offset)
    {
	return (((int) data[offset++]) << 14) +
		(((int) data[offset++]) << 7) +
		((int) data[offset++]);
    }
    public final static int readPlainInt (byte[] data, int offset)
    {
	return (((int) (data[offset++]) & 0xff) << 24) +
		((((int) data[offset++]) & 0xff) << 16) +
		((((int) data[offset++]) & 0xff) << 8) +
		(((int) data[offset++]) & 0xff);
    }
    public final static void writeUnsyncInt (int val, byte[] data, int offset)
    {
	data[offset] = (byte) (val >> 21);
	data[offset++] &= 0x7f;
	data[offset] = (byte) (val >> 14);
	data[offset++] &= 0x7f;
	data[offset] = (byte) (val >> 7);
	data[offset++] &= 0x7f;
	data[offset] = (byte) val;
	data[offset++] &= 0x7f;
    }
    public final static byte[] unsyncArray (byte[] data)
    {
	ByteArrayOutputStream out = new ByteArrayOutputStream();

	for (int i = 0; i < data.length; i++)
	{
	    out.write(data[i]);
	    if (data[i] == 0xFF) out.write(0);
	}

	return out.toByteArray();
    }
    public final static byte[] addPadding (byte[] data, int n)
    {
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	try
	{
	    out.write(data);
	    out.write(new byte[n]);
	}
	catch (IOException ex) { /* we never happen */ }
	return out.toByteArray();
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
		if (tag.hasTag())
		{
		    String ifn = args[i];
		    String ofn = i+".tag";

		    MyInputStream myin = new MyInputStream(new FileInputStream(ifn));
		    tag.readTag(myin); myin.close();
		    OutputStream out = new FileOutputStream(ofn);
		    byte[] b = new byte[myin.readCount];
		    InputStream in = new FileInputStream(ifn);
		    int n = in.read(b);
		    out.write(b, 0, n);
		    in.close();
		    out.close();
		}
	    }
	    catch (Throwable ex)
	    {
		ex.printStackTrace(System.out);
	    }
	}
    }

}

class MyInputStream extends InputStream
{
    public int readCount = 0;
    public InputStream in;

    public MyInputStream (InputStream in)
    {
	this.in = in;
    }

    public int read ()
    throws IOException
    {
	readCount++;
	return in.read();
    }

    public void close()
    throws IOException
    {
	in.close();
    }
}
