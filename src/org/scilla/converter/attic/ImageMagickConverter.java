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
 * <DL>
 * <DT>affine MATRIX       <DD>drawing transform matrix
 * <DT>antialias           <DD>remove pixel-aliasing
 * <DT>blur GEOMETRY       <DD>blur the image
 * <DT>border GEOMETRY     <DD>surround image with a border of color
 * <DT>bordercolor COLOR   <DD>border color
 * <DT>box COLOR           <DD>color for annotation bounding box
 * <DT>channel TYPE        <DD>Red, Green, Blue, Matte
 * <DT>charcoal RADIUS     <DD>simulate a charcoal drawing
 * <DT>colorize VALUE      <DD>colorize the image with the fill color
 * <DT>colors VALUE        <DD>preferred number of colors in the image
 * <DT>colorspace TYPE     <DD>alternate image colorspace
 * <DT>comment STRING      <DD>annotate image with comment
 * <DT>compress TYPE       <DD>type of image compression
 * <DT>contrast N          <DD>enhance or reduce the image contrast N times
 * <DT>crop GEOMETRY       <DD>preferred size and location of the cropped image
 * <DT>cycle AMOUNT        <DD>cycle the image colormap
 * <DT>density GEOMETRY    <DD>vertical and horizontal density of the image
 * <DT>depth VALUE         <DD>depth of the image
 * <DT>despeckle           <DD>reduce the speckles within an image
 * <DT>dispose METHOD      <DD>GIF disposal method
 * <DT>dither              <DD>apply Floyd/Steinberg error diffusion to image
 * <DT>draw STRING         <DD>annotate the image with a graphic primitive
 * <DT>edge RADIUS         <DD>apply a filter to detect edges in the image
 * <DT>emboss RADIUS       <DD>emboss an image
 * <DT>enhance             <DD>apply a digital filter to enhance a noisy image
 * <DT>equalize            <DD>perform histogram equalization to an image
 * <DT>fill COLOR          <DD>color to use when filling a graphic primitive
 * <DT>filter TYPE         <DD>use this filter when resizing an image
 * <DT>flip                <DD>flip image in the vertical direction
 * <DT>flop                <DD>flop image in the horizontal direction
 * <DT>font NAME           <DD>font for rendering text
 * <DT>frame GEOMETRY      <DD>surround image with an ornamental border
 * <DT>fuzz DISTANCE       <DD>colors within this distance are considered equal
 * <DT>gamma VALUE         <DD>level of gamma correction
 * <DT>geometry GEOMETRY   <DD>perferred size or location of the image
 * <DT>gaussian GEOMETRY   <DD>gaussian blur an image
 * <DT>gravity TYPE        <DD>vertical and horizontal text placement
 * <DT>implode AMOUNT      <DD>implode image pixels about the center
 * <DT>intent TYPE         <DD>Absolute, Perceptual, Relative, or Saturation
 * <DT>interlace TYPE      <DD>None, Line, Plane, or Partition
 * <DT>label NAME          <DD>assign a label to an image
 * <DT>matte               <DD>store matte channel if the image has one
 * <DT>median RADIUS       <DD>apply a median filter to the image
 * <DT>modulate VALUE      <DD>vary the brightness, saturation, and hue
 * <DT>monochrome          <DD>transform image to black and white
 * <DT>negate              <DD>replace every pixel with its complementary color 
 * <DT>noise               <DD>add or reduce noise in an image
 * <DT>normalize           <DD>transform image to span the full range of colors
 * <DT>opaque COLOR        <DD>change this color to the fill color
 * <DT>page GEOMETRY       <DD>size and location of an image canvas
 * <DT>paint RADIUS        <DD>simulate an oil painting
 * <DT>pointsize VALUE     <DD>pointsize of Postscript font
 * <DT>quality VALUE       <DD>JPEG/MIFF/PNG compression level
 * <DT>raise VALUE         <DD>lighten/darken image edges to create a 3-D effect
 * <DT>region GEOMETRY     <DD>apply options to a portion of the image
 * <DT>roll GEOMETRY       <DD>roll an image vertically or horizontally
 * <DT>rotate DEGREES      <DD>apply Paeth rotation to the image
 * <DT>sample GEOMETRY     <DD>scale image with pixel sampling
 * <DT>scale GEOMETRY      <DD>resize image
 * <DT>segment VALUES      <DD>segment an image
 * <DT>shade DEGREES       <DD>shade the image using a distant light source
 * <DT>sharpen GEOMETRY    <DD>sharpen the image
 * <DT>shear GEOMETRY      <DD>slide one edge of the image along the X or Y axis
 * <DT>size GEOMETRY       <DD>width and height of image
 * <DT>solarize THRESHOLD  <DD>negate all pixels above the threshold level
 * <DT>spread AMOUNT       <DD>displace image pixels by a random amount
 * <DT>stroke COLOR        <DD>color to use when stoking a graphic primitive
 * <DT>strokewidth VALUE   <DD>width of stroke
 * <DT>swirl DEGREES       <DD>swirl image pixels about the center
 * <DT>threshold VALUE     <DD>threshold the image
 * <DT>transparent COLOR   <DD>make this color transparent within the image
 * <DT>treedepth VALUE     <DD>depth of the color color tree
 * <DT>type TYPE           <DD>Bilevel, Gray, Palette, PaletteMatte, TrueColor, 
 *                         TrueColorMatte, or ColorSeparation
 * <DT>units TYPE          <DD>PixelsPerInch, PixelsPerCentimeter, or Undefined
 * <DT>wave GEOMETRY       <DD>alter an image along a sine wave
 * </DL>
 *
 * @author R.W. van 't Veer
 * @version $Revision: 1.4 $
 */
public class ImageMagickConverter extends Converter
{
    /** parameter name to force the use of this converter */
    public final static String THIS_CONVERTER_PARAMETER = "imagick";
    public final static String CONVERT_EXEC_PROPERTY = "ImageMagickConverter.exec";

    /**
     * Create a ImageMagick converter object.
     */
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
		THIS_CONVERTER_PARAMETER, Request.OUTPUT_TYPE_PARAMETER,
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

    /**
     * Start conversion.
     */
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

    /**
     * Determine if ImageMagick <CODE>convert</CODE> executable
     * exists.
     * @see #CONVERT_EXEC_PROPERTY
     * @see org.scilla.Config
     */
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

    /**
     * Construct command line from request parameters
     * @return request parameters
     */
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
		System.err.println("ImageMagickConverter.convert: parameter '"+rp.key+"' NOT IMPLEMENTED");
	    }
	}
	
	// and the output file
	v.add(MimeTypeFactory.getExtensionForType(getOutputType())
		+":"+outputFile);

	return (String[]) v.toArray(new String[0]);
    }
}
