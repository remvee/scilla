/*
 * scilla
 *
 * Copyright (C) 2003  R.W. van 't Veer
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

package org.scilla.util.vorbis;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Remco van 't Veer
 * @version $Revision: 1.3 $
 */
public class VorbisInfo {
    public final String PAGE_MARKER = "OggS";
    public final String VORBIS_MARKER = "vorbis";

    private int vorbisVersion;
    private int channels;
    private int sampleRate;
    private int maxBitrate;
    private int bitrate;
    private int minBitrate;
    private float seconds;
    private String vendor;
    private Map commentMap;

    public VorbisInfo (InputStream in)
    throws Exception {
	// first 2 frames should be enough..
	Frame f1 = new Frame(in, false);
	Frame f2 = new Frame(in, false);
	// append page data
	byte[] data = new byte[f1.length + f2.length];
	System.arraycopy(f1.data, 0, data, 0, f1.length);
	System.arraycopy(f2.data, 0, data, f1.length, f2.length);
	// identification header
	{
	    if (data[0] != 1 || ! hasVorbisMarker(data, 1)) {
		throw new Exception("didn't find vorbis identification header");
	    }
	    int pos = 7;
	    vorbisVersion = (data[pos++] & 0xff) + ((data[pos++] & 0xff) << 8)
		    + ((data[pos++] & 0xff) << 16) + ((data[pos++] & 0xff) << 24);
	    channels = (int) data[pos++];
	    sampleRate = (data[pos++] & 0xff) + ((data[pos++] & 0xff) << 8)
		    + ((data[pos++] & 0xff) << 16) + ((data[pos++] & 0xff) << 24);
	    maxBitrate = (data[pos++] & 0xff) + ((data[pos++] & 0xff) << 8)
		    + ((data[pos++] & 0xff) << 16) + ((data[pos++] & 0xff) << 24);
	    bitrate = (data[pos++] & 0xff) + ((data[pos++] & 0xff) << 8)
		    + ((data[pos++] & 0xff) << 16) + ((data[pos++] & 0xff) << 24);
	    minBitrate = (data[pos++] & 0xff) + ((data[pos++] & 0xff) << 8)
		    + ((data[pos++] & 0xff) << 16) + ((data[pos++] & 0xff) << 24);
	}
	// comment header
	{
	    if (data[30] != 3 || ! hasVorbisMarker(data, 31)) {
		throw new Exception("didn't find vorbis comment header");
	    }
	    int pos = 37;

	    {
		int l = (data[pos++] & 0xff) + ((data[pos++] & 0xff) << 8)
			+ ((data[pos++] & 0xff) << 16) + ((data[pos++] & 0xff) << 24);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < l; i++) {
		    sb.append((char) data[pos++]);
		}
		vendor = sb.toString();
	    }

	    commentMap = new HashMap();
	    int commentLen = (data[pos++] & 0xff) + ((data[pos++] & 0xff) << 8)
		    + ((data[pos++] & 0xff) << 16) + ((data[pos++] & 0xff) << 24);
	    for (int i = 0; i < commentLen; i++) {
		int l = (data[pos++] & 0xff) + ((data[pos++] & 0xff) << 8)
			+ ((data[pos++] & 0xff) << 16) + ((data[pos++] & 0xff) << 24);
		StringBuffer sb = new StringBuffer();
		for (int j = 0; j < l; j++) {
		    sb.append((char) data[pos++]);
		}
		String v = sb.toString();
		String key = v.substring(0, v.indexOf('=')).toLowerCase();
		String val = v.substring(v.indexOf('=') + 1);
		commentMap.put(key, val);
	    }
	}
	// goto last frame
	{
	    Frame frame = null;
	    try {
		do {
		    frame = new Frame(in, true);
		} while (frame.headerType != 4);
	    } catch (Throwable ex) {
		// ignore
	    }
	    seconds = (float) frame.granulePosition / sampleRate;
	}
    }

    public int getVorbisVersion () {
	return vorbisVersion;
    }
    public int getChannels () {
	return channels;
    }
    public int getSampleRate () {
	return sampleRate;
    }
    public int getMaximumBitrate () {
	return maxBitrate;
    }
    public int getBitrate () {
	return bitrate;
    }
    public int getMinimumBitrate () {
	return minBitrate;
    }
    public float getLength () {
	return seconds;
    }
    public String getVendor () {
	return vendor;
    }
    public String getComment (String key) {
	return (String) commentMap.get(key.toLowerCase());
    }
    public Set getCommentKeys () {
	return new HashSet(commentMap.keySet());
    }

    public String toString () {
	return
	    "vorbisVersion="+vorbisVersion+
	    "\nchannels="+channels+
	    "\nsampleRate="+sampleRate+
	    "\nmaxBitrate="+maxBitrate+
	    "\nnominalBitrate="+bitrate+
	    "\nminBitrate="+minBitrate+
	    "\nvendor="+vendor+
	    "\nseconds="+seconds+
	    "\ncomments="+commentMap;
    }

    private boolean hasVorbisMarker (byte[] data, int offset) {
	char[] marker = VORBIS_MARKER.toCharArray();
	for (int i = 0; i < marker.length; i++) {
	    if (data[offset + i] != marker[i]) {
		return false;
	    }
	}
	return true;
    }

    private class Frame {
	int revision;
	int headerType;
	long granulePosition;
	int serialNumber;
	int pageSequence;
	int pageChecksum;
	int pageSegments;
	int[] segmentTable;
	int length;
	byte[] data;

	Frame (InputStream in, boolean skipData)
	throws IOException {
	    // read page marker
	    char[] marker = PAGE_MARKER.toCharArray();
	    for (int i = 0; i < marker.length; i++) {
		if ((char)in.read() != marker[i]) {
		    throw new IOException("NOT AN OGG VORBIS FILE");
		}
	    }
	    // read page parameters
	    revision = in.read();
	    headerType = in.read();
	    granulePosition = readLong(in);
	    serialNumber = readInt(in);
	    pageSequence = readInt(in);
	    pageChecksum = readInt(in);
	    pageSegments = in.read();

	    // read segment table and determine data length
	    segmentTable = new int[pageSegments];
	    length = 0;
	    for (int i = 0; i < pageSegments; i++) {
		segmentTable[i] = in.read();
		length += segmentTable[i];
	    }

	    // read data
	    if (skipData) {
		in.skip((long)length);
	    } else {
		data = new byte[length];
		if (in.read(data) != length) {
		    throw new IOException("NOT AN OGG VORBIS FILE");
		}
	    }
	}

	private int readInt (InputStream in)
	throws IOException {
	    int c0 = in.read();
	    int c1 = in.read();
	    int c2 = in.read();
	    int c3 = in.read();
	    return c0 + (c1 << 8) + (c1 << 16) + (c1 << 24);
	}

	private long readLong (InputStream in)
	throws IOException {
	    long c0 = (long) in.read();
	    long c1 = (long) in.read();
	    long c2 = (long) in.read();
	    long c3 = (long) in.read();
	    long c4 = (long) in.read();
	    long c5 = (long) in.read();
	    long c6 = (long) in.read();
	    long c7 = (long) in.read();
	    return c0 + (c1 << 8) + (c2 << 16) + (c3 << 24) +
		    (c4 << 32) + (c5 << 40) + (c6 << 48) + (c7 << 56);
	}
    }

    /** debug */
    public static void main (String[] args)
    throws Exception {
	for (int i = 0; i < args.length; i++) {
	    System.out.println(args[i]);
	    FileInputStream in = new FileInputStream(args[i]);
	    VorbisInfo info = new VorbisInfo(in);
	    in.close();
	    System.out.println("  "+info);
	}
    }
}
