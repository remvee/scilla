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
import java.io.InputStream;

import org.scilla.util.MimeType;
import org.scilla.util.mp3.*;
import org.scilla.util.mp3.id3v2.*;
import org.scilla.util.vorbis.*;

/**
 * Audio info.
 *
 * @version $Revision: 1.7 $
 * @author R.W. van 't Veer
 */
public class AudioInfo extends Info {
    public final static String BITRATE = "bitrate";
    public final static String SAMPLERATE = "samplerate";
    public final static String CHANNELS = "channels";
    public final static String LENGTH = "length";

    public final static String ALBUM = "album";
    public final static String ARTIST = "artist";
    public final static String BAND = "band";
    public final static String COMMENT = "comment";
    public final static String COMPOSER = "composer";
    public final static String CONDUCTOR = "conductor";
    public final static String GENRE = "genre";
    public final static String KEY = "key";
    public final static String LANGUAGE = "language";
    public final static String LYRICS = "lyrics";
    public final static String PERFORMER = "performer";
    public final static String RECORDING_DATE = "recdate";
    public final static String RECORDING_LOCATION = "reclocation";
    public final static String REMIXER = "remixer";
    public final static String SECTION = "section";
    public final static String SUBTITLE = "subtitle";
    public final static String TITLE = "title";
    public final static String TRKNUM = "trknum";

    public final static String CODEC_VORBIS = "OGG Vorbis Audio";

    public AudioInfo (String fname) {
	String type = MimeType.getTypeFromFilename(fname);
	if (type.endsWith("/mpeg")) {
	    setupMPEG(fname);
	} else if (type.endsWith("/ogg-vorbis")) {
	    setupVORBIS(fname);
	}
    }

    /** @return Kbits per second or <tt>-1</tt> when unknown */
    public int getBitRate () {
	return getInt(BITRATE);
    }
    /** @return number of channels or <tt>-1</tt> when unknown */
    public int getChannels () {
	return getInt(CHANNELS);
    }
    /** @return number of second this track takes or <tt>-1</tt> when unknown */
    public int getLength () {
	return getInt(LENGTH);
    }
    /** @return artist name or <tt>null</tt> when unkwown */
    public String getArtist () {
	return getString(ARTIST);
    }
    /** @return album title or <tt>null</tt> when unkwown */
    public String getAlbum () {
	return getString(ALBUM);
    }
    /** @return lead performer name or <tt>null</tt> when unkwown */
    public String getPerformer () {
	return getString(PERFORMER);
    }
    /** @return band name or <tt>null</tt> when unkwown */
    public String getBand () {
	return getString(BAND);
    }
    /** @return general comment or <tt>null</tt> when unkwown */
    public String getComment () {
	return getString(COMMENT);
    }
    /** @return composer name or <tt>null</tt> when unkwown */
    public String getComposer () {
	return getString(COMPOSER);
    }
    /** @return conductor name or <tt>null</tt> when unkwown */
    public String getConductor () {
	return getString(CONDUCTOR);
    }
    /** @return genre or <tt>null</tt> when unkwown */
    public String getGenre () {
	return getString(GENRE);
    }
    /** @return lyrics writer name or <tt>null</tt> when unkwown */
    public String getLyrics () {
	return getString(LYRICS);
    }
    /** @return location of recording or <tt>null</tt> when unkwown */
    public String getRecordingLocation () {
	return getString(RECORDING_LOCATION);
    }
    /** @return remixer name or <tt>null</tt> when unkwown */
    public String getRemixer () {
	return getString(REMIXER);
    }
    /** @return section of piece or <tt>null</tt> when unkwown */
    public String getSection () {
	return getString(SECTION);
    }
    /** @return title of track or <tt>null</tt> when unkwown */
    public String getTitle () {
	return getString(TITLE);
    }
    /** @return subtitle of track or <tt>null</tt> when unkwown */
    public String getSubtitle () {
	return getString(SUBTITLE);
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
	    setInt(LENGTH, xi.getLength());
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
		// fh.examineAll();

		setInt(BITRATE, fh.getBitRate() * 1000);
		setInt(SAMPLERATE, fh.getSampleRate());
		setInt(CHANNELS, fh.isMono() ? 1 : 2);
		setInt(LENGTH, fh.getLength());
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
		setString(RECORDING_DATE, t);
	    }

