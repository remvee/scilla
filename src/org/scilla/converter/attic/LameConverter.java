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

package org.scilla.converter;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import org.scilla.*;
import org.scilla.core.*;

/**
 * The Lame Converter.
 * <P>
 * This implementation was made using LAME version 3.70
 * <P>
 * Following are the supported options from the lame --help message:
 * <DL>
 *   <DT> Filter options:
 *     <DD> <DL>
 *       <DT><CODE>nofilter</CODE> <DD> keep ALL frequencies (disables
 *         all filters)
 *       <DT><CODE>lowpass</CODE> FREQ <DD> frequency(kHz), lowpass
 *         filter cutoff above FREQ
 *       <DT><CODE>lowpasswidth</CODE> FREQ <DD> frequency(kHz),
 *         lowpass filter cutoff above FREQ
 *       <DT><CODE>highpass</CODE> FREQ <DD> frequency(kHz), highpass
 *         filter cutoff below FREQ
 *       <DT><CODE>highpasswidth</CODE> FREQ <DD> frequency(kHz) -
 *         default 15% of highpass FREQ
 *       <DT><CODE>resample</CODE> FREQ <DD> sampling  frequency  of
 *         output file(kHz)- default=input sfreq
 *       <DT><CODE>cwlimit</CODE> FREQ <DD> sampling  frequency  of
 *         output file(kHz)- default=input sfreq
 *     </DL>
 *   <DT> Operational options:
 *     <DD> <DL>
 *       <DT><CODE>mode</CODE> MODE <DD> (s)tereo, (j)oint, (f)orce or
 *         (m)ono (default j) force = force ms_stereo on all frames.
 *         Faster
 *       <DT><CODE>preset</CODE> TYPE <DD> type must be phone, voice,
 *         fm, tape, hifi, cd or studio help gives some more infos on
 *         these
 *     </DL>
 *   <DT> CBR (constant bitrate, the default) options:
 *     <DD> <DL>
 *       <DT><CODE>bitrate</CODE> RATE <DD> set the bitrate, default
 *         128kbps
 *       <DT><CODE>quality</CODE> HIGH|LOW <DD> higher quality, but a
 *         little slower.  Recommended. fast mode (very low quality)
 *     </DL>
 *   <DT> VBR options:
 *     <DD> <DL>
 *       <DT><CODE>vbr</CODE> <DD> use variable bitrate (VBR)
 *       <DT><CODE>vbrquality</CODE> N <DD> quality setting for VBR.
 *         default n=4, 0=high quality,bigger files. 9=smaller files
 *       <DT><CODE>minbitrate</CODE> N <DD> specify minimum allowed
 *         bitrate, default 32kbs
 *       <DT><CODE>maxbitrate</CODE> N <DD> specify maximum allowed
 *         bitrate, default 256kbs
 *       <DT><CODE>noxing</CODE> <DD> disable Xing VBR informational tag
 *     </DL>
 *   <DT> MP3 header/stream options:
 *     <DD> <DL>
 *       <DT><CODE>copyright</CODE> <DD> mark as copyright
 *       <DT><CODE>original</CODE> <DD> mark as original
 *       <DT><CODE>checksum</CODE> <DD> error protection. adds 16bit
 *         checksum to every frame
 *       <DT><CODE>nores</CODE> <DD> disable the bit reservoir
 *     </DL>
 *   <DT> Specifying any of the following options will add an ID3 tag:
 *     <DD> <DL>
 *       <DT><CODE>title</CODE> STR <DD>title of song (max 30 chars)
 *       <DT><CODE>artist</CODE> STR <DD>artist who did the song (max
 *         30 chars)
 *       <DT><CODE>album</CODE> STR <DD>album where it came from (max
 *         30 chars)
 *       <DT><CODE>year</CODE> STR <DD>year in which the song/album was
 *         made (max 4 chars)
 *       <DT><CODE>comment</CODE> STR <DD> additional info (max 30
 *         chars)
 *       <DT><CODE>tracknum</CODE> N <DD> track number of the song on
 *         the CD (1 to 99)
 *       <DT><CODE>genre</CODE> N|STR <DD> genre of song (name or
 *         number)
 *     </DL>
 * </DL>
 * <BR>MPEG1 samplerates(kHz): 32 44.1 48 
 * <BR>bitrates(kbs): 32 40 48 56 64 80 96 112 128 160 192 224 256 320 
 * <BR>
 * <BR>MPEG2 samplerates(kHz): 16 22.05 24 
 * <BR>bitrates(kbs): 8 16 24 32 40 48 56 64 80 96 112 128 144 160 
 *
 * @version $Revision: 1.5 $
 * @see <A href="http://www.sulaco.org/mp3/">The LAME Project</A>
 * @author R.W. van 't Veer
 */

