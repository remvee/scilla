/*
 * scilla
 *
 * Copyright (C) 2003  R.W. van 't Veer
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * EXIF.
 *
 * @version $Revision: 1.10 $
 * @author R.W. van 't Veer
 */
public class Exif extends HashMap {
    private TiffHeader tiff;
    
    private static final int EXIF_T_EXIFIFD = 0x8769;
    private static final int EXIF_T_GPSIFD = 0x8825;
    private static final int EXIF_T_INTEROP = 0xa005;
    
    private static final int EXIF_T_MAKE = 0x010f;
    private static final int EXIF_T_MAKERNOTE = 0x927c;

    // exif 2.2 tags
    private static Map labels = new HashMap();
    static {
	labels.put(new Integer(0x0100), "EXIFImageWidth");
	labels.put(new Integer(0x0101), "EXIFImageLength");
	labels.put(new Integer(0x0102), "EXIFBitsPerSample");
	labels.put(new Integer(0x0103), "EXIFCompression");
	labels.put(new Integer(0x0106), "EXIFPhotometricInterpretation");
	labels.put(new Integer(0x010a), "EXIFFillOrder");
	labels.put(new Integer(0x010d), "EXIFDocumentName");
	labels.put(new Integer(0x010e), "EXIFImageDescription");
	labels.put(new Integer(0x010f), "EXIFMake");
	labels.put(new Integer(0x0110), "EXIFModel");
	labels.put(new Integer(0x0111), "EXIFStripOffsets");
	labels.put(new Integer(0x0112), "EXIFOrientation");
	labels.put(new Integer(0x0115), "EXIFSamplesPerPixel");
	labels.put(new Integer(0x0116), "EXIFRowsPerStrip");
	labels.put(new Integer(0x0117), "EXIFStripByteCounts");
	labels.put(new Integer(0x011a), "EXIFXResolution");
	labels.put(new Integer(0x011b), "EXIFYResolution");
	labels.put(new Integer(0x011c), "EXIFPlanarConfiguration");
	labels.put(new Integer(0x0128), "EXIFResolutionUnit");
	labels.put(new Integer(0x012d), "EXIFTransferFunction");
	labels.put(new Integer(0x0131), "EXIFSoftware");
	labels.put(new Integer(0x0132), "EXIFDateTime");
	labels.put(new Integer(0x013b), "EXIFArtist");
	labels.put(new Integer(0x013e), "EXIFWhitePoint");
	labels.put(new Integer(0x013f), "EXIFPrimaryChromaticities");
	labels.put(new Integer(0x0156), "EXIFTransferRange");
	labels.put(new Integer(0x0200), "EXIFJPEGProc");
	labels.put(new Integer(0x0201), "EXIFJPEGInterchangeFormat");
	labels.put(new Integer(0x0202), "EXIFJPEGInterchangeFormatLength");
	labels.put(new Integer(0x0211), "EXIFYCbCrCoefficients");
	labels.put(new Integer(0x0212), "EXIFYCbCrSubSampling");
	labels.put(new Integer(0x0213), "EXIFYCbCrPositioning");
	labels.put(new Integer(0x0214), "EXIFReferenceBlackWhite");
	labels.put(new Integer(0x828d), "EXIFCFARepeatPatternDim");
	labels.put(new Integer(0x828e), "EXIFCFAPattern");
	labels.put(new Integer(0x828f), "EXIFBatteryLevel");
	labels.put(new Integer(0x8298), "EXIFCopyright");
	labels.put(new Integer(0x829a), "EXIFExposureTime");
	labels.put(new Integer(0x829d), "EXIFFNumber");
	labels.put(new Integer(0x83bb), "EXIFIPTC/NAA");
	labels.put(new Integer(0x8769), "EXIFExifOffset");
	labels.put(new Integer(0x8773), "EXIFInterColorProfile");
	labels.put(new Integer(0x8822), "EXIFExposureProgram");
	labels.put(new Integer(0x8824), "EXIFSpectralSensitivity");
	labels.put(new Integer(0x8825), "EXIFGPSInfo");
	labels.put(new Integer(0x8827), "EXIFISOSpeedRatings");
	labels.put(new Integer(0x8828), "EXIFOECF");
	labels.put(new Integer(0x9000), "EXIFExifVersion");
	labels.put(new Integer(0x9003), "EXIFDateTimeOriginal");
	labels.put(new Integer(0x9004), "EXIFDateTimeDigitized");
	labels.put(new Integer(0x9101), "EXIFComponentsConfiguration");
	labels.put(new Integer(0x9102), "EXIFCompressedBitsPerPixel");
	labels.put(new Integer(0x9201), "EXIFShutterSpeedValue");
	labels.put(new Integer(0x9202), "EXIFApertureValue");
	labels.put(new Integer(0x9203), "EXIFBrightnessValue");
	labels.put(new Integer(0x9204), "EXIFExposureBiasValue");
	labels.put(new Integer(0x9205), "EXIFMaxApertureValue");
	labels.put(new Integer(0x9206), "EXIFSubjectDistance");
	labels.put(new Integer(0x9207), "EXIFMeteringMode");
	labels.put(new Integer(0x9208), "EXIFLightSource");
	labels.put(new Integer(0x9209), "EXIFFlash");
	labels.put(new Integer(0x920a), "EXIFFocalLength");
	labels.put(new Integer(0x9214), "EXIFSubjectArea");
	labels.put(new Integer(0x927c), "EXIFMakerNote");
	labels.put(new Integer(0x9286), "EXIFUserComment");
	labels.put(new Integer(0x9290), "EXIFSubsecTime");
	labels.put(new Integer(0x9291), "EXIFSubsecTimeOrginal");
	labels.put(new Integer(0x9292), "EXIFSubsecTimeDigitized");
	labels.put(new Integer(0xa000), "EXIFFlashPixVersion");
	labels.put(new Integer(0xa001), "EXIFColorSpace");
	labels.put(new Integer(0xa002), "EXIFPixelXDimension");
	labels.put(new Integer(0xa003), "EXIFPixelYDimension");
	labels.put(new Integer(0xa004), "EXIFRelatedSoundFile");
	labels.put(new Integer(0xa005), "EXIFInteroperabilityOffset");
	labels.put(new Integer(0xa20b), "EXIFFlashEnergy");
	labels.put(new Integer(0xa20c), "EXIFSpatialFrequencyResponse");
	labels.put(new Integer(0xa20e), "EXIFFocalPlaneXResolution");
	labels.put(new Integer(0xa20f), "EXIFFocalPlaneYResolution");
	labels.put(new Integer(0xa210), "EXIFFocalPlaneResolutionUnit");
	labels.put(new Integer(0xa214), "EXIFSubjectLocation");
	labels.put(new Integer(0xa215), "EXIFExposureIndex");
	labels.put(new Integer(0xa217), "EXIFSensingMethod");
	labels.put(new Integer(0xa300), "EXIFFileSource");
	labels.put(new Integer(0xa301), "EXIFSceneType");
	labels.put(new Integer(0xa302), "EXIFCFAPattern");
	labels.put(new Integer(0xa401), "EXIFCustomRendered");
	labels.put(new Integer(0xa402), "EXIFExposureMode");
	labels.put(new Integer(0xa403), "EXIFWhiteBalance");
	labels.put(new Integer(0xa404), "EXIFDigitalZoomRatio");
	labels.put(new Integer(0xa405), "EXIFFocalLenIn35mmFilm");
	labels.put(new Integer(0xa406), "EXIFSceneCaptureType");
	labels.put(new Integer(0xa407), "EXIFGainControl");
	labels.put(new Integer(0xa408), "EXIFContrast");
	labels.put(new Integer(0xa409), "EXIFSaturation");
	labels.put(new Integer(0xa40a), "EXIFSharpness");
	labels.put(new Integer(0xa40b), "EXIFDeviceSettingDescr");
	labels.put(new Integer(0xa40c), "EXIFSubjectDistRange");
	labels.put(new Integer(0xa420), "EXIFImageUniqueID");
	labels.put(new Integer(0xffff), "EXIFUnknown");
    }

