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
import org.scilla.util.*;

/**
 * The ImageMagick Converter.
 * <P>
 * This converter does not support file sequences.  All options
 * needing a filename argument are not available.  This implementation
 * was made using ImageMagick-5.3.1
 * <P>
 * Following are the supported options from the convert --help
 * message:
 * <PRE>
 * affine MATRIX       drawing transform matrix
 * antialias           remove pixel-aliasing
 * blur GEOMETRY       blur the image
 * border GEOMETRY     surround image with a border of color
 * bordercolor COLOR   border color
 * box COLOR           color for annotation bounding box
 * channel TYPE        Red, Green, Blue, Matte
 * charcoal RADIUS     simulate a charcoal drawing
 * colorize VALUE      colorize the image with the fill color
 * colors VALUE        preferred number of colors in the image
 * colorspace TYPE     alternate image colorspace
 * comment STRING      annotate image with comment
 * compress TYPE       type of image compression
 * contrast N          enhance or reduce the image contrast N times
 * crop GEOMETRY       preferred size and location of the cropped image
 * cycle AMOUNT        cycle the image colormap
 * density GEOMETRY    vertical and horizontal density of the image
 * depth VALUE         depth of the image
 * despeckle           reduce the speckles within an image
 * dispose METHOD      GIF disposal method
 * dither              apply Floyd/Steinberg error diffusion to image
 * draw STRING         annotate the image with a graphic primitive
 * edge RADIUS         apply a filter to detect edges in the image
 * emboss RADIUS       emboss an image
 * enhance             apply a digital filter to enhance a noisy image
 * equalize            perform histogram equalization to an image
 * fill COLOR          color to use when filling a graphic primitive
 * filter TYPE         use this filter when resizing an image
 * flip                flip image in the vertical direction
 * flop                flop image in the horizontal direction
 * font NAME           font for rendering text
 * frame GEOMETRY      surround image with an ornamental border
 * fuzz DISTANCE       colors within this distance are considered equal
 * gamma VALUE         level of gamma correction
 * geometry GEOMETRY   perferred size or location of the image
 * gaussian GEOMETRY   gaussian blur an image
 * gravity TYPE        vertical and horizontal text placement
 * implode AMOUNT      implode image pixels about the center
 * intent TYPE         Absolute, Perceptual, Relative, or Saturation
 * interlace TYPE      None, Line, Plane, or Partition
 * label NAME          assign a label to an image
 * matte               store matte channel if the image has one
 * median RADIUS       apply a median filter to the image
 * modulate VALUE      vary the brightness, saturation, and hue
 * monochrome          transform image to black and white
 * negate              replace every pixel with its complementary color 
 * noise               add or reduce noise in an image
 * normalize           transform image to span the full range of colors
 * opaque COLOR        change this color to the fill color
 * page GEOMETRY       size and location of an image canvas
 * paint RADIUS        simulate an oil painting
 * pointsize VALUE     pointsize of Postscript font
 * quality VALUE       JPEG/MIFF/PNG compression level
 * raise VALUE         lighten/darken image edges to create a 3-D effect
 * region GEOMETRY     apply options to a portion of the image
 * roll GEOMETRY       roll an image vertically or horizontally
 * rotate DEGREES      apply Paeth rotation to the image
 * sample GEOMETRY     scale image with pixel sampling
 * scale GEOMETRY      resize image
 * segment VALUES      segment an image
 * shade DEGREES       shade the image using a distant light source
 * sharpen GEOMETRY    sharpen the image
 * shear GEOMETRY      slide one edge of the image along the X or Y axis
 * size GEOMETRY       width and height of image
 * solarize THRESHOLD  negate all pixels above the threshold level
 * spread AMOUNT       displace image pixels by a random amount
 * stroke COLOR        color to use when stoking a graphic primitive
 * strokewidth VALUE   width of stroke
 * swirl DEGREES       swirl image pixels about the center
 * threshold VALUE     threshold the image
 * transparent COLOR   make this color transparent within the image
 * treedepth VALUE     depth of the color color tree
 * type TYPE           Bilevel, Gray, Palette, PaletteMatte, TrueColor, 
 *                     TrueColorMatte, or ColorSeparation
 * units TYPE          PixelsPerInch, PixelsPerCentimeter, or Undefined
 * wave GEOMETRY       alter an image along a sine wave
 * </PRE>
 */
public class ImageMagickConverter extends Converter
{
    public final static String THIS_CONVERTER_PARAMETER = "imagick";
    public final static String CONVERT_EXEC_PROPERTY = "ImageMagickConverter.exec";

