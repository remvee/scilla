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
import java.util.Vector;
import java.util.StringTokenizer;

import org.apache.log4j.Category;

/**
 * Execute a OS process.  A semaphore is used to allow only a defined
 * amount of processes to run concurrently.  This process maximum is
 * taken from the scilla configuration.
 *
 * @see org.scilla.Config
 * @author R.W. van 't Veer
 * @version $Revision: 1.9 $
 */
public class QueuedProcess
{
    static Category log = Category.getInstance(QueuedProcess.class);

    public static final String MAX_RUNNERS_PROPERTY = "QueuedProcess.maxRunners";
    public static final String WRAPPER_PROPERTY = "QueuedProcess.wrapper";

    int exitValue = -1;
    Process proc;
    OutputLogger stdout;
    OutputLogger stderr;

    static Semaphore sem = null;
    static int maxRunners = 5;
    static Config config = Config.getInstance();
    static
    {
	// get maxRunners from scilla configuration
	String s = config.getParameter(MAX_RUNNERS_PROPERTY);
	try
	{
	    maxRunners = Integer.parseInt(s);
	}
	catch (NullPointerException npe)
	{
	    log.warn(MAX_RUNNERS_PROPERTY+" not availble, defaulting to: "+maxRunners);
	}
	catch (NumberFormatException nfe)
	{
	    log.warn(MAX_RUNNERS_PROPERTY+" not a number, defaulting to: "+maxRunners, nfe);
	}

	// initialized semaphore
	sem = new Semaphore(maxRunners);
    }

    static Vector wrapper = null;
    static
    {
	// get wrapper script from scilla configuration
	String s = config.getParameter(WRAPPER_PROPERTY);
	if (s != null)
	{
	    wrapper = new Vector();
	    StringTokenizer st = new StringTokenizer(s);
	    while (st.hasMoreTokens())
	    {
		wrapper.add(st.nextToken());
	    }
	}
    }

    /**
     * Create and executed a queued OS process.  The process is
     * created when a semaphore is below the configured value.
     * @param args command arguments
     * @throws IOException when execution fails
     * @see java.lang.Runtime#exec(String[])
     * @see #MAX_RUNNERS_PROPERTY
     */
    public QueuedProcess (String[] args)
    throws IOException
    {
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
     * @see #MAX_RUNNERS_PROPERTY
     */
    public QueuedProcess (String[] args, String[] envp, File dir)
    throws IOException
    {
	if (log.isInfoEnabled())
	{
	    StringBuffer sb = new StringBuffer();
	    for (int i = 0; i < args.length; i++)
	    {
		sb.append(args[i]);
		sb.append(' ');
	    }
	    log.info("process: "+sb);

	    if (envp != null)
	    {
		sb = new StringBuffer();
		for (int i = 0; i < envp.length; i++)
		{
		    sb.append(envp[i]);
		    sb.append(' ');
		}
		log.info("env: "+sb);
	    }
	    if (dir != null) log.info("dir: "+dir);
	}

	// make sure a space exists
	sem.decr();

	// attache wrapper
	if (wrapper != null)
	{
	    int i = 0;
	    String[] targs = new String[wrapper.size() + args.length];

	    for (int j = 0; j < wrapper.size(); j++, i++)
	    {
		targs[i] = (String) wrapper.elementAt(j);
	    }

	    for (int j = 0; j < args.length; j++, i++)
	    {
		targs[i] = args[j];
	    }
	    args = targs;
	}

	// execute process
	try
	{
	    proc = Runtime.getRuntime().exec(args, envp, dir);
	}
	catch (IOException e)
	{
	    sem.incr();
	    throw e;
	}

	// redirect stdout and stderr
	stdout = new OutputLogger(proc.getInputStream());
	stdout.start();
	stderr = new OutputLogger(proc.getErrorStream());
	stderr.start();
    }

    /**
     * Wait for process to finish.
     */
    public synchronized void waitFor ()
    {
	if (proc != null)
	{
	    try { proc.waitFor(); }
	    catch (InterruptedException e) { } // will never happen
	    sem.incr();

	    exitValue = proc.exitValue();
	    proc = null;
	}
    }

    /**
     * Wait for process to finished and return the exit value.
     * @return exit value
     */
    public int exitValue ()
    {
	waitFor();
	return exitValue;
    }

    public String getErrorLog ()
    {
	waitFor();
	return stderr.toString();
    }
}

/**
 * simple semaphore class
 */
class Semaphore
{
    int counter;

    public Semaphore (int arg)
    {
	counter = arg;
    }

    public synchronized void incr ()
    {
	counter++;
	notify();
    }

    public synchronized void decr ()
    {
	while (counter == 0)
	{
	    try { wait(); }
	    catch (InterruptedException e) { } // will never happen
	}
	counter--;
    }
}

/**
 * simple output logging thread
 */
class OutputLogger extends Thread
{
    InputStream in;
    String data;

    public OutputLogger (InputStream in)
    {
	this.in = in;
    }

    public void run ()
    {
	BufferedReader br = new BufferedReader(new InputStreamReader(in));
	StringBuffer sb = new StringBuffer();
	String s;
	try
	{
	    while ((s = br.readLine()) != null)
	    {
		sb.append(s);
		sb.append('\n');
	    }
	}
	catch (IOException e) { /* ignore */ }
	finally
	{
	    try { br.close(); } catch (IOException e ) { /* ignore */ }
	}
	data = sb.toString();
    }

    public String toString ()
    {
	try { this.join(); } catch (InterruptedException ex) { /* ignore */ }
	return data;
    }
}
