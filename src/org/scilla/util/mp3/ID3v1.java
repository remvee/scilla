package org.scilla.util.mp3;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Access to ID3v1 (or v1.1) tag in MP3 file.
 * @see <a href="http://www.id3.org/id3v1.html">ID3 made easy</a>
 * @author Remco van 't Veer
 * @version $Revision: 1.1 $
 */
public class ID3v1
{
/// constants
///

  public static final int MAX_TITLE_LEN = 30;
  public static final int MAX_ARTIST_LEN = 30;
  public static final int MAX_ALBUM_LEN = 30;
  public static final int MAX_YEAR_LEN = 4;
  public static final int MAX_COMMENT_LEN = 30;

  /**
   * This array was taken from <code>lame-3.70/id3tag.c</code>.  It
   * maps genre codes to descriptive strings.
   */
  public static final String[] genreList = {
      "Blues", "Classic Rock", "Country", "Dance", "Disco", "Funk",
      "Grunge", "Hip-Hop", "Jazz", "Metal", "New Age", "Oldies", "Other",
      "Pop", "R&B", "Rap", "Reggae", "Rock", "Techno", "Industrial",
      "Alternative", "Ska", "Death Metal", "Pranks", "Soundtrack",
      "Euro-Techno", "Ambient", "Trip-Hop", "Vocal", "Jazz+Funk", "Fusion",
      "Trance", "Classical", "Instrumental", "Acid", "House", "Game",
      "Sound Clip", "Gospel", "Noise", "AlternRock", "Bass", "Soul", "Punk",
      "Space", "Meditative", "Instrumental Pop", "Instrumental Rock",
      "Ethnic", "Gothic", "Darkwave", "Techno-Industrial", "Electronic",
      "Pop-Folk", "Eurodance", "Dream", "Southern Rock", "Comedy", "Cult",
      "Gangsta", "Top 40", "Christian Rap", "Pop/Funk", "Jungle",
      "Native American", "Cabaret", "New Wave", "Psychadelic", "Rave",
      "Showtunes", "Trailer", "Lo-Fi", "Tribal", "Acid Punk", "Acid Jazz",
      "Polka", "Retro", "Musical", "Rock & Roll", "Hard Rock", "Folk",
      "Folk/Rock", "National Folk", "Swing", "Fast-Fusion", "Bebob",
      "Latin", "Revival", "Celtic", "Bluegrass", "Avantgarde",
      "Gothic Rock", "Progressive Rock", "Psychedelic Rock",
      "Symphonic Rock", "Slow Rock", "Big Band", "Chorus", "Easy Listening",
      "Acoustic", "Humour", "Speech", "Chanson", "Opera", "Chamber Music",
      "Sonata", "Symphony", "Booty Bass", "Primus", "Porn Groove", "Satire",
      "Slow Jam", "Club", "Tango", "Samba", "Folklore", "Ballad",
      "Power Ballad", "Rhythmic Soul", "Freestyle", "Duet", "Punk Rock",
      "Drum Solo", "A capella", "Euro-House", "Dance Hall", "Goa",
      "Drum & Bass", "Club House", "Hardcore", "Terror", "Indie", "BritPop",
      "NegerPunk", "Polsk Punk", "Beat", "Christian Gangsta", "Heavy Metal",
      "Black Metal", "Crossover", "Contemporary C", "Christian Rock",
      "Merengue", "Salsa", "Thrash Metal", "Anime", "JPop", "SynthPop",
  };


/// private variables
///

  private String artist = "";
  private String album = "";
  private String title = "";
  private String year = "";
  private String comment = "";
  private int trkn = -1;
  private int genre = 255;

  private File mp3File = null;


/// constructors
///

  /**
   * Empty tag.
   * @see #setFile(File)
   */
  public ID3v1 () {}

  /**
   * Get tag from mp3 file.
   * @param f mp3 file
   * @exception IOException when file can not be read
   */
  public ID3v1 (File f)
    throws IOException
  {
    mp3File = f;
    readTag();
  }


/// accessors
///

