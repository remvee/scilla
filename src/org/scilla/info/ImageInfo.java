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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.scilla.info.exif.Exif;
import org.scilla.info.exif.ExifReader;
import org.scilla.util.MimeType;

/**
 * Image info.
 *
 * @version $Revision: 1.17 $
 * @author R.W. van 't Veer
 */
public class ImageInfo extends Info {
    public final static String WIDTH = "width";
    public final static String HEIGHT = "height";
    public final static String CODEC_PNG = "PNG";
    public final static String CODEC_GIF87A = "GIF version 87a";
    public final static String CODEC_GIF89A = "GIF version 89a";
    public final static String CODEC_BMP_OS2 = "BMP OS/2";
    public final static String CODEC_BMP_WIN3x = "BMP Windows 3.x";
    public final static String CODEC_JPEG = "JPEG";
    public final static String CODEC_UNKNOWN = "unknown";
    public final static String COLORMODE = "colormode";
    public final static String CM_GRAYSCALE = "grayscale";
    public final static String CM_RGB = "RGB";
    public final static String CM_INDEXED = "indexed";
    public final static String CM_UNKNOWN = "unknown";
    public final static String ALPHACHANNEL = "alpha channel";
    public final static String BITS = "bits";
    public final static String COMMENT = "comment";

    public ImageInfo (String fname) {
	super(fname);
	String type = MimeType.getTypeFromFilename(fname);

	if (type.endsWith("/png")) {
	    setupPNG(fname);
	} else if (type.endsWith("/gif")) {
	    setupGIF(fname);
	} else if (type.endsWith("/bmp")) {
	    setupBMP(fname);
	} else if (type.endsWith("/jpeg")) {
	    setupJPEG(fname);
	} else {
	    setupOther(fname);
	}
    }

    /**
     * @return <tt>true</tt> when image of Portable Network
     * Graphics (PNG) format
     */
    public boolean isPNG () {
	return CODEC_PNG.equals(getString(CODEC));
    }

    /**
     * @return <tt>true</tt> when image of Graphics Interchange
     * Format (GIF)
     */
    public boolean isGIF () {
	return CODEC_GIF87A.equals(getString(CODEC))
	    || CODEC_GIF89A.equals(getString(CODEC));
    }

    /**
     * @return <tt>true</tt> when image of BMP format.
     */
    public boolean isBMP () {
	return CODEC_BMP_OS2.equals(getString(CODEC))
	    || CODEC_BMP_WIN3x.equals(getString(CODEC));
    }

    /**
     * @return <tt>true</tt> when image of Joint Photographic
     * Experts Group (JPEG) format.
     */
    public boolean isJPEG () {
	return CODEC_JPEG.equals(getString(CODEC));
    }

    /**
     * @return image width in pixels or <tt>-1</tt> when unknown
     */
    public int getWidth () {
	return getInt(WIDTH);
    }

    /**
     * @return image height pixels or <tt>-1</tt> when unknown
     */
    public int getHeight () {
	return getInt(HEIGHT);
    }

    /**
     * @return number of bits per pixel or <tt>-1</tt> when unknown
     */
    public int getBits () {
	return getInt(BITS);
    }

    /**
     * @return comment embedded in this image or <tt>null</tt>
     */
    public String getComment () {
	return getString(COMMENT);
    }

    /**
     * @return date time embedded in this image or <tt>null</tt>
     */
    public Date getDate () {
	return getDate(Exif.DATE_TIME);
    }

    /**
     * @return <tt>true</tt> when image has RGB color mode or
     * <tt>false</tt> when other mode or mode unknown
     */
    public boolean isRGB () {
	return CM_RGB.equals(getString(COLORMODE));
    }

    /**
     * @return <tt>true</tt> when image has grayscale color mode or
     * <tt>false</tt> when other mode or mode unknown
     */
    public boolean isGrayScale () {
	return CM_GRAYSCALE.equals(getString(COLORMODE));
    }

    /**
     * @return <tt>true</tt> when image has indexed color mode or
     * <tt>false</tt> when other mode or mode unknown
     */
    public boolean isIndexed () {
	return CM_INDEXED.equals(getString(COLORMODE));
    }

