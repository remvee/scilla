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

import java.io.FileOutputStream;
import java.util.Iterator;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import java.awt.image.renderable.ParameterBlock;

import org.scilla.*;
import org.scilla.core.*;
import org.scilla.util.*;

/**
 * The <STRONG>incomplete</STRONG> "Java Advanced Imaging" Converter.
 * Currently this converter only supports the <CODE>size</CODE>
 * parameter.
 *
 * @author R.W. van 't Veer
 * @version $Revision: 1.11 $
 */
public class JAIConverter implements Converter {
    static Logger log = LoggerFactory.get(JAIConverter.class);

    /** parameter name to force the use of this converter */
    public final static String THIS_CONVERTER_PARAMETER = "jai";

    Request request = null;
    String outputFile = null;

    volatile String errorMessage = null;
    volatile int exitValue = 0; // 0 means success
    volatile boolean finished = false;
    volatile boolean started = false;

    static final String[] inputTypeList = new String[] {
	"image/gif", "image/jpeg", "image/png", "image/tiff",
	"image/x-ms-bmp", "image/x-portable-anymap",
	"image/x-portable-graymap"
    };
    static final String[] outputTypeList = new String[] {
	"image/jpeg", "image/png", "image/tiff",
	"image/x-ms-bmp", "image/x-portable-anymap",
	"image/x-portable-graymap"
    };
    static final String[] parameterList = new String[] {
	THIS_CONVERTER_PARAMETER, Request.OUTPUT_TYPE_PARAMETER,
	"scale"
    };

    public void convert () {
        started = true;
        try {
            // lets hope JAI will recognized the input type
            PlanarImage img = JAI.create("fileload", request.getInputFile());

            // loop through parameters
            for (Iterator it = request.getParameters().iterator(); it.hasNext(); ) {
                RequestParameter rp = (RequestParameter) it.next();

                if (rp.key.equals(Request.OUTPUT_TYPE_PARAMETER)) {
                    // handled outside this loop
                } else if (rp.key.equals(THIS_CONVERTER_PARAMETER)) {
                    // force use of this converter; ignore
                } else {
                    img = handleConversion(img, rp);
                }
            }

            // recode to output format
            String type = MimeType.getExtensionForType(request.getOutputType());
            FileOutputStream out = new FileOutputStream(outputFile);
            JAI.create("encode", img, out, type, null);
        } catch (Exception e) {
            exitValue = 1;
            errorMessage = e.getMessage();
        }
        finished = true;
    }

    public boolean exitSuccess () {
        if (! finished) {
            throw new IllegalStateException();
	}
        return exitValue == 0;
    }

    public String getErrorMessage () {
        if (! finished) {
            throw new IllegalStateException();
	}
        return errorMessage;
    }

    public void setOutputFile (String fn) {
        if (started) {
            throw new IllegalStateException();
	}
        outputFile = fn;
    }

    public String getOutputFile () {
        return outputFile;
    }

    public boolean hasFinished () {
        return finished;
    }

    public boolean canConvert (Request req) {
        boolean flag;

        // can handle input?
        final String inType = req.getInputType();
        flag = false;
        for (int i = 0; i < inputTypeList.length; i++) {
            if (inType.equals(inputTypeList[i])) {
                flag = true;
                break;
            }
        }
        if (! flag) {
            return false;
	}

        // can handle output?
        final String outType = req.getOutputType();
        flag = false;
        for (int i = 0; i < outputTypeList.length; i++) {
            if (outType.equals(outputTypeList[i])) {
                flag = true;
                break;
            }
        }
        if (! flag) {
            return false;
	}

        // supports all parameters?
        Iterator it = req.getParameterKeys().iterator();
        flag = false;
        while (it.hasNext()) {
            String key = (String) it.next();
            for (int i = 0; i < parameterList.length; i++) {
                if (key.equals(parameterList[i])) {
                    flag = true;
                }
            }
            if (! flag) {
                return false;
	    }
        }

        return true;
    }

    public void setRequest (Request req) {
        request = req;
    }

    /**
     * Handle step in conversion.
     * @param img source image
     * @param rp conversion parameter
     * @return result image
     */
    PlanarImage handleConversion (PlanarImage img, RequestParameter rp) {
        if (rp.key.equals("scale")) {
            return scale(img, new GeometryParameter(rp.val));
        }

        log.warn("handleConversion: param '"+rp.key+"' NOT YET IMPLEMENTED");
        return null;
    }

    /**
     * Scale image complying to ImageMagick geometry format.
     * Format: <CODE>WxH[+-]X[+-]Y[%][!][&lt;&gt;]</CODE>
     * @param img source image
     * @param geom geometry conversion parameter
     * @return result image
     */
    PlanarImage scale (PlanarImage img, GeometryParameter geom) {
        float w = geom.width;
        float h = geom.height != 0 ? geom.height : geom.width;
        int iw = img.getWidth();
        int ih = img.getHeight();

        if (geom.hasOption('<') && w < iw && h < ih) {
            return img;
	}
        if (geom.hasOption('>') && w > iw && h > ih) {
            return img;
	}

        if (geom.hasOption('%')) {
            w = (float) w / 100;
            h = (float) h / 100;
        } else {
            w = (float) w / iw;
            h = (float) h / ih;
        }

        if (! geom.hasOption('!')) {
            // keep ratio
            if (w < h) {
                h = w;
	    } else {
                w = h;
	    }
        }

        float x = geom.x;
        float y = geom.y;

        ParameterBlock pars = new ParameterBlock();
        pars.addSource(img);
        pars.add(w);
        pars.add(h);
        pars.add(x);
        pars.add(y);

        return JAI.create("scale", pars);
    }
}

/**
 * Class for mapping ImageMagick scale format.
 */
class GeometryParameter {
    int width;
    int height;
    int x;
    int y;
    String options;

    /**
     * Parse ImageMagick like scale format.
     */
    GeometryParameter (String in) {
        int i;
        char[] d = in.toCharArray();
        StringBuffer b;

        do {
            // width
            b = new StringBuffer();
            for (i = 0; i < d.length && Character.isDigit(d[i]); i++) {
                b.append(d[i]);
            }
            if (b.length() > 0) {
                width = Integer.parseInt(b.toString());
	    }

            // height
            if (i >= d.length) {
                break;
	    }
            if (d[i] == 'x' || d[i] == 'X') {
                b = new StringBuffer();
                for (i++; i < d.length && Character.isDigit(d[i]); i++) {
                    b.append(d[i]);
                }
                if (b.length() > 0) {
                    height = Integer.parseInt(b.toString());
		}
            }

            // x
            if (i >= d.length) {
                break;
	    }
            if (d[i] == '+' || d[i] == '-') {
                b = new StringBuffer();
                for (; i < d.length && Character.isDigit(d[i]); i++) {
                    b.append(d[i]);
                }
                if (b.length() > 0) {
                    x = Integer.parseInt(b.toString());
		}
            }

            // y
            if (i >= d.length) {
                break;
	    }
            if (d[i] == '+' || d[i] == '-') {
                b = new StringBuffer();
                for (; i < d.length && Character.isDigit(d[i]); i++) {
                    b.append(d[i]);
                }
                x = Integer.parseInt(b.toString());
            }

            // options
            if (i < d.length) {
                options = new String(d, i, d.length-i);
	    }
        } while (false); // only ones
    }

    boolean hasOption (char c) {
        return options != null && options.indexOf(c) != -1;
    }
}
