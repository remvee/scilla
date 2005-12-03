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
 * @version $Revision: 1.6 $
 */
public class MediaStream extends InputStream {
    private static final Log log = LogFactory.getLog(MediaStream.class);
    private static final Config config = Config.getInstance();

    /** milis to wait for file */
    public static int timeoutForFile = 60000;
    /** milis to wait in wait loop for file loop */
    public static int sleepForFile = 100;
    /** milis to wait for new data */
    public static int timeoutForRead = 30000;
    /** milis to wait for new data in read loop */
    public static int sleepForRead = 100;

    public static final String TIMEOUT_FOR_FILE_KEY = "mediastream.timeout_for_file";
    public static final String SLEEP_FOR_FILE_KEY = "mediastream.sleep_for_file";
    public static final String TIMEOUT_FOR_READ_KEY = "mediastream.timeout_for_read";
    public static final String SLEEP_FOR_READ_KEY = "mediastream.sleep_for_read";
    static {
        // get timeouts from configuration
        try {
            if (config.containsKey(TIMEOUT_FOR_FILE_KEY)) {
                timeoutForFile = config.getInt(TIMEOUT_FOR_FILE_KEY);
            }
            if (config.containsKey(SLEEP_FOR_FILE_KEY)) {
                sleepForFile = config.getInt(SLEEP_FOR_FILE_KEY);
            }
            if (config.containsKey(TIMEOUT_FOR_READ_KEY)) {
                timeoutForRead = config.getInt(TIMEOUT_FOR_READ_KEY);
            }
            if (config.containsKey(SLEEP_FOR_READ_KEY)) {
                sleepForRead = config.getInt(SLEEP_FOR_READ_KEY);
            }
        } catch (Exception ex) {
            log.warn("failed to set timeout from configuration", ex);
        }
    }

    private String filename;
    private RunnerObject runner;

    private InputStream in = null;

    public MediaStream (String filename, RunnerObject runner)
    throws ScillaException {
        this.filename = filename;
        this.runner = runner;

        // make sure input file exists
        File f = new File(filename);
        if (! f.exists()) {
            if (runner != null && ! runner.hasFinished()) {
                // wait for file to appear
                int timeout = timeoutForFile / sleepForFile;
                for (; timeout > 0 && ! f.exists() && ! runner.hasFinished(); timeout--) {
                    try {
                        Thread.sleep(sleepForFile);
                    } catch (InterruptedException ex) {
                        // ignore
                    }
                }
                if (timeout <= 0) {
                    throw new ScillaException("timeout waiting for output");
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
        int timeout = timeoutForRead / sleepForRead;
        for (; timeout > 0 && runner != null && ! runner.hasFinished(); timeout--) {
            try {
                Thread.sleep(sleepForRead);
            } catch (InterruptedException ex) {
                // ignore
            }

            if (in.available() > 0) {
                return in.read();
            }
        }
        if (timeout <= 0) {
            throw new IOException("timeout waiting for data");
        }

        return in.read();
    }
    public int read (byte[] b)
    throws IOException {
        // read without going beyond EOF when k
        int timeout = timeoutForRead / sleepForRead;
        for (; timeout > 0 && runner != null && ! runner.hasFinished(); timeout--) {
            try {
                Thread.sleep(sleepForRead);
            } catch (InterruptedException ex) {
                // ignore
            }

            if (in.available() > 0) {
                return in.read(b);
            }
        }
        if (timeout <= 0) {
            throw new IOException("timeout waiting for data");
        }

        return in.read(b);
    }
    public int read (byte[] b, int off, int len)
    throws IOException {
        // read without going beyond EOF when k
        int timeout = timeoutForRead / sleepForRead;
        for (; timeout > 0 && runner != null && ! runner.hasFinished(); timeout--) {
            try {
                Thread.sleep(sleepForRead);
            } catch (InterruptedException ex) {
                // ignore
            }

            if (in.available() > 0) {
                return in.read(b, off, len);
            }
        }
        if (timeout <= 0) {
            throw new IOException("timeout waiting for data");
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
