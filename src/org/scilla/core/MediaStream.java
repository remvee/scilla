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

import java.io.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.scilla.*;

/**
 * Media input stream that waits for a file from a runner, catches
 * up with it's progress and read till end of file when runner
 * has finished.
 *
 * @author R.W. van 't Veer
 * @version $Revision: 1.3 $
 */
public class MediaStream extends InputStream {
    private static final Log log = LogFactory.getLog(MediaStream.class);

    /** milis to wait in wait for file loop */
    public static final int WAIT_FOR_FILE_TIMEOUT = 100;
    /** milis to wait for nieuw data in read loop */
    public static final int WAIT_FOR_READ_TIMEOUT = 100;

    String filename;
    RunnerObject runner;

    InputStream in = null;

    public MediaStream (String filename, RunnerObject runner)
    throws ScillaException {
        this.filename = filename;
        this.runner = runner;

        // make sure input file exists
        File f = new File(filename);
        if (! f.exists()) {
            if (runner != null && ! runner.hasFinished()) {
                // wait for file to appear
                while (! f.exists() && ! runner.hasFinished()) {
                    try {
                        Thread.currentThread().sleep(WAIT_FOR_FILE_TIMEOUT);
                    } catch (InterruptedException ex) {
			// ignore
		    }
                }
            }
            // did runner leave any output?
            if (! f.exists()) {
                String err = runner != null ? runner.getErrorMessage() : null;
                if (err != null) {
                    throw new ScillaConversionFailedException(err);
                } else {
                    throw new ScillaNoOutputException();
                }
            }
        }

        // open file for reading
        try {
            in = new FileInputStream(f);
        } catch (FileNotFoundException ex) {
            // will never happen?
            log.error(ex);
            throw new RuntimeException("unexpected "+ex.getClass().getName()+" for "+filename);
        }
    }

    public int read ()
    throws IOException {
        // read without going beyond EOF when k
        while (runner != null && ! runner.hasFinished()) {
            try {
                Thread.currentThread().sleep(WAIT_FOR_READ_TIMEOUT);
            } catch (InterruptedException ex) {
		// ignore
	    }

            if (in.available() > 0) {
                return in.read();
	    }
        }

        return in.read();
    }
    public int read (byte[] b)
    throws IOException {
        // read without going beyond EOF when k
        while (runner != null && ! runner.hasFinished()) {
            try {
                Thread.currentThread().sleep(WAIT_FOR_READ_TIMEOUT);
            } catch (InterruptedException ex) {
		// ignore
	    }

            if (in.available() > 0) {
                return in.read(b);
	    }
        }

        return in.read(b);
    }
    public int read (byte[] b, int off, int len)
    throws IOException {
        // read without going beyond EOF when k
        while (runner != null && ! runner.hasFinished()) {
            try {
                Thread.currentThread().sleep(WAIT_FOR_READ_TIMEOUT);
            } catch (InterruptedException ex) {
		// ignore
	    }

            if (in.available() > 0) {
                return in.read(b, off, len);
	    }
        }

        return in.read(b, off, len);
    }

    public void close ()
    throws IOException {
        if (in != null) {
            in.close();
	}
    }
}
