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

package org.scilla.info;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.scilla.util.MimeType;
import org.scilla.util.mp3.*;

/**
 * Audio info.
 *
 * @version $Revision: 1.2 $
 * @author R.W. van 't Veer
 */
public class AudioInfo extends Info {
    public final static String BITRATE = "bitrate";
    public final static String SAMPLERATE = "samplerate";
    public final static String CHANNELS = "channels";
    public final static String TITLE = "title";
    public final static String GENRE = "genre";
    public final static String COMPOSER = "composer";
    public final static String PERFORMER = "performer";
    public final static String CONDUCTOR = "conductor";
    public final static String RECORDING_DATE = "recdate";
    public final static String RECORDING_YEAR = "recdate";
    public final static String RECORDING_LOCATION = "reclocation";
    public final static String ALBUM = "album";
    public final static String COMMENT = "comment";

    public final static String CODEC_OGG = "OGG Vorbis Audio";

    public AudioInfo (String fname) {
	String type = MimeType.getTypeFromFilename(fname);
	if (type.endsWith("/mpeg")) {
	    setupMPEG(fname);
	} else if (type.endsWith("/ogg-vorbis")) {
	    setupOGG(fname);
	}
    }

    /**
     * @return Kbits per second or <tt>-1</tt> when unknown.
     */
    public int getBitRate () {
	return getInt(BITRATE);
    }

    /**
     * @return number of channels or <tt>-1</tt> when unknown.
     */
    public int getChannels () {
	return getInt(CHANNELS);
    }

    private void setupMPEG (String fname) {
	File f = new File(fname);

	// read xing tag
	XingInfo xi = null;
	try {
	    xi = new XingInfo(f);
	    setInt(BITRATE, xi.getBitRate() * 1000);
	    setInt(SAMPLERATE, xi.getSampleRate());
	    setInt(CHANNELS, xi.isMono() ? 1 : 2);
	    setString(CODEC, xi.toString());
	} catch (Throwable ex) {
	    // ignore..
	} finally {
	    if (xi != null) {
		xi.close();
	    }
	}

	// read frame headers if no xing tag
	if (xi == null) {
	    FrameHeader fh = null;
	    try {
		fh = new FrameHeader(f);
		setInt(BITRATE, fh.getBitRate() * 1000);
		setInt(SAMPLERATE, fh.getSampleRate());
		setInt(CHANNELS, fh.isMono() ? 1 : 2);
		setString(CODEC, fh.toString());
	    } catch (Throwable ex) {
		// ignore..
	    } finally {
		if (fh != null) {
		    fh.close();
		}
	    }
	}

	// read id3v1 tags
	try {
	    ID3v1 v1 = new ID3v1(f);
	    String t;

	    t = v1.getArtist();
	    if (t != null && t.trim().length() != 0) {
		setString(PERFORMER, t);
	    }

	    t = v1.getAlbum();
	    if (t != null && t.trim().length() != 0) {
		setString(ALBUM, t);
	    }

	    t = v1.getComment();
	    if (t != null && t.trim().length() != 0) {
		setString(COMMENT, t);
	    }

	    t = v1.getYear();
	    if (t != null && t.trim().length() != 0) {
		setString(RECORDING_YEAR, t);
	    }

	    t = v1.getTitle();
	    if (t != null && t.trim().length() != 0) {
		setString(TITLE, t);
	    }

	    t = ID3v1.getGenreString(v1.getGenre());
	    if (t != null && t.trim().length() != 0) {
		setString(GENRE, t);
	    }
	} catch (Throwable ex) {
	    // ignore
	}

	// read id3v2 tags
	try {
	    ID3v2 v2 = new ID3v2(f);
	    // TODO
	} catch (Throwable ex) {
	    // ignore
	}
    }

