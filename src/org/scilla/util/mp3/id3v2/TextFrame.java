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

/**
 * Representation of two types of text frames.  The simple text
 * frames like in the <tt>T???</tt> series and the frames with a
 * language and identifier like <tt>COMM</tt> and <tt>USLT</tt>.
 *
 * @author Remco van 't Veer
 * @version $Revision: 1.13 $
 */
public class TextFrame extends Frame {
    private String enc;
    private String text;
    private String lang = null;
    private String ident = null;
    private int type;

    /** type constant for the <tt>Txxx</tt> series */
    public final static int PLAIN = 0;
    /** type constant for <tt>COMM</tt> and others */
    public final static int LANGUAGE = 1;
    /** type constant for <tt>TXXX</tt> and others */
    public final static int TXXX = 2;

    /**
     * Constructor for reading a plain text frame.
     * @param data buffer of tag data
     * @param offset offset in buffer
     * @param minor minor version of tag
     */
    public TextFrame (byte[] data, int offset, int minor)
    throws Exception {
        this(data, offset, minor, PLAIN);
    }

    /**
     * Constructor for reading a text frame.
     * @param data buffer of tag data
     * @param offset offset in buffer
     * @param minor minor version of tag
     * @param type {@link #PLAIN} or {@link #LANGUAGE}
     */
    public TextFrame (byte[] data, int offset, int minor, int type)
    throws Exception {
        super(data, offset, minor);
        this.type = type;

        // determine encoding
        int i = 0;
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

        // optionally get language code and identifier
        if (type == LANGUAGE) {
            StringBuffer sb;

            sb = new StringBuffer();
            sb.append((char) frameData[i++]);
            sb.append((char) frameData[i++]);
            sb.append((char) frameData[i++]);
            lang = sb.toString();

            int j = nextStringTerminator(frameData, i, isUnicode);
            ident = new String(frameData, i, j - i, enc);
            i = j + (isUnicode ? 2 : 1);
        } else if (type == TXXX) {
            int j = nextStringTerminator(frameData, i, isUnicode);
            ident = new String(frameData, i, j - i, enc);
            i = j + (isUnicode ? 2 : 1);
        }

        // get text value
        int j = nextStringTerminator(frameData, i, isUnicode);
        text = new String(frameData, i, j - i, enc);
    }

    final static int nextStringTerminator(byte[] data, int i, boolean isUnicode) {
        int j = i;
        for (; j < data.length; j++) {
            if (data[j] != 0) {
                continue;
	    }
            if (! isUnicode) {
                break;
	    }
            if (j < data.length-1 && data[j+1] == 0) {
                break;
	    }
        }
        if (j < data.length && isUnicode) {
            if (((j - i) % 2) != 0) {
                j++;
	    }
        }
        return j;
    }

    /**
     * Constructor for new text frame.
     * @param id frame identifier
     * @param enc string encoding
     * @param text text for this frame
     */
    public TextFrame (String id, String enc, String text) {
        frameId = id;
        type = PLAIN;
        this.enc = enc != null ? enc : "ISO-8859-1";
        this.text = text;
    }

    /**
     * Constructor for new <tt>TXXX</tt> text frame.
     * @param id typically set to "TXXX"
     * @param enc string encoding
     * @param ident text identifier
     * @param text text for this frame
     */
    public TextFrame (String id, String enc, String ident, String text) {
        frameId = id;
        type = TXXX;
        this.ident = ident;
        this.enc = enc != null ? enc : "ISO-8859-1";
        this.text = text;
    }

    /**
     * Constructor for new text frame.
     * @param id frame identifier
     * @param enc string encoding
     * @param lang language according to ISO-639-2
     * @param ident text identifier
     * @param text text for this frame
     */
    public TextFrame (String id, String enc, String lang, String ident, String text) {
        frameId = id;
        this.enc = enc != null ? enc : "ISO-8859-1";
        type = LANGUAGE;
        this.lang = lang;
        this.ident = ident;
        this.text = text;
    }

    public byte[] getBytes ()
    throws UnsupportedEncodingException {
        // translate encoding
        int encId = 0;
        if (enc.equals("ISO-8859-1")) {
            encId = 0;
        } else if (enc.equals("UTF-16")) {
            enc = "ISO-8859-1";
            encId = 0;
            System.err.println("WARNING: converted unicode to latin1");
        } else if (enc.equals("UTF-8")) {
            encId = 3;
        } else {
            enc = "ISO-8859-1";
	}

        // write data to frame
        int i = 0;
        byte[] result;
        byte[] textData = text.getBytes(enc);
        if (type == PLAIN) {
            result = new byte[1+textData.length];
            result[i++] = (byte) encId;
        } else if (type == LANGUAGE) {
            byte[] langData = lang.getBytes();
            byte[] identData = ident.getBytes();
            result = new byte[1+3+identData.length+1+textData.length];
            result[i++] = (byte) encId;
            result[i++] = langData[0];
            result[i++] = langData[1];
            result[i++] = langData[2];
            System.arraycopy(identData, 0, result, i, identData.length);
            i += identData.length;
            result[i++] = 0;
        } else if (type == TXXX) {
            byte[] identData = ident.getBytes(enc);
            int resultLen = 1+identData.length+1+textData.length;
            result = new byte[resultLen];
            result[i++] = (byte) encId;
            System.arraycopy(identData, 0, result, i, identData.length);
            i += identData.length;
            result[i++] = 0;
        } else {
            throw new RuntimeException("internal error: unknown type: "+type);
        }
        System.arraycopy(textData, 0, result, i, textData.length);

        return result;
    }

    /** @return text of this frame */
    public String getText () {
        return text;
    }
    /** @return language of this frame or <tt>null</tt> */
    public String getLanguage () {
        return lang;
    }
    /** @return identifier of this frame or <tt>null</tt> */
    public String getIdentifier () {
        return ident;
    }

    public String toString () {
        switch (type) {
        case PLAIN:
            return super.toString() + ":("+enc+") \"" + text + "\"";
        case LANGUAGE:
            return super.toString() + ":("+enc+") \"" + ident + "\"("+ lang + ") \"" + text + "\"";
        case TXXX:
            return super.toString() + ":("+enc+") \"" + ident + "\" \"" + text + "\"";
        }
        return super.toString();
    }
}
