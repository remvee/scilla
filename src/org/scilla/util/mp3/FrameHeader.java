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

import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.EOFException;

import org.scilla.Logger;
import org.scilla.LoggerFactory;

/**
 * Read only access to MP3 frame header information.
 * @see <a href="http://www.mp3-tech.org/programmer/frame_header.html">MP3'Tech - Frame header</a>
 * @author Remco van 't Veer
 * @version $Revision: 1.9 $
 */
public class FrameHeader
{
/// constants
///
    private static final boolean verbose = true;

    /**
     * MPEG Version 2.5 (later extension of MPEG 2)
     */
    public static final int MPEG_VERSION_2_5 = 0;
    /**
     * MPEG Version 2 (ISO/IEC 13818-3)
     */
    public static final int MPEG_VERSION_2 = 2;
    /**
     * MPEG Version 1 (ISO/IEC 11172-3)
     */
    public static final int MPEG_VERSION_1 = 3;
    public static final int LAYER_III = 1;
    public static final int LAYER_II = 2;
    public static final int LAYER_I = 3;
    public static final int CHANNEL_MODE_STEREO = 0;
    public static final int CHANNEL_MODE_JOINT_STEREO = 1;
    public static final int CHANNEL_MODE_DUAL_CHANNEL = 2;
    public static final int CHANNEL_MODE_SINGLE_CHANNEL = 3;
    public static final int EMPHASIS_NONE = 0;
    public static final int EMPHASIS_50_15_MS = 1;
    public static final int EMPHASIS_CCIT_J_17 = 3;

    static int[][] bitRateTable =
    {
	// V1,L1 V1,L2 V1,L3 V2,L1 V2,L2,L3
	{   0,    0,    0,    0,     0 },
	{  32,   32,   32,   32,     8 },
	{  64,   48,   40,   48,    16 },
	{  96,   56,   48,   56,    24 },
	{ 128,   64,   56,   64,    32 },
	{ 160,   80,   64,   80,    40 },
	{ 192,   96,   80,   96,    48 },
	{ 224,  112,   96,  112,    56 },
	{ 256,  128,  112,  128,    64 },
	{ 288,  160,  128,  144,    80 },
	{ 320,  192,  160,  160,    96 },
	{ 352,  224,  192,  176,   112 },
	{ 384,  256,  224,  192,   128 },
	{ 416,  320,  256,  224,   144 },
	{ 448,  384,  320,  256,   160 },
	{  -1,   -1,   -1,   -1,    -1 },
    };

    final static int[][] sampleRateTable =
    {
	// MPEG1  MPEG2  MPEG2.5
	{  44100, 22050, 11025 },
	{  48000, 24000, 12000 },
	{  32000, 16000,  8000 },
	{      0,     0,     0 },
    };


/// private variables
///

    RandomAccessFile mp3File;
    long fileLength;

    int maxSearch = 32*1024; // max bytes to search for frame

    // values from header
    int     mpegVersion;
    int     layer;
    boolean errorProtection;
    int     bitRateIndex;
    int     sampleRateIndex;
    boolean paddingBit;
    boolean privateBit;
    int     channelMode;
    int     modeExtension;
    boolean copyrightBit;
    boolean originalBit;
    int     emphasis;

    // calculated values
    int  bitRate;
    int  sampleRate;
    long frameSize;

    // VBR info
    int length         = -1;
    int averageBitRate = -1;
    int roundBitRate   = -1;
    boolean vbrFlag;


/// constructors
///

    /**
     * Read first frame header in file and derive MP3 properties from it.
     * @see #close()
     * @param f mp3 file
     * @throws IOException when file not readable
     * @throws Mp3Exception when no frameheader found
     */
    public FrameHeader (File f)
	throws IOException, Mp3Exception
    {
	mp3File = new RandomAccessFile(f, "r");
	fileLength = mp3File.length();

	next(false); // hit first frame
    }

    protected void finalize () throws Throwable
    {
	try { close(); } finally { super.finalize(); }
    }


/// accessors
///