  public void setFile (File f)
    throws IOException
  {
    mp3File = f;
  }
  public File getFile () { return mp3File; }

  public void setTitle (String s) { title = trunc(s, MAX_TITLE_LEN); }
  public String getTitle () { return title; }

  public void setArtist (String s) { artist = trunc(s, MAX_ARTIST_LEN); }
  public String getArtist () { return artist; }

  public void setAlbum (String s) { album = trunc(s, MAX_ALBUM_LEN); }
  public String getAlbum () { return album; }

  public void setYear (String s) { year = trunc(s, MAX_YEAR_LEN); }
  public String getYear () { return year; }

  public void setComment (String s) { comment = trunc(s, MAX_COMMENT_LEN); }
  public String getComment () { return comment; }

  /**
   * Note: this is a ID3v1.1 extension, if this is set the comment
   * field will be 2 characters shorter.
   * @param i track number or -1 to unset
   */
  public void setTrkNum (int i) { trkn = i; }
  public int getTrkNum () { return trkn; }

  /**
   * @param i genre code or -1 to unset
   */
  public void setGenre (int i) { genre = i == -1 ? 255 : i; }
  public int getGenre () { return genre; }

  public void clear ()
  {
    title = "";
    artist = "";
    album = "";
    year = "";
    comment = "";
    trkn = -1;
    genre = 255;
  }


/// actions
///

  /**
   * Test if file has tag
   * @return true, if file has tag
   * @exception IOException when file can not be read
   */
  public boolean fileHasTag ()
    throws IOException
  {
    byte[] b = new byte[3];
    RandomAccessFile f = new RandomAccessFile(mp3File, "r");

    try
    {
      if (f.length() < 129) throw new IOException("Unexpected EOF");
      f.seek(f.length() - 128);

      if (f.read(b) != 3) throw new IOException("Unexpected EOF");
    }
    finally
    {
      f.close();
    }

    return (new String(b)).equals("TAG");
  }

  /**
   * @return true if is a ID3v1.1 (has a tack number)
   */
  public boolean isID3v1_1 ()
  {
    return trkn != -1;
  }

  /**
   * Read tag from file
   * @exception IOException when file can not be read
   */
  public void readTag ()
    throws IOException
  {
    if (!fileHasTag())
    {
      clear();
      return;
    }

    byte[] b = new byte[125];
    RandomAccessFile f = new RandomAccessFile(mp3File, "r");

    try
    {
      f.seek(f.length() - 125);
      if (f.read(b) != 125) throw new IOException("Unexpected EOF");
    }
    finally
    {
      f.close();
    }

    setFromID3Tag(b);
  }

  /**
   * Write tag to file
   * @exception IOException when file can not be written
   */
  public void writeTag ()
    throws IOException
  {
    RandomAccessFile f = new RandomAccessFile(mp3File, "rw");
    try
    {
      f.seek(fileHasTag() ? f.length() - 128 : f.length());
      f.write(getFromID3Tag());
    }
    finally
    {
      f.close();
    }
  }

  /**
   * @return binary version of tag
   */
  public byte[] getFromID3Tag ()
  {
    StringBuffer b = new StringBuffer("TAG");

    b.append(fixLength(title,  MAX_TITLE_LEN));
    b.append(fixLength(artist, MAX_ARTIST_LEN));
    b.append(fixLength(album,  MAX_ALBUM_LEN));
    b.append(fixLength(year,   MAX_YEAR_LEN));
    if (trkn == -1)
    {
      b.append(fixLength(comment, MAX_COMMENT_LEN));
    }
    else // ID3v1.1
    {
      b.append(fixLength(comment, MAX_COMMENT_LEN-2));
      b.append('\0');
      b.append((char)(trkn & 0xff));
    }
    b.append((char)(genre & 0xff));

    return b.toString().getBytes();
  }

