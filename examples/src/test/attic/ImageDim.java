package test;

import java.io.*;
import java.awt.*;
import java.util.*;

public class ImageDim {
    public int width = -1;
    public int height = -1;

    public static ImageDim measure (String fn)
    throws FileNotFoundException, IOException, InterruptedException {
	// try to pull from cache
	CacheEntry ce = (CacheEntry) dimCache.get(fn);
	long timestamp = (new File(fn)).lastModified();

	// see if cache entry valid
	if (ce != null && timestamp == ce.timestamp) {
	    return ce.dim;
	}

	// create a new cache entry
	ce = new CacheEntry();
	ce.timestamp = timestamp;
	if (fn.toLowerCase().endsWith(".png")) {
	    ce.dim = measurePNG(fn);
	} else if (fn.toLowerCase().endsWith(".gif")) {
	    ce.dim = measureGIF(fn);
	} else if (fn.toLowerCase().endsWith(".bmp")) {
	    ce.dim = measureBMP(fn);
	} else {
	    ce.dim = measureOther(fn);
	}

	// cache it!
	dimCache.put(fn, ce);

	return ce.dim;
    }
    private static Map dimCache = new HashMap();
    private static class CacheEntry {
	ImageDim dim = null;
	long timestamp = -1L;
    }

    public static ImageDim measurePNG (String fn)
    throws FileNotFoundException, IOException {
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
	ImageDim dim = null;
	FileInputStream in = null;
	try {
	    in = new FileInputStream(fn);
	    byte[] d = new byte[24];
	    if (in.read(d) == 24
		    && d[0] == (byte) 0x89 && d[1] == (byte) 0x50
		    && d[2] == (byte) 0x4e && d[3] == (byte) 0x47
		    && d[4] == (byte) 0x0d && d[5] == (byte) 0x0a
		    && d[6] == (byte) 0x1a && d[7] == (byte) 0x0a) {
		dim = new ImageDim();
		dim.width = ((d[16] & 0xff) << 24)
			+ ((d[17] & 0xff) << 16)
			+ ((d[18] & 0xff) << 8)
			+ (d[19] & 0xff);
		dim.height = ((d[20] & 0xff) << 24)
			+ ((d[21] & 0xff) << 16)
			+ ((d[22] & 0xff) << 8)
			+ (d[23] & 0xff);
	    } else {
		throw new IOException("corrupted PNG file");
	    }
	} finally {
	    if (in != null) {
		in.close();
	    }
	}
	return dim;
    }

    public static ImageDim measureGIF (String fn)
    throws FileNotFoundException, IOException {
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
	ImageDim dim = null;
	FileInputStream in = null;
	try {
	    in = new FileInputStream(fn);
	    byte[] d = new byte[10];
	    if (in.read(d) == 10
		    && d[0] == (byte) 'G' && d[1] == (byte) 'I'
		    && d[2] == (byte) 'F' && d[3] == (byte) '8'
		    && (d[4] == (byte) '7' || d[4] == (byte) '9')
		    && d[5] == (byte) 'a') {
		dim = new ImageDim();
		dim.width = (d[6] & 0xff) + ((d[7] & 0xff) << 8);
		dim.height = (d[8] & 0xff) + ((d[9] & 0xff) << 8);
	    } else {
		throw new IOException("corrupted GIF file");
	    }
	} finally {
	    if (in != null) {
		in.close();
	    }
	}
	return dim;
    }

    public static ImageDim measureBMP (String fn)
    throws FileNotFoundException, IOException {
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
	ImageDim dim = null;
	FileInputStream in = null;
	try {
	    in = new FileInputStream(fn);
	    byte[] d = new byte[28];
	    if (in.read(d) == 28 && d[0] == (byte) 'B' && d[1] == (byte) 'M') {
		switch (d[14]) {
		    case 12: case 64:
			dim = new ImageDim();
			dim.width = ((d[19] & 0xff) << 8)
				+ (d[18] & 0xff);
			dim.height = ((d[21] & 0xff) << 8)
				+ (d[20] & 0xff);
			break;
		    case 40:
			dim = new ImageDim();
			dim.width = ((d[21] & 0xff) << 24)
				+ ((d[20] & 0xff) << 16)
				+ ((d[19] & 0xff) << 8)
				+ (d[18] & 0xff);
			dim.height = ((d[25] & 0xff) << 24)
				+ ((d[24] & 0xff) << 16)
				+ ((d[23] & 0xff) << 8)
				+ (d[22] & 0xff);
			break;
		}
	    } else {
		throw new IOException("corrupted BMP file");
	    }
	} finally {
	    if (in != null) {
		in.close();
	    }
	}
	return dim;
    }

    public static ImageDim measureOther (String fn)
    throws InterruptedException, IOException {
	Image img = Toolkit.getDefaultToolkit().getImage(fn);
	boolean err;
	synchronized (tracker) {
	    tracker.addImage(img, 0);
	    tracker.waitForAll();
	    err = tracker.isErrorAny();
	    tracker.removeImage(img);
	}
	if (err) {
	    throw new IOException("unable to load image");
	}
	ImageDim dim = new ImageDim();
	dim.width = img.getWidth(null);
	dim.height = img.getHeight(null);
	return dim;
    }
    private final static Component component = new Component() {};
    private final static MediaTracker tracker = new MediaTracker(component);
}