    /**
     * @return bitrate or highest bitrate after <code>examineAll()</code>
     * @see #examineAll()
     */
    public int getBitRate ()
    {
	return roundBitRate != -1 ? roundBitRate : bitRate;
    }

    public int getAverageBitRate () { return averageBitRate; }

    public int getSampleRate ()
    {
	return sampleRate;
    }

    /**
     * @return estimated length in seconds or after
     * <code>examineAll()</code> real length
     * @see #examineAll()
     */
    public int getLength ()
    {
	return length != -1
		? length
		: (int) (fileLength / (bitRate * 125));
    }

    /**
     * @return MPEG_VERSION_1, MPEG_VERSION_2 or MPEG_VERSION_25
     */
    public int getMpegVersion () { return mpegVersion; }
    public boolean isMpegVersion1 () { return mpegVersion == MPEG_VERSION_1; }
    public boolean isMpegVersion2 () { return mpegVersion == MPEG_VERSION_2; }
    public boolean isMpegVersion25 () { return mpegVersion == MPEG_VERSION_2_5; }
    public String mpegVersionToString ()
    {
	return isMpegVersion1() ? "1" :
		(isMpegVersion2() ? "2" : (isMpegVersion2() ? "2.5" : null));
    }
     
    /**
     * @return LAYER_I, LAYER_II or LAYER_III
     */
    public int getLayer () { return layer; }
    public boolean isLayerI () { return layer == LAYER_I; }
    public boolean isLayerII () { return layer == LAYER_II; }
    public boolean isLayerIII () { return layer == LAYER_III; }
    public int layerToInt ()
    {
	return isLayerI() ? 1 : (isLayerII() ? 2 : (isLayerIII() ? 3 : 0));
    }

    /**
     * @return CHANNEL_MODE_STEREO, CHANNEL_MODE_JOINT_STEREO,
     * CHANNEL_MODE_DUAL_CHANNEL or CHANNEL_MODE_SINGLE_CHANNEL
     */
    public int getChannelMode () { return channelMode; }
    public boolean isStereo () { return channelMode == CHANNEL_MODE_STEREO; }
    public boolean isJointStereo () { return channelMode == CHANNEL_MODE_JOINT_STEREO; }
    public boolean isDualChannel () { return channelMode == CHANNEL_MODE_DUAL_CHANNEL; }
    public boolean isSingleChannel () { return channelMode == CHANNEL_MODE_SINGLE_CHANNEL; }
    /**
     * Same as <code>isSingleChannel()</code>
     * @see #isSingleChannel()
     */
    public boolean isMono () { return isSingleChannel(); }
    public String channelModeToString ()
    {
	return isStereo() ? "stereo" : (isJointStereo() ? "j-stereo" :
		(isDualChannel() ? "dual-ch" : (isSingleChannel() ? "single-ch" : null)));
    }

    public int getModeExtension () { return modeExtension; }

    public int getEmphasis () { return emphasis; }
    public boolean needEmphasis () { return emphasis != 0; }

    public boolean hasErrorProtection () { return errorProtection; }
    public boolean hasPadding () { return paddingBit; }
    public boolean isPrivate () { return privateBit; }
    public boolean isOriginal () { return originalBit; }
    public boolean isCopyright () { return copyrightBit; }

    /**
     * Determine if MP3 file has a variable bitrate (VBR) by reading
     * frame headers and comparing bitrates.    It will not examine the
     * file when <code>examineAll()</code> already did. Note: it is
     * faster to determine a file is VBR than proving is is not.
     * @param n maximum number of frames to read, -1 reads all frames
     * and 0 ensures the file is not touched
     * @return true if MP3 file is a VBR
     * @exception when file access fails
     * @see #examineAll()
     */
    public boolean isVbr (int n)
	throws IOException, Mp3Exception
    {
	if (length != -1 || n == 0) return vbrFlag;

	mp3File.seek(0);
	next();
	int firstBitRate = bitRate;

	vbrFlag = false; // until proven otherwise
	boolean eof = false;
	for (int i = 1; !eof && (n == -1 || i < n); i++)
	{
	    try
	    {
		next();
		if (bitRate != firstBitRate)
		{
		    vbrFlag = true;
		    break;
		}
	    }
	    catch (EOFException e)
	    {
		eof = true;
	    }
	}
	
	return vbrFlag;
    }
    public boolean isVbr ()
    {
	try { return isVbr(0); } catch (Exception e) { }
	return false;
    }


/// actions
///

