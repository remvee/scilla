/*
 * scilla
 *
 * Copyright (C) 2005  R.W. van 't Veer
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

package org.scilla.info.exif;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.scilla.info.TiffException;
import org.scilla.info.TiffHeader;

/**
 * EXIF reader.
 *
 * @version $Revision: 1.2 $
 * @author R.W. van 't Veer
 */
public class ExifReader implements Exif {
    private static final Log log = LogFactory.getLog(ExifReader.class);
    
    private static final int EXIF_T_EXIFIFD = 0x8769;
    private static final int EXIF_T_GPSIFD = 0x8825;
    private static final int EXIF_T_INTEROP = 0xa005;
    
    private static final int EXIF_T_MAKE = 0x010f;
    private static final int EXIF_T_MAKERNOTE = 0x927c;

    // exif 2.2 tags
    private static final Map LABELS = new HashMap();
    static {
        LABELS.put(new Integer(0x0100), IMAGE_WIDTH);
        LABELS.put(new Integer(0x0101), IMAGE_LENGTH);
        LABELS.put(new Integer(0x0102), BITS_PER_SAMPLE);
        LABELS.put(new Integer(0x0103), COMPRESSION);
        LABELS.put(new Integer(0x0106), PHOTOMETRIC_INTERPRETATION);
        LABELS.put(new Integer(0x010a), FILL_ORDER);
        LABELS.put(new Integer(0x010d), DOCUMENT_NAME);
        LABELS.put(new Integer(0x010e), IMAGE_DESCRIPTION);
        LABELS.put(new Integer(0x010f), MAKE);
        LABELS.put(new Integer(0x0110), MODEL);
        LABELS.put(new Integer(0x0111), STRIP_OFFSETS);
        LABELS.put(new Integer(0x0112), ORIENTATION);
        LABELS.put(new Integer(0x0115), SAMPLES_PER_PIXEL);
        LABELS.put(new Integer(0x0116), ROWS_PER_STRIP);
        LABELS.put(new Integer(0x0117), STRIP_BYTE_COUNTS);
        LABELS.put(new Integer(0x011a), XRESOLUTION);
        LABELS.put(new Integer(0x011b), YRESOLUTION);
        LABELS.put(new Integer(0x011c), PLANAR_CONFIGURATION);
        LABELS.put(new Integer(0x0128), RESOLUTION_UNIT);
        LABELS.put(new Integer(0x012d), TRANSFER_FUNCTION);
        LABELS.put(new Integer(0x0131), SOFTWARE);
        LABELS.put(new Integer(0x0132), DATE_TIME);
        LABELS.put(new Integer(0x013b), ARTIST);
        LABELS.put(new Integer(0x013e), WHITE_POINT);
        LABELS.put(new Integer(0x013f), PRIMARY_CHROMATICITIES);
        LABELS.put(new Integer(0x0156), TRANSFER_RANGE);
        LABELS.put(new Integer(0x0200), JPEGPROC);
        LABELS.put(new Integer(0x0201), JPEGINTERCHANGE_FORMAT);
        LABELS.put(new Integer(0x0202), JPEGINTERCHANGE_FORMAT_LENGTH);
        LABELS.put(new Integer(0x0211), YCB_CR_COEFFICIENTS);
        LABELS.put(new Integer(0x0212), YCB_CR_SUB_SAMPLING);
        LABELS.put(new Integer(0x0213), YCB_CR_POSITIONING);
        LABELS.put(new Integer(0x0214), REFERENCE_BLACK_WHITE);
        LABELS.put(new Integer(0x828d), CFAREPEAT_PATTERN_DIM);
        LABELS.put(new Integer(0x828e), CFAPATTERN);
        LABELS.put(new Integer(0x828f), BATTERY_LEVEL);
        LABELS.put(new Integer(0x8298), COPYRIGHT);
        LABELS.put(new Integer(0x829a), EXPOSURE_TIME);
        LABELS.put(new Integer(0x829d), FNUMBER);
        LABELS.put(new Integer(0x83bb), IPTC_NAA);
        LABELS.put(new Integer(0x8769), EXIF_OFFSET);
        LABELS.put(new Integer(0x8773), INTER_COLOR_PROFILE);
        LABELS.put(new Integer(0x8822), EXPOSURE_PROGRAM);
        LABELS.put(new Integer(0x8824), SPECTRAL_SENSITIVITY);
        LABELS.put(new Integer(0x8825), GPSINFO);
        LABELS.put(new Integer(0x8827), ISOSPEED_RATINGS);
        LABELS.put(new Integer(0x8828), OECF);
        LABELS.put(new Integer(0x9000), EXIF_VERSION);
        LABELS.put(new Integer(0x9003), DATE_TIME_ORIGINAL);
        LABELS.put(new Integer(0x9004), DATE_TIME_DIGITIZED);
        LABELS.put(new Integer(0x9101), COMPONENTS_CONFIGURATION);
        LABELS.put(new Integer(0x9102), COMPRESSED_BITS_PER_PIXEL);
        LABELS.put(new Integer(0x9201), SHUTTER_SPEED_VALUE);
        LABELS.put(new Integer(0x9202), APERTURE_VALUE);
        LABELS.put(new Integer(0x9203), BRIGHTNESS_VALUE);
        LABELS.put(new Integer(0x9204), EXPOSURE_BIAS_VALUE);
        LABELS.put(new Integer(0x9205), MAX_APERTURE_VALUE);
        LABELS.put(new Integer(0x9206), SUBJECT_DISTANCE);
        LABELS.put(new Integer(0x9207), METERING_MODE);
        LABELS.put(new Integer(0x9208), LIGHT_SOURCE);
        LABELS.put(new Integer(0x9209), FLASH);
        LABELS.put(new Integer(0x920a), FOCAL_LENGTH);
        LABELS.put(new Integer(0x9214), SUBJECT_AREA);
        LABELS.put(new Integer(0x927c), MAKER_NOTE);
        LABELS.put(new Integer(0x9286), USER_COMMENT);
        LABELS.put(new Integer(0x9290), SUBSEC_TIME);
        LABELS.put(new Integer(0x9291), SUBSEC_TIME_ORGINAL);
        LABELS.put(new Integer(0x9292), SUBSEC_TIME_DIGITIZED);
        LABELS.put(new Integer(0xa000), FLASH_PIX_VERSION);
        LABELS.put(new Integer(0xa001), COLOR_SPACE);
        LABELS.put(new Integer(0xa002), PIXEL_XDIMENSION);
        LABELS.put(new Integer(0xa003), PIXEL_YDIMENSION);
        LABELS.put(new Integer(0xa004), RELATED_SOUND_FILE);
        LABELS.put(new Integer(0xa005), INTEROPERABILITY_OFFSET);
        LABELS.put(new Integer(0xa20b), FLASH_ENERGY);
        LABELS.put(new Integer(0xa20c), SPATIAL_FREQUENCY_RESPONSE);
        LABELS.put(new Integer(0xa20e), FOCAL_PLANE_XRESOLUTION);
        LABELS.put(new Integer(0xa20f), FOCAL_PLANE_YRESOLUTION);
        LABELS.put(new Integer(0xa210), FOCAL_PLANE_RESOLUTION_UNIT);
        LABELS.put(new Integer(0xa214), SUBJECT_LOCATION);
        LABELS.put(new Integer(0xa215), EXPOSURE_INDEX);
        LABELS.put(new Integer(0xa217), SENSING_METHOD);
        LABELS.put(new Integer(0xa300), FILE_SOURCE);
        LABELS.put(new Integer(0xa301), SCENE_TYPE);
        LABELS.put(new Integer(0xa302), CFAPATTERN);
        LABELS.put(new Integer(0xa401), CUSTOM_RENDERED);
        LABELS.put(new Integer(0xa402), EXPOSURE_MODE);
        LABELS.put(new Integer(0xa403), WHITE_BALANCE);
        LABELS.put(new Integer(0xa404), DIGITAL_ZOOM_RATIO);
        LABELS.put(new Integer(0xa405), FOCAL_LEN_IN_35MM_FILM);
        LABELS.put(new Integer(0xa406), SCENE_CAPTURE_TYPE);
        LABELS.put(new Integer(0xa407), GAIN_CONTROL);
        LABELS.put(new Integer(0xa408), CONTRAST);
        LABELS.put(new Integer(0xa409), SATURATION);
        LABELS.put(new Integer(0xa40a), SHARPNESS);
        LABELS.put(new Integer(0xa40b), DEVICE_SETTING_DESCR);
        LABELS.put(new Integer(0xa40c), SUBJECT_DIST_RANGE);
        LABELS.put(new Integer(0xa420), IMAGE_UNIQUE_ID);
        LABELS.put(new Integer(0xffff), UNKNOWN);
    }
    