    // provide inputTypeList, outputTypeList and propertyList
    public ImageMagickConverter ()
    {
	super();
	inputTypeList = new String[] {
		"image/gif", "image/jpeg", "image/png", "image/tiff",
		"image/x-cmu-raster", "image/x-ms-bmp",
		"image/x-portable-anymap", "image/x-portable-bitmap",
		"image/x-portable-graymap", "image/x-portable-pixmap",
		"image/x-rgb", "image/x-xbitmap", "image/x-xpixmap",
		"image/x-xwindowdump"
	};
	outputTypeList = inputTypeList;

	parameterList = new String[] {
		THIS_CONVERTER_PARAMETER, Request.OUTPUT_TYPE_PROPERTY,
		"affine", "antialias", "blur", "border",
		"bordercolor", "box", "channel", "charcoal",
		"colorize", "colors", "colorspace", "comment",
		"compress", "contrast", "crop", "cycle", "density",
		"depth", "despeckle", "dispose", "dither", "draw",
		"edge", "emboss", "enhance", "equalize", "fill",
		"filter", "flip", "flop", "font", "frame", "fuzz",
		"gamma", "geometry", "gaussian", "gravity", "implode",
		"intent", "interlace", "label", "label", "matte",
		"median", "modulate", "monochrome", "negate", "noise",
		"normalize", "opaque", "page", "paint", "pointsize",
		"quality", "raise", "region", "roll", "rotate",
		"sample", "scale", "segment", "shade", "sharpen",
		"shear", "size", "solarize", "spread", "stroke",
		"strokewidth", "swirl", "threshold", "transparent",
		"treedepth", "type", "units", "wave"
	};
    }

    public void convert ()
    {
	// create command line
	String[] cmdLine = createCmdLine();

	// run system command "convert"
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

    public boolean isFunctional ()
    {
	File f = new File(Config.getParameter(CONVERT_EXEC_PROPERTY));
	return f.exists();
    }

    static Vector noArgsList = new Vector();
    static Vector oneArgList = new Vector();
    static
    {
	noArgsList.add("antialias"); noArgsList.add("despeckle");
	noArgsList.add("dither"); noArgsList.add("enhance");
	noArgsList.add("equalize"); noArgsList.add("flip");
	noArgsList.add("flop"); noArgsList.add("matte");
	noArgsList.add("monochrome"); noArgsList.add("negate");
	noArgsList.add("normalize");

	oneArgList.add("noise"); oneArgList.add("affine");
	oneArgList.add("blur"); oneArgList.add("border");
	oneArgList.add("bordercolor"); oneArgList.add("box");
	oneArgList.add("channel"); oneArgList.add("charcoal");
	oneArgList.add("colorize"); oneArgList.add("colors");
	oneArgList.add("colorspace"); oneArgList.add("comment");
	oneArgList.add("compress"); oneArgList.add("crop");
	oneArgList.add("cycle"); oneArgList.add("density");
	oneArgList.add("depth"); oneArgList.add("dispose");
	oneArgList.add("draw"); oneArgList.add("edge");
	oneArgList.add("emboss"); oneArgList.add("fill");
	oneArgList.add("filter"); oneArgList.add("font");
	oneArgList.add("frame"); oneArgList.add("fuzz");
	oneArgList.add("gamma"); oneArgList.add("geometry");
	oneArgList.add("gaussian"); oneArgList.add("gravity");
	oneArgList.add("implode"); oneArgList.add("intent");
	oneArgList.add("interlace"); oneArgList.add("label");
	oneArgList.add("median"); oneArgList.add("modulate");
	oneArgList.add("opaque"); oneArgList.add("page");
	oneArgList.add("paint"); oneArgList.add("pointsize");
	oneArgList.add("quality"); oneArgList.add("raise");
	oneArgList.add("region"); oneArgList.add("roll");
	oneArgList.add("rotate"); oneArgList.add("sample");
	oneArgList.add("scale"); oneArgList.add("segment");
	oneArgList.add("shade"); oneArgList.add("sharpen");
	oneArgList.add("shear"); oneArgList.add("size");
	oneArgList.add("solarize"); oneArgList.add("spread");
	oneArgList.add("stroke"); oneArgList.add("strokewidth");
	oneArgList.add("swirl"); oneArgList.add("threshold");
	oneArgList.add("transparent"); oneArgList.add("treedepth");
	oneArgList.add("type"); oneArgList.add("units");
	oneArgList.add("wave");
    }

    String[] createCmdLine ()
    {
	// create command line
	Vector v = new Vector();
	v.add(Config.getParameter(CONVERT_EXEC_PROPERTY));
	v.add(inputFile);

	// added conversion parameters to command line
	for (Iterator it = pars.iterator(); it.hasNext(); )
	{
	    RequestParameter rp = (RequestParameter) it.next();
	    if (noArgsList.contains(rp.key))
	    {
		v.add("-"+rp.key);
	    }
	    else if (oneArgList.contains(rp.key))
	    {
		v.add("-"+rp.key);
		v.add(rp.val);
	    }
	    else if (rp.key.equals("contrast"))
	    {
		int n = Integer.parseInt(rp.val);
		char c = n < 0 ? '+' : '-';
		n = n < 0 ? -n : n;
		for (int i = 0; i < n; i++)
		{
		    v.add(c+"contrast");
		}
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
		System.err.println("ImageMagickConverter.convert: parameter '"+rp.key+"' NOT IMPLEMENTED");
	    }
	}
	
	// and the output file
	v.add(MimeTypeFactory.getExtensionForType(getOutputType())
		+":"+outputFile);

	return (String[]) v.toArray(new String[0]);
    }
}
