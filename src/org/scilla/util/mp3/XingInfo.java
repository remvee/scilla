package org.scilla.util.mp3;

import java.io.*;

/**
 * Read-only access the Xing info tag in an MP3 file.
 *
 * @author Remco van 't Veer <rwvtveer@xs4all.nl>
 * @version $Revision: 1.2 $
 * @see <CODE>xmms-1.2.4/Input/mpg123/dxhead.c</CODE>
 */
public class XingInfo extends FrameHeader
{

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
   * @throws MP3Exception when tag not present
   */
  public XingInfo (File f) throws IOException, Mp3Exception
  {
    super(f);

    // forward to Xing info
    mp3File.skipBytes(isMpegVersion1()
	  ? (isSingleChannel() ? 17 : 32)
	  : (isSingleChannel() ? 9 : 17));

    // test if tag is present
    byte[] tag = new byte[4];
    mp3File.read(tag);
    if (! (new String(tag)).equals("Xing"))
    {
      throw new Mp3Exception("no tag present");
    }

    vbrFlag = true;

    // extract data
    flags = extractI4(mp3File);

    if ((flags & FRAMES_FLAG) != 0) frames = extractI4(mp3File);

    if ((flags & BYTES_FLAG) != 0) bytes = extractI4(mp3File);

    if ((flags & TOC_FLAG) != 0)
    {
      toc = new byte[100];
      for (int i = 0; i < 100; i++)
      {
	toc[i] = mp3File.readByte();
      }
    }

    if ((flags & VBR_SCALE_FLAG) != 0) vbrscale = extractI4(mp3File);

    tpf = tpfbs[layerToInt()] / getSampleRate();
    if (isMpegVersion25() || isMpegVersion2()) tpf /= 2;
  }


/// accessors
///

  /** @return number of frames */
  public int getFrames () { return frames; }

  /** @return number of data bytes */
  public int getBytes () { return bytes; }

  /** @return variable bitrate scale */
  public int getVbrScale () { return vbrscale; }

  /** @return table of content */
  public byte[] getToc () { return toc; }

  /** @return length in seconds */
  public int getLength ()
  {
    double n = tpf * (double) frames;
    return (int) n;
  }

  /** @return average bitrate */
  public int getBitRate ()
  {
    return (int) ( (bytes * 8) / (tpf * frames * 1000) );
  }


/// private functions
///

  private static int extractI4 (RandomAccessFile f)
      throws IOException
  {
    int n = 0;
    final byte[] b = new byte[4]; f.read(b);
    
    n |= b[0] & 0xff; n <<= 8;
    n |= b[1] & 0xff; n <<= 8;
    n |= b[2] & 0xff; n <<= 8;
    n |= b[3] & 0xff;

    return n;
  }


/// debugging
///

  /**
   * debugging..
   */
  public static void main (String[] args)
    throws Exception
  {
    for (int i = 0; i < args.length; i++)
    {
      XingInfo h = new XingInfo(new File(args[i]));
      System.out.println(args[i] + ":\n  " + h);
    }
  }
}

/* end of $Id: XingInfo.java,v 1.2 2001/09/21 12:38:27 remco Exp $ */
