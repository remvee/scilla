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
 * The FFMpeg Converter.
 * <P>
 * This implementation was made using FFMpeg version 0.4.5
 * <P>
 * <DL>
 *   <DT> Filter options:
 *     <DD> <DL>
 *       <DT><CODE>duration</CODE> <DD> length of clip from beginning
 *           of input
 *       <DT><CODE>title</CODE> <DD> title of clip
 *       <DT><CODE>author</CODE> <DD> author of clip
 *       <DT><CODE>copyright</CODE> <DD> copyright of clip
 *       <DT><CODE>comment</CODE> <DD> comment of clip
 *       <DT><CODE>vbitrate</CODE> <DD> video bitrate in kbit/s
 *       <DT><CODE>framerate</CODE> <DD> framerate in Hz
 *       <DT><CODE>framesize</CODE> <DD> frame size (WxH)
 *       <DT><CODE>novideo</CODE> <DD> suppress video in output
 *       <DT><CODE>abitrate</CODE> <DD> audio bitrate in kbit/s
 *       <DT><CODE>asamplerate</CODE> <DD> audio sampling rate in Hz
 *       <DT><CODE>achannels</CODE> <DD> number of audio channels
 *       <DT><CODE>noaudio</CODE> <DD> suppress audio in output
 *       <DT><CODE>gobsize</CODE> <DD> set the group of picture size
 *       <DT><CODE>intra</CODE> <DD> use only intra frames (no
 *           parameters)
 *       <DT><CODE>qscale</CODE> <DD> use fixed video quantiser scale
 *           (0-31)
 *       <DT><CODE>vcodec</CODE> <DD> force video codec (see
 *           <CODE>ffmpeg -formats</CODE> for supported codecs)
 *       <DT><CODE>acodec</CODE> <DD> force audio codec (see
 *           <CODE>ffmpeg -formats</CODE> for supported codecs)
 *       <DT><CODE>deinterlace</CODE> <DD> deinterlace pictures (no
 *           parameters)
 *     </DL>
 * </DL>
 *
 * @version $Revision: 1.2 $
 * @see <A href="http://ffmpeg.sourceforge.net/">FFMpeg Streaming
 * Multimedia System</A>
 * @author R.W. van 't Veer
 */

public class FFMpegConverter extends Converter
{
    /** parameter name to force the use of this converter */
    public final static String THIS_CONVERTER_PARAMETER = "ffmpeg";
    public final static String FFMPEG_EXEC_PROPERTY = "FFMpegConverter.exec";

    QueuedProcess proc = null;
    int exitValue = -1; // 0 means success

    /**
     * Create a FFMpeg converter object.
     */
    public FFMpegConverter ()
    {
	super();
	inputTypeList = new String[] {
		"video/mpeg", "video/x-msvideo",  "video/x-ms-asf" };
	outputTypeList = new String[] {
		"video/mpeg", "video/x-msvideo",  "video/x-ms-asf",
		"video/vnd.rn-realvideo",
		"audio/mpeg", "audio/x-pn-realaudio"
	};
	parameterList = new String[] {
		THIS_CONVERTER_PARAMETER, Request.OUTPUT_TYPE_PARAMETER,
		"duration", "title", "author", "copyright", "comment",
		"vbitrate", "framerate", "framesize", "novideo",
		"abitrate", "asamplerate", "achannels", "noaudio",
		"gobsize", "intra", "qscale", "vcodec", "acodec",
		"deinterlace"
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
	    proc = new QueuedProcess(cmdLine);
	    exitValue = proc.exitValue();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    /**
     * Determine if <CODE>ffmpeg</CODE> executable exists.
     * @see #FFMPEG_EXEC_PROPERTY
     * @see org.scilla.Config
     */
    public boolean isFunctional ()
    {
	File f = new File(Config.getParameter(FFMPEG_EXEC_PROPERTY));
	return f.exists();
    }

    /**
     * @return true if exit successfull
     */
    public boolean exitSuccess ()
    {
	if (proc == null || isAlive()) throw new IllegalStateException();
	return exitValue == 0;
    }

    /**
     * @return error message
     */
    public String getErrorMessage ()
    {
	if (proc == null || isAlive()) throw new IllegalStateException();
	return proc.getErrorLog();
    }

    /**
     * Construct command line from request parameters
     * @return request parameters
     */
    String[] createCmdLine ()
    {
	// create command line
	Vector v = new Vector();
	v.add(Config.getParameter(FFMPEG_EXEC_PROPERTY));

	// input file
	v.add("-i");
	v.add(inputFile);

	// added conversion parameters to command line
	for (Iterator it = pars.iterator(); it.hasNext(); )
	{
	    RequestParameter rp = (RequestParameter) it.next();

	    if (rp.key.equals("duration"))
	    {
		v.add("-t");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("title"))
	    {
		v.add("-title");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("author"))
	    {
		v.add("-author");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("copyright"))
	    {
		v.add("-copyright");
	    }
	    else if (rp.key.equals("comment"))
	    {
		v.add("-comment");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("vbitrate"))
	    {
		v.add("-b");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("framerate"))
	    {
		v.add("-r");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("framesize"))
	    {
		v.add("-s");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("novideo"))
	    {
		v.add("-vn");
	    }
	    else if (rp.key.equals("abitrate"))
	    {
		v.add("-ab");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("asamplerate"))
	    {
		v.add("-ar");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("achannels"))
	    {
		v.add("-ac");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("noaudio"))
	    {
		v.add("-an");
	    }
	    else if (rp.key.equals("gobsize"))
	    {
		v.add("-g");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("intra"))
	    {
		v.add("-intra");
	    }
	    else if (rp.key.equals("qscale"))
	    {
		v.add("-qscale");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("vcodec"))
	    {
		v.add("-vcodec");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("acodec"))
	    {
		v.add("-acodec");
		v.add(rp.val);
	    }
	    else if (rp.key.equals("deinterlace"))
	    {
		v.add("-deinterlace");
	    }
	    else if (rp.key.equals(Request.OUTPUT_TYPE_PARAMETER))
	    {
		// handled outside this loop
	    }
	    else if (rp.key.equals(THIS_CONVERTER_PARAMETER))
	    {
		// force use of this converter; ignore
	    }
	    else
	    {
		System.err.println("FFMpegConverter.convert: param '"+rp.key+"' NOT YET IMPLEMENTED");
	    }
	}

	// drop video output when audio requested
	if (outputType.startsWith("audio/"))
	{
	    v.add("-vn");
	}

	// and the output file
	v.add(outputFile);

	return (String[]) v.toArray(new String[0]);
    }
}
