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
 * @version $Revision: 1.9 $
 */
public class Frame {
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

    // id3v2.2 to id3v2.3 translation map
    static Map t2to3Map = null;
    static
    {
        t2to3Map = new HashMap();
        t2to3Map.put("COM", "COMM");
        t2to3Map.put("GEO", "GEOB");
        t2to3Map.put("IPL", "IPLS");
        t2to3Map.put("LNK", "LINK");
        t2to3Map.put("MCI", "MCDI");
        t2to3Map.put("MLL", "MLLT");
        t2to3Map.put("PIC", "APIC");
        t2to3Map.put("POP", "POPM");
        t2to3Map.put("REV", "RVRB");
        t2to3Map.put("RVA", "RVAD");
        t2to3Map.put("SLT", "SYLT");
        t2to3Map.put("STC", "SYTC");
        t2to3Map.put("TAL", "TALB");
        t2to3Map.put("TBP", "TBPM");
        t2to3Map.put("TCM", "TCOM");
        t2to3Map.put("TCO", "TCON");
        t2to3Map.put("TCR", "TCOP");
        t2to3Map.put("TDA", "TDAT");
        t2to3Map.put("TDY", "TDLY");
        t2to3Map.put("TEN", "TENC");
        t2to3Map.put("TFT", "TFLT");
        t2to3Map.put("TIM", "TIME");
        t2to3Map.put("TKE", "TKEY");
        t2to3Map.put("TLA", "TLAN");
        t2to3Map.put("TLE", "TLEN");
        t2to3Map.put("TMT", "TMED");
        t2to3Map.put("TOA", "TOPE");
        t2to3Map.put("TOF", "TOFN");
        t2to3Map.put("TOL", "TOLY");
        t2to3Map.put("TOR", "TORY");
        t2to3Map.put("TOT", "TOAL");
        t2to3Map.put("TP1", "TPE1");
        t2to3Map.put("TP2", "TPE2");
        t2to3Map.put("TP3", "TPE3");
        t2to3Map.put("TP4", "TPE4");
        t2to3Map.put("TPA", "TPOS");
        t2to3Map.put("TPB", "TPUB");
        t2to3Map.put("TRC", "TSRC");
        t2to3Map.put("TRD", "TRDA");
        t2to3Map.put("TRK", "TRCK");
        t2to3Map.put("TSI", "TSIZ");
        t2to3Map.put("TSS", "TSSE");
        t2to3Map.put("TT1", "TIT1");
        t2to3Map.put("TT2", "TIT2");
        t2to3Map.put("TT3", "TIT3");
        t2to3Map.put("TXT", "TEXT");
        t2to3Map.put("TXX", "TXXX");
        t2to3Map.put("TYE", "TYER");
        t2to3Map.put("UFI", "UFID");
        t2to3Map.put("ULT", "USLT");
        t2to3Map.put("WAF", "WOAF");
        t2to3Map.put("WAR", "WOAR");
        t2to3Map.put("WAS", "WOAS");
        t2to3Map.put("WCM", "WCOM");
        t2to3Map.put("WCP", "WCOP");
        t2to3Map.put("WPB", "WPUB");
        t2to3Map.put("WXX", "WXXX");
        t2to3Map = Collections.unmodifiableMap(t2to3Map);
    }


    /**
     * Empty constructor for creating a frame.
     */
    public Frame () {}

    /**
     * Constructor for reading an existing frame.  All v2.2 frame
     * IDs will be translated to their v2.3 equivalents.
     * @param data buffer of tag data
     * @param offset offset in buffer
     * @param minor minor version of tag
     */
    public Frame (byte[] data, int offset, int minor) {
        int i = offset;

        // read frame identifier
        StringBuffer sb = new StringBuffer();
        sb.append((char)data[i++]);
        sb.append((char)data[i++]);
        sb.append((char)data[i++]);
        if (minor == 2) {
            // translate v2.2 ID to v2.3
            frameId = (String) t2to3Map.get(sb.toString());
        } else {
            sb.append((char)data[i++]);
            frameId = sb.toString();
        }

        // read frame size
        switch (minor) {
        case 2:
            frameSize = ID3v2.readUnsyncInt3(data, i);
            i += 3;
            break;
        case 3:
            frameSize = ID3v2.readPlainInt(data, i);
            i += 4;
            break;
        case 4:
            frameSize = ID3v2.readUnsyncInt(data, i);
            i += 4;
            break;
        default:
            throw new RuntimeException("ID3v2."+minor+".X not supported");
        }

        // read frame flags
        if (minor != 2) {
            frameFlags = ((int) data[i++] << 8) + (int) data[i++];
            tagAlterPreserv = (frameFlags & 0x4000) != 0;
            fileAlterPreserv = (frameFlags & 0x2000) != 0;
            readOnly = (frameFlags & 0x1000) != 0;
            groupingIdentity = (frameFlags & 0x0040) != 0;
            compression = (frameFlags & 0x0008) != 0;
            encryption = (frameFlags & 0x0004) != 0;
            unsynchronisation = (frameFlags & 0x0002) != 0;
            dataLengthIndicator = (frameFlags & 0x0001) != 0;
        }

        // calculate frame length
        frameLength = frameSize + 4 + (minor == 2 ? 2 : 6);

        // copy data
        frameData = new byte[frameSize];
        System.arraycopy(data, i, frameData, 0, frameSize);
    }

    /**
     * @return <TT>null</TT> implementations should override this
     * method
     */
    public byte[] getBytes ()
    throws UnsupportedEncodingException {
        System.err.println("WARNING: frame \""+frameId+"\" omitted");
        return null;
    }

    public byte[] getByteArray ()
    throws UnsupportedEncodingException {
        byte[] data = getBytes();
        if (data == null) {
            return null;
	}

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // ID
        out.write(frameId.charAt(0));
        out.write(frameId.charAt(1));
        out.write(frameId.charAt(2));
        out.write(frameId.charAt(3));

        // length
        byte[] length = new byte[4];
        ID3v2.writeUnsyncInt(data.length, length, 0);
        try {
            out.write(length);
        } catch (IOException ex) {
	    // will never happen
        }

        // flags
        frameFlags = 0; // TODO TODO
        out.write(frameFlags >> 8);
        out.write(frameFlags);

        // data
        try {
            out.write(data);
        } catch (IOException ex) {
	    // will never happen
        }

        return out.toByteArray();
    }

    /** @return frame identifier */
    public String getID () {
        return frameId;
    }
    /** @return full length of this frame */
    public int getLength () {
        return frameLength;
    }

    public String toString () {
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
