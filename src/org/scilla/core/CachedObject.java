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
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.scilla.*;

/**
 * A cached object is a media object already existing in cache,
 * finished or not.
 *
 * @author R.W. van 't Veer
 * @version $Revision: 1.18 $
 */
public class CachedObject implements MediaObject {
    private static final Log log = LogFactory.getLog(CachedObject.class);

    /** output file name */
    private String filename;
    /** output producer */
    private RunnerObject runner = null;


    /**
     * Construct media object for cached object.
     * @param filename full name of result file
     */
    public CachedObject (String filename) {
        this.filename = filename;
    }

    /**
     * Construct media object for cached object.
     * @param filename full name of result file
     * @param runner still running output producer
     */
    public CachedObject (String filename, RunnerObject runner) {
        this.filename = filename;
        this.runner = runner;
    }


    public InputStream getStream ()
    throws ScillaException {
        return new MediaStream(filename, runner);
    }

    /**
     * @return file length or -1 if still being generated
     */
    public long getLength () {
        if (runner != null && !runner.hasFinished()) {
            return -1;
        }

        File f = new File(filename);
        if (log.isDebugEnabled()) {
            log.debug("length="+f.length());
        }
        return f.length();
    }

    /**
     * Wait for output and return output filename.
     * @return name of output filename
     */
    public String getFilename () {
        // wait till runner has finished
        while (runner != null && !runner.hasFinished()) {
            try {
                Thread.sleep(WAIT_FOR_RUNNER_TIMEOUT);
            } catch (InterruptedException ex) {
                // ignore
            }
        }

        // return filename
        return filename;
    }
    public static final int WAIT_FOR_RUNNER_TIMEOUT = 100;
}
