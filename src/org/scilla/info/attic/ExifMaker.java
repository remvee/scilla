package org.scilla.info;

import java.util.Map;

/**
 * Interface for camera specific makernote reading.
 * @author R.W. van 't Veer
 * @version $Revision: 1.2 $
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