public class LameConverter extends Converter
{
    /** parameter name to force the use of this converter */
    public final static String THIS_CONVERTER_PARAMETER = "lame";
    public final static String LAME_EXEC_PROPERTY = "LameConverter.exec";

    /**
     * Create a Lame converter object.
     */
    public LameConverter ()
    {
	super();
	inputTypeList = new String[] {
		"audio/mp3", "audio/mpeg", "audio/wav", "audio/x-wav" };
	outputTypeList = new String[] { "audio/mp3", "audio/mpeg" };
	parameterList = new String[] {
		THIS_CONVERTER_PARAMETER, Request.OUTPUT_TYPE_PROPERTY,
		"album", "artist", "bitrate", "checksum", "comment",
		"copyright", "cwlimit", "genre", "highpass",
		"highpasswidth", "lowpass", "lowpasswidth",
		"maxbitrate", "minbitrate", "mode", "nofilter",
		"nores", "noxing", "original", "preset", "quality",
		"resample", "title", "tracknum", "vbr", "vbrquality",
		"year"
	};
    }

    /**
     * Start conversion.
     */
    public void convert ()
    {
	// create command line
	String[] cmdLine = createCmdLine();

	// run system command
	try
	{
	    QueuedProcess proc = new QueuedProcess(cmdLine);
	    /* discard */ proc.exitValue();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    /**
     * Determine if <CODE>lame</CODE> executable exists.
     * @see #LAME_EXEC_PROPERTY
     * @see org.scilla.Config
     */
    public boolean isFunctional ()
    {
	File f = new File(Config.getParameter(LAME_EXEC_PROPERTY));
	return f.exists();
    }

    /**
     * Construct command line from request parameters
     * @return request parameters
     */
    String[] createCmdLine ()
    {
	boolean original = false;

	// create command line
	Vector v = new Vector();
	v.add(Config.getParameter(LAME_EXEC_PROPERTY));

	// added conversion parameters to command line
	for (Iterator it = pars.iterator(); it.hasNext(); )
	{
	    RequestParameter rp = (RequestParameter) it.next();

	    if (rp.key.equals("album"))
	    {
		v.add("--tl");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("artist"))
	    {
		v.add("--ta");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("bitrate"))
	    {
		v.add("-b");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("checksum"))
	    {
		v.add("-p");
	    }
	    else if (rp.key.equals("comment"))
	    {
		v.add("--tc");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("copyright"))
	    {
		v.add("-c");
	    }
	    else if (rp.key.equals("cwlimit"))
	    {
		v.add("--cwlimit");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("genre"))
	    {
		v.add("--tg");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("highpass"))
	    {
		v.add("--highpass");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("highpasswidth"))
	    {
		v.add("--highpasswidth");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("lowpass"))
	    {
		v.add("--lowpass");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("lowpasswidth"))
	    {
		v.add("--lowpasswidth");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("maxbitrate"))
	    {
		v.add("-B");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("minbitrate"))
	    {
		v.add("-b");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("mode"))
	    {
		v.add("-m");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("nofilter"))
	    {
		v.add("-k");
	    }
	    else if (rp.key.equals("nores"))
	    {
		v.add("--nores");
	    }
	    else if (rp.key.equals("noxing"))
	    {
		v.add("-t");
	    }
	    else if (rp.key.equals("original"))
	    {
		original = true;
	    }
	    else if (rp.key.equals("preset"))
	    {
		v.add("--preset");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("quality"))
	    {
		String val = rp.val;
		if (val.toLowerCase().startsWith("l")
			|| val.toLowerCase().startsWith("f")
			|| val.toLowerCase().startsWith("0"))
		{
		    v.add("-f");
		}
		else
		{
		    v.add("-h");
		}
	    }
	    else if (rp.key.equals("resample"))
	    {
		v.add("--resample");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("title"))
	    {
		v.add("--tt");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("tracknum"))
	    {
		v.add("--tn");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("vbr"))
	    {
		v.add("-v");
	    }
	    else if (rp.key.equals("vbrquality"))
	    {
		v.add("-V");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("year"))
	    {
		v.add("--ty");
		v.add(rp.val);
	    }
	    else if (rp.key.equals(Request.OUTPUT_TYPE_PROPERTY))
	    {
		// handled outside this loop
	    }
	    else if (rp.key.equals(THIS_CONVERTER_PARAMETER))
	    {
		// force use of this converter; ignore
	    }
	    else
	    {
		System.err.println("LameConverter.convert: param '"+rp.key+"' NOT YET IMPLEMENTED");
	    }
	}

	// input file
	if (!inputType.equals("audio/x-wav") && !inputType.equals("audio/wav"))
	{
	    v.add("--mp3input");
	}
	v.add(inputFile);

	// handle original flag
	if (!original) v.add("-o");

	// keep it quiet
	v.add("-S");

	// and the output file
	v.add(outputFile);

	return (String[]) v.toArray(new String[0]);
    }
}