    /**
     * @return <tt>true</tt> when image has alphachannel/
     * transparency, <tt>false</tt> when no alpachannel or unknown
     */
    public boolean hasAlphaChannel () {
	return getBoolean(ALPHACHANNEL);
    }

    /**
     * Determine meta data from a PNG file.
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
	    byte[] d = new byte[28];
	    if (in.read(d) == 28
		    && d[0] == (byte) 0x89 && d[1] == (byte) 0x50
		    && d[2] == (byte) 0x4e && d[3] == (byte) 0x47
		    && d[4] == (byte) 0x0d && d[5] == (byte) 0x0a
		    && d[6] == (byte) 0x1a && d[7] == (byte) 0x0a) {
		setString(CODEC, CODEC_PNG);
		setInt(WIDTH, ((d[16] & 0xff) << 24)
			+ ((d[17] & 0xff) << 16)
			+ ((d[18] & 0xff) << 8)
			+ (d[19] & 0xff));
		setInt(HEIGHT, ((d[20] & 0xff) << 24)
			+ ((d[21] & 0xff) << 16)
			+ ((d[22] & 0xff) << 8)
			+ (d[23] & 0xff));

		// color mode
		switch (d[25]) {
		    case 0:
			setString(COLORMODE, CM_GRAYSCALE);
			break;
		    case 2:
			setString(COLORMODE, CM_RGB);
			break;
		    case 3:
			setString(COLORMODE, CM_INDEXED);
			break;
		    case 4:
			setString(COLORMODE, CM_GRAYSCALE);
			setBoolean(ALPHACHANNEL, true);
			break;
		    case 6:
			setString(COLORMODE, CM_RGB);
			setBoolean(ALPHACHANNEL, true);
			break;
		    default:
			setString(COLORMODE, CM_UNKNOWN);
			break;
		}
		setInt(BITS, (d[24] & 0xff));
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
     * Determine meta data from a GIF file.
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
		setString(CODEC, ((char)d[4]) == '7' ? CODEC_GIF87A : CODEC_GIF89A);
		setString(COLORMODE, CM_INDEXED);
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
     * Determine meta data from a BMP file.
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
	    byte[] d = new byte[30];
	    if (in.read(d) == 30 && d[0] == (byte) 'B' && d[1] == (byte) 'M') {
		switch (d[14]) {
		    case 12: case 64:
			setInt(WIDTH, ((d[19] & 0xff) << 8) + (d[18] & 0xff));
			setInt(HEIGHT, ((d[21] & 0xff) << 8) + (d[20] & 0xff));
			setString(CODEC, CODEC_BMP_OS2);
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
			setInt(BITS, ((d[29] & 0xff) << 8) + (d[28] & 0xff));
			setString(CODEC, CODEC_BMP_WIN3x);
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
     * Determine meta data from a JPEG file.  Algorithm from <tt>rdjpgcom.c</tt>.
     * @param fname filename
     */
    private void setupJPEG (String fname) {
	FileInputStream in = null;
	try {
	    in = new FileInputStream(fname);

	    // JPEG marker
	    if (jpegFirstMarker(in) != JPEG_SOI) {
		setString(CODEC, "corrupted JPEG file");
		return;
	    }

	    for (;;) {
		int marker = jpegNextMarker(in);
		switch (marker) {
		    case JPEG_SOF0:	// Baseline
		    case JPEG_SOF1:	// Extended sequential, Huffman
		    case JPEG_SOF2:	// Progressive, Huffman
		    case JPEG_SOF3:	// Lossless, Huffman
		    case JPEG_SOF5:	// Differential sequential, Huffman
		    case JPEG_SOF6:	// Differential progressive, Huffman
		    case JPEG_SOF7:	// Differential lossless, Huffman
		    case JPEG_SOF9:	// Extended sequential, arithmetic
		    case JPEG_SOF10:	// Progressive, arithmetic
		    case JPEG_SOF11:	// Lossless, arithmetic
		    case JPEG_SOF13:	// Differential sequential, arithmetic
		    case JPEG_SOF14:	// Differential progressive, arithmetic
		    case JPEG_SOF15:	// Differential lossless, arithmetic
			{
			    int length = jpegRead2Bytes(in);
			    int dataPrecision = in.read();
			    int imageHeight = jpegRead2Bytes(in);
			    int imageWidth = jpegRead2Bytes(in);
			    int numComponents = in.read();
			    if (length != (8 + numComponents * 3)) {
				setString(CODEC, "corrupted JPEG file");
				return;
			    }
			    setString(CODEC, CODEC_JPEG);
			    setInt(WIDTH, imageWidth);
			    setInt(HEIGHT, imageHeight);
			    setInt(BITS, dataPrecision);
			}
                        break;
		    case JPEG_EOI:	// in case it's a tables-only JPEG stream
		    case JPEG_SOS:	// stop before hitting compressed data
			return;
		    case JPEG_COM:	// comment
                        setString(COMMENT, new String(jpegReadFrame(in)));
                        break;
		    case JPEG_APP1:	// exif?
			{
			    byte[] d = jpegReadFrame(in);
			    if (d[0] == 'E' && d[1] == 'x' && d[2] == 'i' && d[3] == 'f' &&
				    d[4] == 0 && d[5] == 0) {
				byte[] e = new byte[d.length - 6];
				System.arraycopy(d, 6, e, 0, e.length);
				try {
				    ExifReader.readIntoMap(e, this);
				} catch (TiffException ex) {
				    // TODO log problem
				}
			    }
			}
			break;
		    default:
			jpegReadFrame(in);
                        break;
		}
	    }
	} catch (Throwable ex) {
	    // TODO: properly escalate problem
            ex.printStackTrace();
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
    private final static int JPEG_SOF0 = 0xC0;		// Start Of Frame N
    private final static int JPEG_SOF1 = 0xC1;		// N indicates which compression process
    private final static int JPEG_SOF2 = 0xC2;		// Only SOF0-SOF2 are now in common use
    private final static int JPEG_SOF3 = 0xC3;
    private final static int JPEG_SOF5 = 0xC5;		// NB: codes C4 and CC are NOT SOF markers
    private final static int JPEG_SOF6 = 0xC6;
    private final static int JPEG_SOF7 = 0xC7;
    private final static int JPEG_SOF9 = 0xC9;
    private final static int JPEG_SOF10 = 0xCA;
    private final static int JPEG_SOF11 = 0xCB;
    private final static int JPEG_SOF13 = 0xCD;
    private final static int JPEG_SOF14 = 0xCE;
    private final static int JPEG_SOF15 = 0xCF;
    private final static int JPEG_SOI = 0xD8;		// Start Of Image (beginning of datastream)
    private final static int JPEG_EOI = 0xD9;		// End Of Image (end of datastream)
    private final static int JPEG_SOS = 0xDA;		// Start Of Scan (begins compressed data)
    private final static int JPEG_APP1 = 0xE1;		// EXIF marker
    private final static int JPEG_COM = 0xFE;		// COMment
    private int jpegFirstMarker (InputStream in)
    throws IOException {
	int c1 = in.read();
	int c2 = in.read();
	return c1 == 0xff ? c2 : -1;
    }
    private int jpegNextMarker (InputStream in)
    throws IOException {
	int c = in.read();
	while (c != 0xff && c != -1) {
	    c = in.read();
	}
	do {
	    c = in.read();
	} while (c == 0xff);
	return c;
    }
    private byte[] jpegReadFrame (InputStream in)
    throws IOException {
	int l = jpegRead2Bytes(in);
	if (l < 2) {
	    throw new IOException("Erroneous JPEG marker length");
	}
        l -= 2;

	byte[] r = new byte[l];
	for (int n = 0, m = 0; (m = in.read(r, n, l - n)) != -1 && n < l; n += m)
	    ;

        return r;
    }
    private int jpegRead2Bytes (InputStream in)
    throws IOException {
	int c1 = in.read();
	int c2 = in.read();
	return (c1 << 8) + c2;
    }

    /**
     * Use <tt>java.awt.Toolkit</tt> to get meta data.
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

    /** debug */
    public static void main (String[] args) {
	for (int i = 0; i < args.length; i++) {
	    String fn = args[i];
	    ImageInfo ii = new ImageInfo(fn);
	    System.out.println(fn+": "+ii);
	}
    }
}
