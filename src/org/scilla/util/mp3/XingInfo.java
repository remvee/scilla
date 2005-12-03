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

/**
 * Read-only access the Xing info tag in an MP3 file.
 *
 * @author Remco van 't Veer
 * @version $Revision: 1.7 $
 * @see <CODE>xmms-1.2.4/Input/mpg123/dxhead.c</CODE>
 */
public class XingInfo extends FrameHeader {

    /// locals
    ///

    int flags = 0;
    int frames = -1;
    int bytes = -1;
    int vbrscale = -1;
    byte[] toc = null;
    double tpf = 0;


    /// constants
    ///

    public final static int FRAMES_FLAG = 1;
    public final static int BYTES_FLAG = 2;
    public final static int TOC_FLAG = 4;
    public final static int VBR_SCALE_FLAG = 8;
    private final static double[] tpfbs = { 0, 384, 1152, 1152 };


    /// constructors
    ///

    /**
     * Pass to FrameHeader to access first head and try to extract a
     * Xing info tag from this first frame.
     *
     * @see #close()
     * @throws IOException when file not readable
     * @throws Mp3Exception when tag not present
     */
    public XingInfo (File f) throws IOException, Mp3Exception {
        super(f);

        // forward to Xing info
        mp3File.skipBytes(isMpegVersion1()
                ? (isSingleChannel() ? 17 : 32)
                : (isSingleChannel() ?  9 : 17));

        // test if tag is present
        byte[] tag = new byte[4];
        mp3File.read(tag);
        if (! (new String(tag)).equals("Xing")) {
            super.close();
            throw new Mp3Exception("no tag present");
        }

        vbrFlag = true;

        // extract data
        flags = extractI4(mp3File);

        if ((flags & FRAMES_FLAG) != 0) {
            frames = extractI4(mp3File);
        }
        if ((flags & BYTES_FLAG) != 0) {
            bytes = extractI4(mp3File);
        }
        if ((flags & TOC_FLAG) != 0) {
            toc = new byte[100];
            for (int i = 0; i < 100; i++) {
                toc[i] = mp3File.readByte();
            }
        }

        if ((flags & VBR_SCALE_FLAG) != 0) {
            vbrscale = extractI4(mp3File);
        }

        tpf = tpfbs[layerToInt()] / getSampleRate();
        if (isMpegVersion25() || isMpegVersion2()) {
            tpf /= 2;
        }
    }


    /// accessors
    ///

    /** @return number of frames */
    public int getFrames () {
        return frames;
    }

    /** @return number of data bytes */
    public int getBytes () {
        return bytes;
    }

    /** @return variable bitrate scale */
    public int getVbrScale () {
        return vbrscale;
    }

    /** @return table of content */
    public byte[] getToc () {
        return toc;
    }

    /** @return length in seconds */
    public int getLength () {
        double n = tpf * (double) frames;
        return (int) n;
    }

    /** @return average bitrate */
    public int getBitRate () {
        return (int) ( (bytes * 8) / (tpf * frames * 1000) );
    }


    /// private functions
    ///

    private static int extractI4 (RandomAccessFile f)
    throws IOException {
        int n = 0;
        final byte[] b = new byte[4];
        f.read(b);

        n |= b[0] & 0xff;
        n <<= 8;
        n |= b[1] & 0xff;
        n <<= 8;
        n |= b[2] & 0xff;
        n <<= 8;
        n |= b[3] & 0xff;

        return n;
    }


    /// debugging
    ///

    /**
     * debugging..
     */
    public static void main (String[] args)
    throws Exception {
        for (int i = 0; i < args.length; i++) {
            XingInfo h = new XingInfo(new File(args[i]));
            System.out.println(args[i] + ":\n    " + h);
        }
    }
}

/* end of $Id: XingInfo.java,v 1.7 2005/09/30 13:51:06 remco Exp $ */