    /**
     * Close MPEG file.
     */
    public void close ()
    {
	if (mp3File != null)
	{
	    try { mp3File.close(); }
	    catch (IOException e) { }
	    finally { mp3File = null; }
	}
    }

    /**
     * Scan through file to count the number of frames and determine the
     * play time.    Note: this function is especially needed for variable
     * bitrate (VBR) files.
     * @exception when file access fails
     */
    public void examineAll ()
	throws IOException, Mp3Exception
    {
	vbrFlag = false; // until proven otherwise

	mp3File.seek(0);
	next();

	int firstBitRate = bitRate;
	int n = 0; int c = 0; int b = 0;

	boolean eof = false;
	while (!eof)
	{
	    try
	    {
		n++; c += frameSize; b += bitRate;
		if (bitRate != firstBitRate && !vbrFlag) vbrFlag = true;

		next();
	    }
	    catch (EOFException e)
	    {
		eof = true;
	    }
	}

	length = c / ((b * 125) / n);
	averageBitRate = b / n;

	// find closest valid bitrate
	int column =
		 isMpegVersion1()  && isLayerI()   ? 0 :
		(isMpegVersion1()  && isLayerII()  ? 1 :
		(isMpegVersion1()  && isLayerIII() ? 2 :
		(isMpegVersion2()  && isLayerI()   ? 3 :
		(isMpegVersion25() && isLayerI()   ? 3 :
		(isMpegVersion2()  && isLayerII()  ? 4 :
		(isMpegVersion25() && isLayerII()  ? 4 :
		(isMpegVersion2()  && isLayerIII() ? 4 :
		(isMpegVersion25() && isLayerIII() ? 4 : -1
		))))))));

	// DEPEND ON rate increments over row in bitRateTable
	int i;
	for (i = 1; i < 15; i++)
	    if (bitRateTable[i][column] > averageBitRate) break;

	int d1 = (averageBitRate - bitRateTable[i-1][column]);
	int d2 = (bitRateTable[i][column] - averageBitRate);
	roundBitRate =    d1 < d2 ?
		    bitRateTable[i-1][column]
		: bitRateTable[i][column];
    }

    public String toString ()
    {
	StringBuffer b = new StringBuffer();
	b.append("MPEG-" + mpegVersionToString() + " ");
	if (isLayerIII()) b.append("Layer III ");
	if (isLayerII()) b.append("Layer II ");
	if (isLayerI()) b.append("Layer I ");
	if (hasErrorProtection()) b.append("CRC ");
	if (isVbr()) b.append("VBR avg. bitrate ");
	b.append(getBitRate() + "Kbit/s ");
	b.append(getSampleRate() + "Hz ");
	if (isPrivate()) b.append("Private ");
	b.append(channelModeToString() + " ");
	if (isCopyright()) b.append("Copyright ");
	if (isOriginal()) b.append("Original ");
	if (getEmphasis() == EMPHASIS_50_15_MS) b.append("50/15 ms ");
	if (getEmphasis() == EMPHASIS_CCIT_J_17) b.append("CCIT J.17 ");
	int l = getLength();
	b.append(l / 60 + ":" + (l % 60 < 10 ? "0" : "") + l % 60 + " ");
	return b.toString().trim();
    }


/// private functions
///

    void next ()
	throws IOException, Mp3Exception
    {
	next(true);
    }

