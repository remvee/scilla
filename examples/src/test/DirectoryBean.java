package test;

import java.io.*;
import java.util.*;
import org.scilla.info.*;
import org.scilla.util.*;

public class DirectoryBean {

    public void setPath (String path)
    throws Exception {
	this.path = path;

	String[] files = (new File(path)).list();
	Arrays.sort(files);

	for (int i = 0; i < files.length; i++) {
	    String fn = path + File.separator + files[i];

	    if ((new File(fn)).isDirectory()) {
		directories.add(files[i]);
	    } else {
		String type = MimeType.getTypeFromFilename(fn);
		if (type != null && type.startsWith("audio/")) {
		    AudioInfo info = (AudioInfo) InfoFactory.get(fn);
		    tracks.add(info);

		    artists.add(info.getArtist());
		    performers.add(info.getPerformer());
		    albums.add(info.getAlbum());

		    if (info.getLength() > 0) {
			length += info.getLength();
		    }
		} else if (type != null && type.startsWith("image/")) {
		    ImageInfo info = (ImageInfo) InfoFactory.get(fn);
		    images.add(info);
		}
	    }
	}
	artists.remove(null);
	performers.remove(null);
	albums.remove(null);
    }
    public String getPath () {
	return path;
    }
    private String path = null;

    public Set getArtists () {
	return artists;
    }
    public int getNumOfArtists () {
	return artists.size();
    }
    public String getArtist () {
	return (String) artists.iterator().next();
    }
    private Set artists = new HashSet();

    public Set getPerformers () {
	return performers;
    }
    public String getPerformer () {
	return (String) performers.iterator().next();
    }
    public int getNumOfPerformers () {
	return performers.size();
    }
    private Set performers = new HashSet();

    public Set getAlbums () {
	return albums;
    }
    public String getAlbum () {
	return (String) albums.iterator().next();
    }
    public int getNumOfAlbums () {
	return albums.size();
    }
    private Set albums = new HashSet();

    public int getTotalLength () {
	return length;
    }
    public String getTotalTime () {
	int hours = length / (60 * 60);
	int minutes = (length / 60) % 60;
	int seconds = length % 60;

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
    private int length = 0;

    public List getTracks () {
	return tracks;
    }
    public int getNumOfTracks () {
	return tracks.size();
    }
    private List tracks = new ArrayList();

    public List getImages () {
	return images;
    }
    public int getNumOfImages () {
	return images.size();
    }
    private List images = new ArrayList();

    public List getDirectories () {
	return directories;
    }
    public int getNumOfDirectories () {
	return directories.size();
    }
    private List directories = new ArrayList();
}