    private static Map makers = new HashMap();
    static {
        makers.put("Canon", new ExifMakerCanon());
    }

    public Exif (byte[] data)
    throws TiffException {
	super();
        this.tiff = new TiffHeader(data);

        // collect exif fields
        List fields = new ArrayList(tiff.getFields());
	for (Iterator it = tiff.getFields().iterator(); it.hasNext();) {
	    TiffHeader.Field field = (TiffHeader.Field) it.next();
            int tag = field.getTag();
            if (tag == EXIF_T_EXIFIFD || tag == EXIF_T_GPSIFD || tag == EXIF_T_INTEROP) {
                TiffHeader t = new TiffHeader(data, field.getOffset(), tiff.isLittleEndian());
                fields.addAll(t.getFields());
            }
        }
        
        // process entrys
        TiffHeader.Field make = null;
        TiffHeader.Field makernote = null;
        for (Iterator it = fields.iterator(); it.hasNext();) {
            TiffHeader.Field field = (TiffHeader.Field) it.next();
            int tag = field.getTag();
            
            switch (tag) {
                case EXIF_T_MAKE:
                    make = field;
                    break;
                case EXIF_T_MAKERNOTE:
                    makernote = field;
                    break;
                default:
                    Object key = labels.get(new Integer(field.getTag()));
                    Object val = field.getValue();
                    put(key, val);
            }
        }
        
        // process makernote
        if (make != null && makernote != null) {
            ExifMaker maker = (ExifMaker) makers.get(make.getValue());
            if (maker != null) {
                try {
                    putAll(maker.getTags(data, makernote, tiff.isLittleEndian()));
                } catch (Exception ex) {
                    // TODO log failure
                }
            }
        }
    }
}