  /**
   * @param b binary version of tag
   */
  public void setFromID3Tag (byte[] b)
  {
    int i = 0;
    String s = new String(b);

    title  = s.substring(i, i += MAX_TITLE_LEN).trim();
    artist = s.substring(i, i += MAX_ARTIST_LEN).trim();
    album  = s.substring(i, i += MAX_ALBUM_LEN).trim();
    year   = s.substring(i, i += MAX_YEAR_LEN).trim();

    // ID3v1.1
    comment = s.substring(i, i += MAX_COMMENT_LEN-2);
    if (s.charAt(i++) != '\0')
    {
      trkn = -1;
      comment = comment + s.substring(i - 1, ++i);
    }
    else
    {
      trkn = (byte) s.charAt(i++);
    }
    comment = comment.trim();

    genre = (int) s.charAt(i++);
  }

  /**
   * Map the genre code to a string.
   * @param i genre code
   * @return genre description or <code>null</code> if genre is not recognized
   */
  public static String getGenreString (int i)
  {
    return i >= 0 && i < genreList.length ? genreList[i] : null;
  }

  public String toString ()
  {
    return "title=\"" + getTitle() + "\"," +
	"artist=\"" + getArtist() + "\"," +
	"album=\"" + getAlbum() + "\"," +
	"year=\"" + getYear() + "\"," +
	"comment=\"" + getComment() + "\"," +
	"trkn=" + getTrkNum() + "," +
	"genre=" + getGenreString(getGenre());
  }


/// private
///

  private String trunc (String s, int n)
  {
    return s.substring(0, n < s.length() ? n : s.length());
  }

  private String fixLength (String s, int n)
  {
    StringBuffer b = new StringBuffer(s);
    b.setLength(n);
    return b.toString();
  }

  private String trimNullPadding (String s)
  {
    int i = s.indexOf('\0');
    i = i != -1 ? i : s.length();
    return s.substring(0, i);
  }


/// debugging
///

  /**
   * debugging..
   */
  public static void main (String[] args)
    throws Exception
  {
    String newTitle, newArtist, newAlbum, newYear, newComment;
    int newTrkn, newGenre;

    for (int i = 0; i < args.length; i++)
    {
      newTitle = newArtist = newAlbum = newYear = newComment = null;
      newTrkn = newGenre = -2;

      while (args[i].startsWith("-"))
      {
	if (args[i].equals("-t")) { newTitle = args[i+1]; i += 2; }
	else if (args[i].equals("-a")) { newArtist = args[i+1]; i += 2; }
	else if (args[i].equals("-l")) { newAlbum = args[i+1]; i += 2; }
	else if (args[i].equals("-y")) { newYear = args[i+1]; i += 2; }
	else if (args[i].equals("-c")) { newComment = args[i+1]; i += 2; }
	else if (args[i].equals("-g")) { newGenre = Integer.parseInt(args[i+1]); i += 2; }
	else if (args[i].equals("-n")) { newTrkn = Integer.parseInt(args[i+1]); i += 2; }
	else if (args[i].equals("-G"))
	{
	  String s = args[i+1].toLowerCase();

	  for (int j = 0; j < genreList.length; j++)
	    if (s.equals(genreList[j].toLowerCase())) newGenre = j;
	  if (newGenre == -2) throw new Exception("Genre not in list: "+args[i]);

	  i += 2;
	}
	else throw new Exception("unrecognized switch: " + args[i]);
      }

      ID3v1 t = new ID3v1(new File(args[i]));

      boolean changed = false;
      if (newTitle != null) { changed = true; t.setTitle(newTitle); }
      if (newArtist != null) { changed = true; t.setArtist(newArtist); }
      if (newAlbum != null) { changed = true; t.setAlbum(newAlbum); }
      if (newYear != null) { changed = true; t.setYear(newYear); }
      if (newComment != null) { changed = true; t.setComment(newComment); }
      if (newTrkn != -2) { changed = true; t.setTrkNum(newTrkn); }
      if (newGenre != -2) { changed = true; t.setGenre(newGenre); }
      if (changed) t.writeTag();

      System.out.println(t.toString());
    }
  }

}


/* end of $Id: ID3v1.java,v 1.1 2001/09/16 21:56:03 remco Exp $ */