	    t = v1.getTitle();
	    if (t != null && t.trim().length() != 0) {
		setString(TITLE, t);
	    }

	    t = ID3v1.getGenreString(v1.getGenre());
	    if (t != null && t.trim().length() != 0) {
		setString(GENRE, t);
	    }

	    int trknum = v1.getTrkNum();
	    if (trknum != -1) {
		setInt(TRKNUM, trknum);
	    }
	} catch (Throwable ex) {
	    // ignore
	}

	// read id3v2 tags
	try {
	    ID3v2 v2 = new ID3v2(f);

	    String[][] keys = {
		    { "TPE1", PERFORMER },
		    { "TPE2", BAND },
		    { "TPE3", CONDUCTOR },
		    { "TPE4", REMIXER },
		    { "TALB", ALBUM },
		    { "TIT1", SECTION },
		    { "TIT2", TITLE },
		    { "TIT3", SUBTITLE },
		    // TODO: possiblity to append frames into one field
		    { "TIME", RECORDING_DATE },
		    { "TDAT", RECORDING_DATE },
		    { "TYER", RECORDING_DATE },
		    { "TCOM", COMPOSER },
		    { "TEXT", LYRICS },
		    { "TCON", GENRE },
		    { "TKEY", KEY },
		    { "TLAN", LANGUAGE },
	    };
	    for (int i = 0; i < keys.length; i++) {
		String frame = keys[i][0];
		String key = keys[i][1];

		try {
		    TextFrame tf = (TextFrame) v2.getFrame(frame);
		    if (tf != null) {
			setString(key, tf.getText());
		    }
		} catch (Exception ex) {
		    // ignore
		}
	    }

	    try {
		TextFrame tf = (TextFrame) v2.getFrame("TRCK");
		if (tf != null) {
		    String t = tf.getText();
		    if (t.indexOf("/") != -1) {
			t = t.substring(0, t.indexOf("/"));
		    }
		    setInt(TRKNUM, Integer.parseInt(t));
		}
	    } catch (Exception ex) {
		// ignore
	    }
	} catch (Throwable ex) {
	    // ignore
	}
    }

    private void setupVORBIS (String fname) {
	// read vorbis info
	InputStream in = null;
	try {
	    in = new FileInputStream(fname);
	    VorbisInfo info = new VorbisInfo(in);
	    setString(CODEC, CODEC_VORBIS);
	    setInt(BITRATE, info.getBitrate());
	    setInt(SAMPLERATE, info.getSampleRate());
	    setInt(CHANNELS, info.getChannels());
	    setInt(LENGTH, (int) info.getLength());

	    String t;
	    t = info.getComment("ALBUM");
	    if (t != null && t.trim().length() != 0) {
		setString(ALBUM, t);
	    }
	    t = info.getComment("ARTIST");
	    if (t != null && t.trim().length() != 0) {
		setString(ARTIST, t);
	    }
	    t = info.getComment("DESCRIPTION");
	    if (t != null && t.trim().length() != 0) {
		setString(COMMENT, t);
	    }
	    t = info.getComment("GENRE");
	    if (t != null && t.trim().length() != 0) {
		setString(GENRE, t);
	    }
	    t = info.getComment("PERFORMER");
	    if (t != null && t.trim().length() != 0) {
		setString(PERFORMER, t);
	    }
	    t = info.getComment("DATE");
	    if (t != null && t.trim().length() != 0) {
		setString(RECORDING_DATE, t);
	    }
	    t = info.getComment("LOCATION");
	    if (t != null && t.trim().length() != 0) {
		setString(RECORDING_LOCATION, t);
	    }
	    t = info.getComment("TITLE");
	    if (t != null && t.trim().length() != 0) {
		setString(TITLE, t);
	    }
	    try {
		setInt(TRKNUM, Integer.parseInt(info.getComment("TRACKNUMBER")));
	    } catch (Throwable ex) {
		// ignore
	    }
	} catch (Throwable ex) {
	    // ignore..
	} finally {
	    if (in != null) {
		try { in.close(); } catch (IOException ex) { /* ignore */ }
	    }
	}
    }
}
