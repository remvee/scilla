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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.scilla.info.TiffException;
import org.scilla.info.TiffHeader;

/**
 * Canon specific EXIF stuff.
 * 
 * @see exiftags-0.98
 * @author R.W. van 't Veer
 * @version $Revision: 1.1 $
 */
public class ExifMakerCanon implements ExifMaker {   
    public Map getTags(byte[] data, TiffHeader.Field makernote, boolean isLittleEndian)
    throws TiffException {
        Map result = new HashMap();
        TiffHeader note = new TiffHeader(data, makernote.getOffset(), isLittleEndian);
        for (Iterator it = note.getFields().iterator(); it.hasNext();) {
            TiffHeader.Field f = (TiffHeader.Field) it.next();
            int tag = f.getTag();
            
            switch (tag) {
                case 1:
                    result.putAll(readTag(tag01Table, f));
                    break;
                case 4:
                    result.putAll(readTag(tag04Table, f));
                    break;
                case 6:
                    result.put("CanonImageType", f.getValue());
                    break;
                case 7:
                    result.put("CanonFirmwareVersion", f.getValue());
                    break;
                case 8:
                    long v = ((Long)f.getValue()).longValue();
                    result.put("CanonImageNumber", (v / 10000) + "/" + (v % 10000));
                    break;
                case 9:
                    result.put("CanonOwnerName", f.getValue());
                    break;
                case 12:
                    result.put("CanonSerialNumber", f.getValue());
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    private static Map canon_macro = new HashMap();
    static {
        canon_macro.put(new Integer(1), "macro");
        canon_macro.put(new Integer(2), "normal");
    }
    private static Map canon_quality = new HashMap();
    static {
        canon_quality.put(new Integer(2), "normal");
        canon_quality.put(new Integer(3), "fine");
        canon_quality.put(new Integer(5), "superfine");
    }
    private static Map canon_flash = new HashMap();
    static {
        canon_flash.put(new Integer(0), "off");
        canon_flash.put(new Integer(1), "auto");
        canon_flash.put(new Integer(2), "on");
        canon_flash.put(new Integer(3), "red-eye reduction");
        canon_flash.put(new Integer(4), "slow-synchro");
        canon_flash.put(new Integer(5), "red-eye reduction (auto)");
        canon_flash.put(new Integer(6), "red-eye reduction (on)");
        canon_flash.put(new Integer(16), "external flash");
    }
    private static Map canon_drive = new HashMap();
    static {
        canon_drive.put(new Integer(0), "single");
        canon_drive.put(new Integer(1), "continuous");
    }
    private static Map canon_focus1 = new HashMap();
    static {
        canon_focus1.put(new Integer(0), "one-shot");
        canon_focus1.put(new Integer(1), "AI servo");
        canon_focus1.put(new Integer(2), "AI focus");
        canon_focus1.put(new Integer(3), "manual");
        canon_focus1.put(new Integer(4), "single");
        canon_focus1.put(new Integer(5), "continuous");
        canon_focus1.put(new Integer(6), "manual");
    }
    private static Map canon_imagesz = new HashMap();
    static {
        canon_imagesz.put(new Integer(0), "large");
        canon_imagesz.put(new Integer(1), "medium");
        canon_imagesz.put(new Integer(2), "small");
    }
    private static Map canon_shoot = new HashMap();
    static {
        canon_shoot.put(new Integer(0), "full auto");
        canon_shoot.put(new Integer(1), "manual");
        canon_shoot.put(new Integer(2), "landscape");
        canon_shoot.put(new Integer(3), "fast shutter");
        canon_shoot.put(new Integer(4), "slow shutter");
        canon_shoot.put(new Integer(5), "night");
        canon_shoot.put(new Integer(6), "black and white");
        canon_shoot.put(new Integer(7), "sepia");
        canon_shoot.put(new Integer(8), "portrait");
        canon_shoot.put(new Integer(9), "sports");
        canon_shoot.put(new Integer(10), "macro");
        canon_shoot.put(new Integer(11), "pan focus");
    }
    private static Map canon_range = new HashMap();
    static {
        canon_range.put(new Integer(0), "normal");
        canon_range.put(new Integer(1), "high");
        canon_range.put(new Integer(0xffff), "low");
    }
    private static Map canon_iso = new HashMap();
    static {
        canon_iso.put(new Integer(15), "auto");
        canon_iso.put(new Integer(16), new Integer(50));
        canon_iso.put(new Integer(17), new Integer(100));
        canon_iso.put(new Integer(18), new Integer(200));
        canon_iso.put(new Integer(19), new Integer(400));
    }
    private static Map canon_meter = new HashMap();
    static {
        canon_meter.put(new Integer(3), "evaluative");
        canon_meter.put(new Integer(4), "partial");
        canon_meter.put(new Integer(5), "center-weighted");
    }
    private static Map canon_focustype = new HashMap();
    static {
        canon_focustype.put(new Integer(0), "manual");
        canon_focustype.put(new Integer(1), "auto");
        canon_focustype.put(new Integer(2), "auto");
        canon_focustype.put(new Integer(3), "closeup/macro");
        canon_focustype.put(new Integer(7), "infinity mode");
        canon_focustype.put(new Integer(8), "locked (pan mode)");
    }
    private static Map canon_expmode = new HashMap();
    static {
        canon_expmode.put(new Integer(0), "easy shooting");
        canon_expmode.put(new Integer(1), "program");
        canon_expmode.put(new Integer(2), "tv-priority");
        canon_expmode.put(new Integer(3), "av-priority");
        canon_expmode.put(new Integer(4), "manual");
        canon_expmode.put(new Integer(5), "A-DEP");
        canon_expmode.put(new Integer(6), "DEP");
    }
    private static Map canon_whitebalance = new HashMap();
    static {
        canon_whitebalance.put(new Integer(0), "auto");
        canon_whitebalance.put(new Integer(1), "daylight");
        canon_whitebalance.put(new Integer(2), "cloudy");
        canon_whitebalance.put(new Integer(3), "tungsten");
        canon_whitebalance.put(new Integer(4), "fluorescent");
        canon_whitebalance.put(new Integer(5), "flash");
        canon_whitebalance.put(new Integer(6), "custom");
        canon_whitebalance.put(new Integer(7), "black and white");
        canon_whitebalance.put(new Integer(8), "shade");
        canon_whitebalance.put(new Integer(9), "manual temperature");
    }
    private static Map canon_fbias = new HashMap();
    static {
        canon_fbias.put(new Integer(0x0000), "0 EV");
        canon_fbias.put(new Integer(0x000c), "0.33 EV");
        canon_fbias.put(new Integer(0x0010), "0.50 EV");
        canon_fbias.put(new Integer(0x0014), "0.67 EV");
        canon_fbias.put(new Integer(0x0020), "1 EV");
        canon_fbias.put(new Integer(0x002c), "1.33 EV");
        canon_fbias.put(new Integer(0x0030), "1.50 EV");
        canon_fbias.put(new Integer(0x0034), "1.67 EV");
        canon_fbias.put(new Integer(0x0040), "2 EV");
        canon_fbias.put(new Integer(0xffc0), "-2 EV");
        canon_fbias.put(new Integer(0xffcc), "-1.67 EV");
        canon_fbias.put(new Integer(0xffd0), "-1.50 EV");
        canon_fbias.put(new Integer(0xffd4), "-1.33 EV");
        canon_fbias.put(new Integer(0xffe0), "-1 EV");
        canon_fbias.put(new Integer(0xffec), "-0.67 EV");
        canon_fbias.put(new Integer(0xfff0), "-0.50 EV");
        canon_fbias.put(new Integer(0xfff4), "-0.33 EV");
    }

    private static class CanonTag {
        String name;
        Map trans;
        CanonTag (String name, Map trans) {
            this.name = name;
            this.trans = trans;
        }
    }
    
    private static CanonTag[] tag01Table = new CanonTag[] {
        null, // 0
        new CanonTag("CanonMacroMode", canon_macro), // 1
        new CanonTag("CanonTimerLen", null), // 2
        new CanonTag("CanonQuality", canon_quality), // 3
        new CanonTag("CanonFlashMode", canon_flash), // 4
        new CanonTag("CanonDriveMode", canon_drive), // 5
        null, // 6
        new CanonTag("CanonFocusMode", canon_focus1), // 7
        null, null, // 8,9
        new CanonTag("CanonImageSize", canon_imagesz), // 10 
        new CanonTag("CanonShootMode", canon_shoot), // 11
        new CanonTag("CanonDigiZoom", null), // 12
        new CanonTag("CanonContrast", canon_range), // 13
        new CanonTag("CanonSaturate", canon_range), // 14
        new CanonTag("CanonSharpness", canon_range), // 15
        new CanonTag("EXIFISOSpeedRatings", canon_iso), // 16
        new CanonTag("EXIFMeteringMode", canon_meter), // 17
        new CanonTag("CanonFocusType", canon_focustype), // 18
        new CanonTag("CanonAFPoint", null), // 19
        new CanonTag("CanonExpMode", canon_expmode), // 20
        null, null, // 21,22
        new CanonTag("CanonMaxFocal", null), // 23
        new CanonTag("CanonMinFocal", null), // 24
        new CanonTag("CanonFocalUnits", null), // 25
        null, null, // 26,27
        new CanonTag("CanonFlashActivity", null), // 28
        new CanonTag("CanonFlashDetails", null), // 29
        null, null, null, null, null, null, // 30,31,32,33,34,35
        new CanonTag("CanonDZoomRes", null), // 36
        new CanonTag("CanonBZoomRes", null), // 37
    };
    private static CanonTag[] tag04Table = new CanonTag[] {
        null, null, null, null, null, null, null, // 0,1,2,3,4,5,6
        new CanonTag("EXIFWhiteBalance", canon_whitebalance), // 7
        null, // 8
        new CanonTag("CanonSequence", null), // 9
        null, null, null, null, // 10,11,12,13
        new CanonTag("CanonAFPoint2", null), // 14
        new CanonTag("CanonFlashBias", canon_fbias), // 15
        null, null, null, // 16,17,18
        new CanonTag("CanonSubjectDistance", null), // 19
    };
    
    private Map readTag (CanonTag[] table, TiffHeader.Field field) {
        Map result = new HashMap();
        int[] data = (int[]) field.getValue();
        int l = Math.min(data.length, table.length);
        for (int i = 1; i < l; i++) {
            CanonTag f = table[i];
            if (f != null) {
                Object val = new Integer(data[i]);
                if (f.trans != null && f.trans.get(val) != null) {
                    val = f.trans.get(val);
                }
                result.put(f.name, val);
            }
        }
        return result;
    }
}
