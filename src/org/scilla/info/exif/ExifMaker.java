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

import java.util.Map;

import org.scilla.info.TiffHeader;

/**
 * Interface for camera specific makernote reading.
 * @author R.W. van 't Veer
 * @version $Revision: 1.1 $
 */
public interface ExifMaker {
    /**
     * Get camera specific EXIF info.
     * @param data EXIF data block
     * @param makernote EXIF makernote field
     * @param isLittleEndian EXIF block byte order
     * @return a map of camera specific info
     * @throws Exception when tags could not be read
     */
    Map getTags (byte[] data, TiffHeader.Field makernote, boolean isLittleEndian)
    throws Exception;
}
