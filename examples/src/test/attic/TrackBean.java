package test;

import java.io.*;
import java.util.*;

import org.scilla.*;
import org.scilla.info.*;

public class TrackBean {
    private String fname = null;
    private AudioInfo info = null;

    public TrackBean (String fname)
    throws Exception {
	this.fname = fname;

	String source = ConfigProvider.get().getString(Config.SOURCE_DIR_KEY);
	info = (AudioInfo) InfoFactory.get(source + File.separator + fname);
    }

    public String getFilename () {
	return fname;
    }

    public String getArtist () {
	return info.getArtist();
    }
    public String getPerformer () {
	return info.getPerformer();
    }
    public String getAlbum () {
	return info.getAlbum();
    }
    public String getTitle () {
	return info.getTitle();
    }
    public int getLength () {
	return info.getLength();
    }
    public String getTime () {
	if (getLength() >= 0) {
	    int hours = getLength() / (60 * 60);
	    int minutes = (getLength() / 60) % 60;
	    int seconds = getLength() % 60;

	    StringBuffer out = new StringBuffer();
	    if (hours > 0) {
		out.append(hours + ":");
	    }
	    if (minutes > 0) {
		if (hours > 0 && minutes < 10) {
		    out.append('0');
		}
		out.append(minutes + "");
	    }
	    out.append(":");
	    out.append(seconds < 10 ? "0" : "");
	    out.append(seconds + "");

	    return out.toString();
	}
	return "?:??";
    }
}