    void next (boolean forwardToNextFrame)
	throws IOException, Mp3Exception
    {
	int i;

	// forward to next header
	int l = 0;
	for (i = 0; i < maxSearch && (l & 0xffe00000) != 0xffe00000; i++)
	{
	    l <<= 8;
	    l |= mp3File.readByte() & 0xff;
	}
	if (i == maxSearch) throw new Mp3Exception("no sync frame found");

	// read header
	mpegVersion     = ((l >> 19) & 0x3);
	layer           = ((l >> 17) & 0x3);
	errorProtection = ((l >> 16) & 0x1) == 0;
	bitRateIndex    = ((l >> 12) & 0xF);
	sampleRateIndex = ((l >> 10) & 0x3);
	paddingBit      = ((l >> 9)  & 0x1) == 1;
	privateBit      = ((l >> 8)  & 0x1) == 1;
	channelMode     = ((l >> 6)  & 0x3);
	modeExtension   = ((l >> 4)  & 0x3);
	copyrightBit    = ((l >> 3)  & 0x1) == 1;
	originalBit     = ((l >> 2)  & 0x1) == 1;
	emphasis        = ( l        & 0x3);

	// see if it doesn't contain reserved values
	if (       mpegVersion == 1
		|| layer == 0
		|| bitRateIndex == 0
		|| bitRateIndex == 15
		|| sampleRateIndex == 3
		|| emphasis == 2
	     )
	{
	    if (verbose) System.err.println("next: RESERVED VALUES");
	    mp3File.seek(mp3File.getFilePointer()-3);
	    next(forwardToNextFrame);
	}

	// determine bitrate
	i =      isMpegVersion1()  && isLayerI()   ? 0 :
		(isMpegVersion1()  && isLayerII()  ? 1 :
		(isMpegVersion1()  && isLayerIII() ? 2 :
		(isMpegVersion2()  && isLayerI()   ? 3 :
		(isMpegVersion25() && isLayerI()   ? 3 :
		(isMpegVersion2()  && isLayerII()  ? 4 :
		(isMpegVersion25() && isLayerII()  ? 4 :
		(isMpegVersion2()  && isLayerIII() ? 4 :
		(isMpegVersion25() && isLayerIII() ? 4 : -1
		))))))));
	bitRate = bitRateTable[bitRateIndex][i];

	// see if it doesn't contain illegal combinations
	if (       bitRate ==  32 && isStereo()
		|| bitRate ==  32 && isDualChannel()
//              || bitRate ==  48 && isStereo()                 // lame
		|| bitRate ==  48 && isDualChannel()
//              || bitRate ==  56 && isStereo()                 // lame
		|| bitRate ==  56 && isDualChannel()
//              || bitRate ==  80 && isStereo()                 // lame
		|| bitRate ==  80 && isDualChannel()
		|| bitRate == 224 && isSingleChannel()
		|| bitRate == 256 && isSingleChannel()
		|| bitRate == 320 && isSingleChannel()
		|| bitRate == 384 && isSingleChannel()
	     )
	{
	    if (verbose) System.err.println("next: ILLEGAL COMBINATION");
	    mp3File.seek(mp3File.getFilePointer()-3);
	    next(forwardToNextFrame);
	}

	// determine samplerate
	i =      isMpegVersion1()  ? 0 :
		(isMpegVersion2()  ? 1 :
		(isMpegVersion25() ? 2 : -1
		));
	sampleRate = sampleRateTable[sampleRateIndex][i];

	// calculate frame size (including header)
	// derived from mpg123-0.59r/common.c
	switch (layer)
	{
	    case LAYER_I:
		frameSize  = bitRate * 12000;
		frameSize /= sampleRate;
		frameSize += (paddingBit ? 1 : 0);
		frameSize *= 4;
		break;
	    case LAYER_II:
		frameSize  = bitRate * 144000;
		frameSize /= sampleRate;
		frameSize += (paddingBit ? 1 : 0);
		break;
	    case LAYER_III:
		frameSize  = bitRate * 144000;
		frameSize /= sampleRate * (isMpegVersion1() ? 1 : 2);
		frameSize += (paddingBit ? 1 : 0);
		break; 
	}

	// set file pointer just before next frame header
	if (forwardToNextFrame) mp3File.seek(mp3File.getFilePointer() + frameSize - 4);
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
	    FrameHeader h = new FrameHeader(new File(args[i]));
	    if (h.isVbr(20)) h.examineAll();
	    System.out.println(args[i] + ":\n    " + h);
	}
    }
}


/* end of $Id: FrameHeader.java,v 1.9 2002/02/16 16:37:41 remco Exp $ */
