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

import org.scilla.util.MimeType;
import org.scilla.util.mp3.*;

/**
 * Audio info.
 *
 * @version $Revision: 1.1 $
 * @author R.W. van 't Veer
 */
public class AudioInfo extends Info {
    public final static String BITRATE = "bitrate";
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

    public AudioInfo (String fname) {
	String type = MimeType.getTypeFromFilename(fname);
	if (type.endsWith("/mpeg")) {
	    setupMPEG(fname);
	} else if (type.endsWith("/ogg-vorbis")) {
	    setupOGG(fname);
	}
    }

    private void setupMPEG (String fname) {
	File f = new File(fname);

	// read xing tag
	XingInfo xi = null;
	try {
	    xi = new XingInfo(f);
	    setInt(BITRATE, xi.getBitRate());
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
	if (xi != null) {
	    FrameHeader fh = null;
	    try {
		fh = new FrameHeader(f);
		setInt(BITRATE, fh.getBitRate());
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
	// determine biterate etc.
	// TODO
	// read clip info
	// TODO
    }
}
