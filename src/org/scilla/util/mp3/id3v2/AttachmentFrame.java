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
 * Representation of attachment frame (<TT>APIC</TT> and
 * <TT>GEOB</TT>).
 *
 * @author Remco van 't Veer
 * @version $Revision: 1.2 $
 */
public class AttachmentFrame extends Frame {
    String mimeType;
    String filename;
    int picType = -1;
    String descr;
    byte[] attachedData;
    String enc;

    /**
     * Constructor for a attachment frame.
     * @param data buffer of tag data
     * @param offset offset in buffer
     * @param minor minor version of tag
     */
    public AttachmentFrame (byte[] data, int offset, int minor)
    throws Exception {
        super(data, offset, minor);

        // determine frame type
        boolean isGEOB = frameId.equals("GEOB");

        // encoding for description
        int i = 0, j = 0;
        ByteArrayOutputStream out;
        boolean isUnicode = false;
        switch (frameData[i++]) {
        case 0:
            enc = "ISO-8859-1";
            break;
        case 1:
        case 2:
            enc = "UTF-16";
            isUnicode = true;
            break;
        case 3:
            enc = "UTF-8";
            break;
        default:
            throw new RuntimeException("text encoding not supported");
        }

        // get mime type
        j = TextFrame.nextStringTerminator(frameData, i, false);
        mimeType = new String(frameData, i, j - i);
        i = j + 1;

        if (isGEOB) {
            // filename
            j = TextFrame.nextStringTerminator(frameData, i, isUnicode);
            filename = new String(frameData, i, j - i, enc);
            i = j + 1;
        } else {
            // picture type
            picType = (int) frameData[i++] & 0xff;
        }

        // get description
        j = TextFrame.nextStringTerminator(frameData, i, isUnicode);
        descr = new String(frameData, i, j - i, enc);
        i = j + 1;

        // copy attachment data
        int size = frameData.length - i;
        attachedData = new byte[size];
        System.arraycopy(frameData, i, attachedData, 0, size);
    }

    /** @return mime type of attached data */
    public String getMimeType () {
        return mimeType;
    }
    /** @return filename for attached data (<TT>GEOB</TT>) or <TT>null</TT> */
    public String getFilename () {
        return filename;
    }
    /** @return type of picture (<TT>APIC</TT>) or <TT>-1</TT> */
    public int getPictureType () {
        return picType;
    }
    /** @return description of attached data */
    public String getDescription () {
        return descr;
    }
    /** @return attached data */
    public byte[] getData () {
        return attachedData;
    }

    public String toString () {
        return frameId.equals("GEOB")
               ? super.toString() + ": ("+enc+")\"" + mimeType + "\" \"" + filename + "\" \""
               + descr + "\" " + attachedData.length + " bytes"
               : super.toString() + ": ("+enc+")\"" + mimeType + "\" (" + picType + ") \""
               + descr + "\" " + attachedData.length + " bytes";
    }
}
