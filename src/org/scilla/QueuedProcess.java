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

package org.scilla;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.scilla.util.Semaphore;

/**
 * Execute a OS process.  A semaphore is used to allow only a defined
 * amount of processes to run concurrently.  This process maximum is
 * taken from the scilla configuration.
 *
 * @author R.W. van 't Veer
 * @version $Revision: 1.23 $
 */
public class QueuedProcess {
    private static final Log log = LogFactory.getLog(QueuedProcess.class);
    private static final Config config = Config.getInstance();

    public static final String MAX_RUNNERS_KEY = "converters.osprocess.runners.sem";
    public static final String WRAPPER_KEY = "converters.osprocess.wrapper.exec";

    private volatile int exitValue = -1;
    private volatile Thread thrd = null;
    private volatile Throwable crash = null;
    private volatile OutputLog stdout;
    private volatile OutputLog stderr;

    private static Semaphore sem = null;
    private static int maxRunners = 5;
    static
    {
        // get maxRunners from scilla configuration
        try {
            maxRunners = Integer.parseInt(config.getString(MAX_RUNNERS_KEY));
        } catch (Exception ex) {
            log.warn(MAX_RUNNERS_KEY+" not available, defaulting to: "+maxRunners);
        }
        // initialized semaphore
        sem = new Semaphore(maxRunners);
    }

    /**
     * Create and executed a queued OS process.  The process is
     * created when a semaphore is below the configured value.
     * @param args command arguments
     * @throws IOException when execution fails
     * @see java.lang.Runtime#exec(String[])
     * @see #MAX_RUNNERS_KEY
     */
    public QueuedProcess (String[] args)
    throws IOException {
        this(args, null, null);
    }

    /**
     * Create and executed a queued OS process.  The process is
     * created when a semaphore is below the configured value.
     * @param args command arguments
     * @param envp array of strings, each element of which has
     * environment variable settings in format name=value
     * @param dir working directory
     * @throws IOException when execution fails
     * @see java.lang.Runtime#exec(String[])
     * @see #MAX_RUNNERS_KEY
     */
    public QueuedProcess (String[] args, String[] envp, File dir)
    throws IOException {
        // attache wrapper
        String[] wrapper = config.getStringArray(WRAPPER_KEY);
        if (wrapper != null) {
            String[] targs = new String[wrapper.length + args.length];
            System.arraycopy(wrapper, 0, targs, 0, wrapper.length);
            System.arraycopy(args, 0, targs, wrapper.length, args.length);
            args = targs;
        }

        // log execution
        if (log.isInfoEnabled()) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < args.length; i++) {
                sb.append(args[i]);
                sb.append(' ');
            }
            log.info("process: "+sb);

            if (log.isDebugEnabled() && envp != null) {
		if (envp != null) {
		    sb = new StringBuffer();
		    for (int i = 0; i < envp.length; i++) {
			sb.append(envp[i]);
			sb.append(' ');
		    }
		    log.debug("env: "+sb);
		}
		if (dir != null) {
		    log.debug("dir: "+dir);
		}
	    }
        }

        // execute process
	final String[] args_ = args;
	final String[] envp_ = envp;
	final File dir_ = dir;
	thrd = new Thread () {
	    public void run () {
		try {
		    sem.decr();
		    log.debug("process started");
		    Process proc = Runtime.getRuntime().exec(args_, envp_, dir_);

		    // redirect stdout and stderr
		    stdout = new OutputLog(proc.getInputStream());
		    stdout.start();
		    stderr = new OutputLog(proc.getErrorStream());
		    stderr.start();

		    // wait for process to finish
		    proc.waitFor();
		    // record exit value
		    exitValue = proc.exitValue();
		} catch (Throwable ex) {
		    log.error("process execution failed", ex);

		    // record exception
		    crash = ex;
		} finally {
		    log.debug("process finished");
		    sem.incr();
		}
	    }
	};
	thrd.setDaemon(true);
	thrd.start();
    }

    /**
     * Wait for process to finish.
     */
    public synchronized void waitFor () {
        if (thrd != null) {
            try {
                thrd.join();
            } catch (InterruptedException e) {
		// will never happen
	    }
            thrd = null;
        }
    }

    /**
     * Wait for process to finished and return the exit value.
     * @return exit value
     */
    public int exitValue () {
        waitFor();
        return exitValue;
    }

    public String getErrorLog () {
        waitFor();
        return stderr.toString();
    }
}

/**
 * simple output logging thread
 */
class OutputLog extends Thread {
    InputStream in;
    String data;

    public OutputLog (InputStream in) {
	super();
        this.in = in;
    }

    public void run () {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuffer sb = new StringBuffer();
        String s;
        try {
            while ((s = br.readLine()) != null) {
                sb.append(s);
                sb.append('\n');
            }
        } catch (IOException e) {
	    // ignore
        } finally {
            try {
                br.close();
            } catch (IOException e ) {
		// ignore
            }
        }
        data = sb.toString();
    }

    public String toString () {
        try {
            this.join();
        } catch (InterruptedException ex) {
	    // ignore
        }
        return data;
    }
}
