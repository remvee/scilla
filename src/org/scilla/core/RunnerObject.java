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
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.scilla.*;
import org.scilla.converter.*;

/**
 * A runner object is a media object currently being converted.
 *
 * @author R.W. van 't Veer
 * @version $Revision: 1.25 $
 */
public class RunnerObject implements MediaObject {
    private Converter conv;

    /**
     * Create media object.
     * @param conv ready to run coonverter
     */
    public RunnerObject (Converter conv) {
        this.conv = conv;
    }

    /**
     * Start converter in the background.
     * @see org.scilla.converter.Converter#convert()
     */
    public void start () {
        // start converter thread
        final Converter conv_ = conv;
        final RunnerObject runner_ = this;
        Thread convThread = new Thread() {
            public void run () {
                conv_.convert();
                runner_.fireChangeEvent(RunnerChangeListener.RUNNER_FINISHED);
            }
        };
        convThread.start();
    }

    /**
     * @return true if converter has finished
     */
    public boolean hasFinished () {
        if (finishedFlag) {
            return true;
        }

        if (conv.hasFinished()) {
            finishedFlag = true;

            String fn = conv.getOutputFile();
            if (! exitSuccess()) {
                (new File(fn)).delete();
            }
        }
        return finishedFlag;
    }
    private boolean finishedFlag = false;

    /**
     * A wrapper for Converter.exitSuccess().
     * @return true if converter exit success
     * @see org.scilla.converter.Converter#exitSuccess()
     */
    public boolean exitSuccess () {
        return conv.exitSuccess();
    }

    /**
     * A wrapper for Converter.getErrorMessage().
     * @return conversion error message
     * @see org.scilla.converter.Converter#getErrorMessage()
     */
    public String getErrorMessage() {
        return conv.getErrorMessage();
    }

    /**
     * @return file this runner write to
     */
    public String getOutputFile () {
        return conv.getOutputFile();
    }

    /**
     * @return -1 because file length currently unknown
     */
    public long getLength () {
        return -1;
    }

    public InputStream getStream ()
    throws ScillaException {
        String filename = conv.getOutputFile();
        return new MediaStream(filename, this);
    }

    public void addRunnerChangeListener (RunnerChangeListener listener) {
        listeners.add(listener);
    }
    private List listeners = new Vector();

    public void fireChangeEvent (int code) {
        for (Iterator it = listeners.iterator(); it.hasNext();) {
            RunnerChangeListener listener = (RunnerChangeListener) it.next();
            listener.runnerChange(this, code);
        }
    }

    /**
     * Wait for output and return output filename.
     * @return name of output filename
     */
    public String getFilename () {
        // wait till runner has finished
        while (!hasFinished()) {
            try {
                Thread.sleep(WAIT_FOR_RUNNER_TIMEOUT);
            } catch (InterruptedException ex) {
                // ignore
            }
        }

        // return filename
        return conv.getOutputFile();
    }
    public static final int WAIT_FOR_RUNNER_TIMEOUT = 100;
}