    private void setupOGG (String fname) {
	// from /etc/magic:
	//   # vorbis:  file(1) magic for Ogg/Vorbis files
	//   #
	//   # From Felix von Leitner <leitner@fefe.de>
	//   # Extended by Beni Cherniavsky <cben@crosswinds.net>
	//   #
	//   # Most (everything but the number of channels and bitrate) is commented
	//   # out with `##' as it's not interesting to the average user.  The most
	//   # probable things advanced users would want to uncomment are probably
	//   # the number of comments and the encoder version.
	//   #
	//   # --- Ogg Framing ---
	//   0		string		OggS		Ogg data
	//   >4		byte		!0		UNKNOWN REVISION %u
	//   ##>4		byte		0		revision 0
	//   >4		byte		0
	//   ##>>14		lelong		x		(Serial %lX)
	//   # --- First vorbis packet - general header ---
	//   >>28		string		\x01vorbis	\b, Vorbis audio,
	//   >>>35		lelong		!0		UNKNOWN VERSION %lu,
	//   ##>>>35		lelong		0		version 0,
	//   >>>35		lelong		0
	//   >>>>39		ubyte		1		mono,
	//   >>>>39		ubyte		2		stereo,
	//   >>>>39		ubyte		>2		%u channels,
	//   >>>>40		lelong		x		%lu Hz
	//   # Minimal, nominal and maximal bitrates specified when encoding
	//   >>>>48		string		<\xff\xff\xff\xff\xff\xff\xff\xff\xff\xff\xff\xff	\b,
	//   # The above tests if at least one of these is specified:
	//   >>>>>44		lelong		!-1
	//   >>>>>>44	lelong		x		>%lu
	//   >>>>>48		lelong		!-1
	//   >>>>>>48	lelong		x		~%lu
	//   >>>>>52		lelong		!-1
	//   >>>>>>52	lelong		x		<%lu
	//   >>>>>48		string		<\xff\xff\xff\xff\xff\xff\xff\xff\xff\xff\xff\xff	kbps
	//   # -- Second vorbis header packet - the comments
	//   >>>102		string		\x03vorbis
	//   # A kludge to read the vendor string.  It's a counted string, not a
	//   # zero-terminated one, so file(1) can't read it in a generic way.
	//   # libVorbis is the only one existing currently, so I detect specifically
	//   # it.  The interesting value is the cvs date (8 digits decimal).
	//   ##>>>>113		string/c	Xiphophorus\ libVorbis\ I	\b, created by: Xiphophorus libVorbis I
	//   ##>>>>>137	string		>00000000	%.8s
	//   # Map to beta version numbers:
	//   ##>>>>>>137	string		<20000508	(<beta1 - prepublic)
	//   # The string has not changed from beta1 to 2 - they are indistinguishable.
	//   ##>>>>>>137	string		20000508	(beta1/2)
	//   ##>>>>>>137	string		>20000508
	//   ##>>>>>>>137	string		<20001031	(beta2-3)
	//   ##>>>>>>137	string		20001031	(beta3)
	//   ##>>>>>>137	string		>20001031
	//   ##>>>>>>>137	string		<20010225	(beta3-4)
	//   ##>>>>>>137	string		20010225	(beta4)
	//   ##>>>>>>137	string		>20010225
	//   ##>>>>>>>137	string		<20010615	(beta4-RC1)
	//   ##>>>>>>137	string		20010615	(RC1)
	//   ##>>>>>>137	string		>20010615	(>RC1)
	//   # Then come the comments, again length-counted (and number-counted).
	//   # Some looping constructs and registers would allow reading them but now
	//   # it's impossible.  However we can print the number of comments present
	//   # (skipping by the vendor string length):
	//   ##>>>>(109.l.113)	lelong		0		\b, no comments
	//   ##>>>>(109.l+113)	lelong		>0		\b, %lu comments
	FileInputStream in = null;
	try {
	    in = new FileInputStream(fname);
	    byte[] d = new byte[54];
	    if (in.read(d) == 54
		    && d[0] == (byte) 'O' && d[1] == (byte) 'g'
		    && d[2] == (byte) 'g' && d[3] == (byte) 'S'
		    && d[28] == (byte) 0x01 && d[29] == (byte) 'v'
		    && d[30] == (byte) 'o' && d[31] == (byte) 'r'
		    && d[32] == (byte) 'b' && d[33] == (byte) 'i'
		    && d[34] == (byte) 's') {
		setString(CODEC, CODEC_OGG);
		setInt(CHANNELS, (d[39] & 0xff));
		setInt(SAMPLERATE, ((d[43] & 0xff) << 24)
			+ ((d[42] & 0xff) << 16)
			+ ((d[41] & 0xff) << 8)
			+ (d[40] & 0xff));
		setInt(BITRATE, ((d[51] & 0xff) << 24)
			+ ((d[50] & 0xff) << 16)
			+ ((d[49] & 0xff) << 8)
			+ (d[48] & 0xff));
	    } else {
		setString(CODEC, "corrupted OGG file");
	    }
	} catch (Throwable ex) {
	    // ignore
	} finally {
	    if (in != null) {
		try {
		    in.close();
		} catch (IOException ex) {
		    // ignore
		}
	    }
	}
    }
}