    private static final Set DATE_LABELS = new HashSet();
    static {
        DATE_LABELS.add(DATE_TIME);
        DATE_LABELS.add(DATE_TIME_ORIGINAL);
        DATE_LABELS.add(DATE_TIME_DIGITIZED);
    }

    private static final Map MAKERS = new HashMap();
    static {
        MAKERS.put("Canon", new ExifMakerCanon());
    }

    public static void readIntoMap (byte[] data, Map dest)
    throws TiffException {
        TiffHeader tiff =  new TiffHeader(data);

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
                Object key = LABELS.get(new Integer(field.getTag()));
                if (key == null) {
                    key = "EXIF#" + field.getTag();
                }
                Object val = field.getValue();
                if (DATE_LABELS.contains(key)) {
                    val = parseDate(val);
                }
                dest.put(key, val);
            }
        }
        
        // process makernote
        if (make != null && makernote != null) {
            ExifMaker maker = (ExifMaker) MAKERS.get(make.getValue());
            if (maker != null) {
                try {
                    dest.putAll(maker.getTags(data, makernote, tiff.isLittleEndian()));
                } catch (Exception ex) {
                    log.debug("failed to add camera specific info", ex);
                }
            }
        }
    }
    
    /**
     * Date format according to EXIF 2.2.
     */
    private final static String DATEFORMAT = "yyyy:MM:dd HH:mm:ss";
    
    /**
     * Parse date field.
     * @param in date string
     * @return a date object or <tt>in</tt> if date could not be parsed
     */
    private static Object parseDate(Object in) {
        try {
            return new SimpleDateFormat(DATEFORMAT).parse((String) in);
        } catch (ParseException ex) {
            log.debug("failed to parse date: '" + in + "'", ex);
        } catch (ClassCastException ex) {
            log.debug("date not a string: '" + in + "'", ex);
        }
        return in;
    }
}
