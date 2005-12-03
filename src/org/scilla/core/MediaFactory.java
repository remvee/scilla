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

package org.scilla.core;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.scilla.*;
import org.scilla.converter.*;

/**
 * The MediaFactory creates a runner or file object.
 *
 * @version $Revision: 1.20 $
 * @author R.W. van 't Veer
 */
public class MediaFactory {
    private static final Log log = LogFactory.getLog(MediaFactory.class);
    private static final Config config = Config.getInstance();

    /** config key to converter class list */
    public static final String CONVERTERS_KEY = "converters.classes";

    /**
     * Create a runner or file object for given request.
     * @param req media object request
     * @param ofn output file name
     * @return runner or file media object
     */
    public static MediaObject createObject (Request req, String ofn)
    throws ScillaException {
        if (log.isDebugEnabled()) {
            log.debug("createObject("+req+", "+ofn+")");
        }

        // see if source exists
        String file = req.getInputFile();
        if (file != null && ! (new File(file).exists())) {
            throw new ScillaNoInputException();
        }

        // its a conversionless hit
        if (! req.needConverter()) {
            log.debug("createObject: conversionless hit");
            return new FileObject(req.getInputFile());
        }

        // find appropriate converter
        Converter conv = null;
        Class[] convs = config.getClassArray(CONVERTERS_KEY);
        for (int i = 0; i < convs.length; i++) {
            Converter c;
            try {
                c = (Converter) convs[i].newInstance();
            } catch (Exception ex) {
                log.warn("createObject: unable to get instance for: "+convs[i], ex);
                continue;
            }

            if (c.canConvert(req)) {
                conv = c;
                break;
            }
        }
        if (conv == null) {
            throw new ScillaNoConverterException();
        }

        // configure converter
        conv.setRequest(req);
        conv.setOutputFile(ofn);

        // remove old output
        (new File(ofn)).delete();

        // log creation of converter
        if (log.isDebugEnabled()) {
            log.debug("createObject: create runner with: "+conv.getClass().getName());
        }

        // create runner object
        return new RunnerObject(conv);
    }
}
