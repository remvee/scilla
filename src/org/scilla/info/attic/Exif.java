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

import java.io.IOException;
import java.util.*;

/**
 * EXIF.
 *
 * @version $Revision: 1.3 $
 * @author R.W. van 't Veer
 */
public class Exif extends HashMap {
    private boolean littleEndian;
    private byte[] data;

    private static final int EXIF_T_EXIFIFD = 0x8769;
    private static final int EXIF_T_GPSIFD = 0x8825;
    private static final int EXIF_T_INTEROP = 0xa005;

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

    public Exif (byte[] in)
    throws IOException {
	super();

	int pos = 0;
	data = in;

	// determine byte order
	{
	    int c1 = read1ByteInt(data, pos);
	    pos++;
	    int c2 = read1ByteInt(data, pos);
	    pos++;
	    if (c1 == 'I' && c2 == 'I') {
		littleEndian = true;
	    } else if (c1 == 'M' && c2 == 'M') {
		littleEndian = false;
	    } else {
		throw new IOException("not a tiff; missing II or MM");
	    }
	}

	// verify endianess
	if (read2ByteInt(data, pos) != 42) {
	    throw new IOException("not a tiff; byte order broken");
	}
	pos += 2;

	// goto first IFD
	pos = read4ByteInt(data, pos);

	// read all IFDs
	List ifds = new ArrayList();
	List exififds = new ArrayList();
	readIfds(data, pos, ifds, exififds);

	// process exif ifds
	for (Iterator it = exififds.iterator(); it.hasNext();) {
	    IFD ifd = (IFD) it.next();
	    int ipos = (int) ((Long) ifd.val).longValue();
	    readIfds(data, ipos, ifds, null);
	}

	// put ifds in map
	for (Iterator it = ifds.iterator(); it.hasNext();) {
	    IFD ifd = (IFD) it.next();
	    String key = (String) labels.get(new Integer(ifd.tag));
	    Object val = ifd.val;
	    put(key, val);
	}
    }

    private int read1ByteInt (byte[] data, int pos) {
	return data[pos] & 0xff;
    }

    private int read2ByteInt (byte[] data, int pos) {
	int n1 = data[pos++] & 0xff;
	int n2 = data[pos] & 0xff;

	return littleEndian
		? ((n2 << 8) | n1)
		: ((n1 << 8) | n2);
    }

    private int read4ByteInt (byte[] data, int pos) {
	int n1 = data[pos++] & 0xff;
	int n2 = data[pos++] & 0xff;
	int n3 = data[pos++] & 0xff;
	int n4 = data[pos] & 0xff;

	return littleEndian
		? ((n4 << 24) | (n3 << 16) | (n2 << 8) | n1)
		: ((n1 << 24) | (n2 << 16) | (n3 << 8) | n4);
    }

    private void readIfds(byte[] data, int pos, List ifds, List exififds) {
        int p = pos;
	while (p != 0) {
	    int num = read2ByteInt(data, p);
	    p += 2;

	    for (int i = 0; i < num; i++) {
		IFD ifd = new IFD(data, p);
		p += 12;

		ifds.add(ifd);

		if (exififds != null) {
		    switch (ifd.tag) {
			case EXIF_T_EXIFIFD:
			case EXIF_T_GPSIFD:
			case EXIF_T_INTEROP:
			    exififds.add(ifd);
                        default:
		    }
		}
	    }

	    p = read4ByteInt(data, p);
	}
    }

    private class IFD {
	int tag;
	int type;
	int len;
	int voffset;
	byte[] data;
	Object val;

	IFD (byte[] idata, int pos) {
	    data = idata;

	    tag = read2ByteInt(data, pos);
	    pos += 2;
	    type = read2ByteInt(data, pos);
	    pos += 2;
	    len = read4ByteInt(data, pos);
	    pos += 4;
	    voffset = read4ByteInt(data, pos);

	    // read value
	    val = null;
	    switch (type) {
		case 1: // BYTE
		case 6: // SIGNED BYTE
		    if (len == 1) {
			short v = (short) (voffset & 0xff);
			val = new Short(isSigned(type) ? signedByte(v) : v);
		    } else {
			int p = len > 4 ? voffset : pos;
			byte[] d = new byte[len];
			for (int i = 0; i < len; i++) {
			    d[i] = data[p++];
			}
			val = d;
		    }
		    break;
		case 2: // ASCII
		    {
			int i = len > 4 ? voffset : pos;
			StringBuffer sb = new StringBuffer();
			for (; data[i] != 0; i++) {
			    sb.append((char) data[i]);
			}
			val = sb.toString();
		    }
		    break;
		case 3: // SHORT 16-bit unsigned
		case 8: // SSHORT 16-bit signed
		    if (len == 1) {
			int v = voffset & 0xffff;
			val = new Integer(isSigned(type) ? signedShort(v) : v);
		    } else {
			int p = len > 2 ? voffset : pos;
			int[] d = new int[len];
			for (int i = 0; i < len; i++) {
			    int v = read2ByteInt(data, p);
			    d[i] = isSigned(type) ? signedShort(v) : v;
			    p += 2;
			}
			val = d;
		    }
		    break;
		case 4: // LONG 32-bit unsigned
		case 9: // SLONG 32-bit signed
		    if (len == 1) {
			long v = voffset & 0xffffffff;
			val = new Long(isSigned(type) ? signedLong(v) : v);
		    } else {
			int p = len > 1 ? voffset : pos;
			long[] d = new long[len];
			for (int i = 0; i < len; i++) {
			    long v = read4ByteInt(data, p);
			    d[i] = isSigned(type) ? signedLong(v) : v;
			    p += 4;
			}
			val = d;
		    }
		    break;
		case 5: // RATIONAL
		case 10: // SRATIONAL
		    if (len == 1) {
			long numerator = (long) read4ByteInt(data, voffset);
			long demoninator = (long) read4ByteInt(data, voffset + 4);
			if (isSigned(type)) {
			    numerator = signedLong(numerator);
			    demoninator = signedLong(demoninator);
			}
			val = new Double((double) numerator / (double) demoninator);
		    }
		    break;
		case 7: // UNDEF
		    val = null;
		    break;
		case 11: // FLOAT
		    val = "IMPLEMENT FLOAT!";
		    break;
		case 12: // DOUBLE
		    val = "IMPLEMENT DOUBLE!";
		    break;
		default: // UNKNOWN
		    val = "UNKNOWN";
	    }
	}
    }

    private static boolean isSigned (int type) {
	switch (type) {
	    case 6: case 8: case 9: case 10:
		return true;
            default:
                return false;
	}
    }

    private static long signedLong (long v) {
	return (v & 0x80000000L) != 0 ?  v | 0xffffffff00000000L : v;
    }

    private static int signedShort (int v) {
	return (v & 0x8000) != 0 ?  v | 0xffff0000 : v;
    }

    private static short signedByte (short v) {
	return (v & 0x80) != 0 ? (short) (v | 0xff00) : v;
    }
}
