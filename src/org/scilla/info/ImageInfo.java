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

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.scilla.util.MimeType;

/**
 * Image info.
 *
 * @version $Revision: 1.1 $
 * @author R.W. van 't Veer
 */
public class ImageInfo extends Info {
    public final static String WIDTH = "width";
    public final static String HEIGHT = "height";

    public ImageInfo (String fname) {
	String type = MimeType.getTypeFromFilename(fname);

	if (type.endsWith("/png")) {
	    setupPNG(fname);
	} else if (type.endsWith("/gif")) {
	    setupGIF(fname);
	} else if (type.endsWith("/bmp")) {
	    setupBMP(fname);
	} else {
	    setupOther(fname);
	}
    }

    /**
     * Determine dimensions from a PNG file.
     * @param fname filename
     */
    private void setupPNG (String fname) {
	// from /etc/magic:
	//   # PNG [Portable Network Graphics, or "PNG's Not GIF"] images
	//   # (Greg Roelofs, newt@uchicago.edu)
	//   # (Albert Cahalan, acahalan@cs.uml.edu)
	//   #
	//   # 137 P N G \r \n ^Z \n [4-byte length] H E A D [HEAD data] [HEAD crc] ...
	//   #
	//   0      string  \x89PNG      PNG image data,
	//   >4     belong  !0x0d0a1a0a  CORRUPTED,
	//   >4     belong  0x0d0a1a0a
	//   >>16   belong  x            %ld x
	//   >>20   belong  x            %ld,
	//   >>24   byte    x            %d-bit
	//   >>25   byte    0            grayscale,
	//   >>25   byte    2            \b/color RGB,
	//   >>25   byte    3            colormap,
	//   >>25   byte    4            gray+alpha,
	//   >>25   byte    6            \b/color RGBA,
	//   #>>26  byte    0            deflate/32K,
	//   >>28   byte    0            non-interlaced
	//   >>28   byte    1            interlaced
	//   1      string  PNG          PNG image data, CORRUPTED
	FileInputStream in = null;
	try {
	    in = new FileInputStream(fname);
	    byte[] d = new byte[24];
	    if (in.read(d) == 24
		    && d[0] == (byte) 0x89 && d[1] == (byte) 0x50
		    && d[2] == (byte) 0x4e && d[3] == (byte) 0x47
		    && d[4] == (byte) 0x0d && d[5] == (byte) 0x0a
		    && d[6] == (byte) 0x1a && d[7] == (byte) 0x0a) {
		setInt(WIDTH, ((d[16] & 0xff) << 24)
			+ ((d[17] & 0xff) << 16)
			+ ((d[18] & 0xff) << 8)
			+ (d[19] & 0xff));
		setInt(HEIGHT, ((d[20] & 0xff) << 24)
			+ ((d[21] & 0xff) << 16)
			+ ((d[22] & 0xff) << 8)
			+ (d[23] & 0xff));
		setString(CODEC, "PNG");  // TODO can do better!
	    } else {
		setString(CODEC, "corrupted PNG file");
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

    /**
     * Determine dimensions from a GIF file.
     * @param fname filename
     */
    private void setupGIF (String fname) {
	// from /etc/magic:
	//   # GIF
	//   0       string          GIF8            GIF image data
	//   >4      string          7a              \b, version 8%s,
	//   >4      string          9a              \b, version 8%s,
	//   >6      leshort         >0              %hd x
	//   >8      leshort         >0              %hd,
	//   #>10    byte            &0x80           color mapped,
	//   #>10    byte&0x07       =0x00           2 colors
	//   #>10    byte&0x07       =0x01           4 colors
	//   #>10    byte&0x07       =0x02           8 colors
	//   #>10    byte&0x07       =0x03           16 colors
	//   #>10    byte&0x07       =0x04           32 colors
	//   #>10    byte&0x07       =0x05           64 colors
	//   #>10    byte&0x07       =0x06           128 colors
	//   #>10    byte&0x07       =0x07           256 colors
	FileInputStream in = null;
	try {
	    in = new FileInputStream(fname);
	    byte[] d = new byte[10];
	    if (in.read(d) == 10
		    && d[0] == (byte) 'G' && d[1] == (byte) 'I'
		    && d[2] == (byte) 'F' && d[3] == (byte) '8'
		    && (d[4] == (byte) '7' || d[4] == (byte) '9')
		    && d[5] == (byte) 'a') {
		setInt(WIDTH, (d[6] & 0xff) + ((d[7] & 0xff) << 8));
		setInt(HEIGHT, (d[8] & 0xff) + ((d[9] & 0xff) << 8));
		setString(CODEC, "GIF version 8"+((char)d[4])+"a");
	    } else {
		setString(CODEC, "corrupted GIF file");
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

    /**
     * Determine dimensions from a BMP file.
     * @param fname filename
     */
    private void setupBMP (String fname) {
	// from /etc/magic:
	//  # PC bitmaps (OS/2, Windoze BMP files) (Greg Roelofs, newt@uchicago.edu)
	//  0     string   BM  PC bitmap data
	//  >14   leshort  12  \b, OS/2 1.x format
	//  >>18  leshort  x   \b, %d x
	//  >>20  leshort  x   %d
	//  >14   leshort  64  \b, OS/2 2.x format
	//  >>18  leshort  x   \b, %d x
	//  >>20  leshort  x   %d
	//  >14   leshort  40  \b, Windows 3.x format
	//  >>18  lelong   x   \b, %d x
	//  >>22  lelong   x   %d x
	//  >>28  leshort  x   %d
	//  0     string   IC  PC icon data
	//  0     string   PI  PC pointer image data
	//  0     string   CI  PC color icon data
	//  0     string   CP  PC color pointer image data
	FileInputStream in = null;
	try {
	    in = new FileInputStream(fname);
	    byte[] d = new byte[28];
	    if (in.read(d) == 28 && d[0] == (byte) 'B' && d[1] == (byte) 'M') {
		switch (d[14]) {
		    case 12: case 64:
			setInt(WIDTH, ((d[19] & 0xff) << 8) + (d[18] & 0xff));
			setInt(HEIGHT, ((d[21] & 0xff) << 8) + (d[20] & 0xff));
			break;
		    case 40:
			setInt(WIDTH, ((d[21] & 0xff) << 24)
				+ ((d[20] & 0xff) << 16)
				+ ((d[19] & 0xff) << 8)
				+ (d[18] & 0xff));
			setInt(HEIGHT, ((d[25] & 0xff) << 24)
				+ ((d[24] & 0xff) << 16)
				+ ((d[23] & 0xff) << 8)
				+ (d[22] & 0xff));
			break;
		    default:
			setString(CODEC, "corrupted BMP file");
		}
	    } else {
		setString(CODEC, "corrupted BMP file");
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

    /**
     * Use <tt>java.awt.Toolkit</tt> to determine image dimensions.
     * @param fname filename
     */
    private void setupOther (String fname) {
	try {
	    // get default toolkit
	    if (! tkLoaded) {
		tkLoaded = true;
		tk = Toolkit.getDefaultToolkit();
	    }
	    // determine if toolkit loading failed
	    if (tk == null) {
		return;
	    }

	    // read image
	    Image img = tk.getImage(fname);
	    boolean err = false;
	    synchronized (tracker) {
		tracker.addImage(img, 0);
		tracker.waitForAll();
		err = tracker.isErrorAny();
		tracker.removeImage(img);
	    }
	    if (err) {
		return;
	    }

	    setInt(WIDTH, img.getWidth(null));
	    setInt(HEIGHT, img.getHeight(null));
	} catch (Throwable ex) {
	    // ignore
	}
    }
    /** keep toolkit for reuse */
    private static Toolkit tk = null;
    /** prevent loading toolkit multiple times when not possible */
    private static boolean tkLoaded = false;
    /** mediatracker to track image loading */
    private final static MediaTracker tracker = new MediaTracker(new Component() {});
}
