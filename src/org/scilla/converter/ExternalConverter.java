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

import java.io.File;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.scilla.*;
import org.scilla.core.*;
import org.scilla.util.*;

/**
 * External converter class.  Takes converter configurations from
 * scilla configuration.
 * <DL>
 * <DT><CODE>converters.external.NAME.exec</CODE></DT>
 *     <DD>
 *         <CODE>exec</CODE> holds the executable name for this converter.
 *     </DD>
 * <DT><CODE>converters.external.NAME.format</CODE></DT>
 *     <DD>
 *         <CODE>format</CODE> holds the commandline format for this
 *         command.  Typically something like <CODE>sio</CODE>.
 *         <CODE>s</CODE> stands for switches block, <CODE>i</CODE> input
 *         file (including optional input switches) and <CODE>o</CODE>
 *         output file (same as <CODE>i</CODE>).
 *     </DD>
 * <DT><CODE>converters.external.NAME.silent_switch</CODE></DT>
 *     <DD>
 *         <EM>Optional</EM> switch to suppress progress info, like
 *         <CODE>-q</CODE>.
 *     </DD>
 * <DT><CODE>converters.external.NAME.ignore_exitstat</CODE></DT>
 *     <DD>
 *         <EM>Optional</EM>, ignore process exit status.
 *     </DD>
 * <DT><CODE>converters.external.NAME.inputtypes</CODE></DT>
 *     <DD>
 *         Spaces separated allowed input mime-types list.
 *     </DD>
 * <DT><CODE>converters.external.NAME.outputtypes</CODE></DT>
 *     <DD>
 *         Spaces separated allowed output mime-types list.
 *     </DD>
 * <DT><CODE>converters.external.NAME.inputswitch.TYPE</CODE></DT>
 *     <DD>
 *         <EM>Optional</EM> switch prepended to input filename.
 *         The <CODE>TYPE</CODE> part is optional, it can be set to
 *         the mime-type base (audio, video etc.) or a full
 *         mime-type identifier.
 *     </DD>
 * <DT><CODE>converters.external.NAME.outputswitch.TYPE</CODE></DT>
 *     <DD>
 *         <EM>Optional</EM> switch prepended to output filename.
 *         The <CODE>TYPE</CODE> part is optional, it can be set to
 *         the mime-type base (audio, video etc.) or a full
 *         mime-type identifier.
 *     </DD>
 * <DT><CODE>converters.external.NAME.switch.string.PARAM</CODE></DT>
 *     <DD>
 *         Defines a switch translation.  The given
 *         <CODE>PARAM</CODE> will be substituted by the set value
 *         to build a commandline.
 *     </DD>
 * <DT><CODE>converters.external.NAME.switch.number.PARAM</CODE></DT>
 *     <DD>
 *         Defines a switch translation.  The given
 *         <CODE>PARAM</CODE> will be substituted by the set value
 *         to build a commandline.
 *     </DD>
 * <DT><CODE>converters.external.NAME.switch.boolean.PARAM</CODE></DT>
 *     <DD>
 *         Defines a switch translation.  The given
 *         <CODE>PARAM</CODE> will be substituted by the set value
 *         to build a commandline.  The request parameter value will
 *         be discarded.
 *     </DD>
 * </DL>
 * @see org.scilla.Config
 * @author R.W. van 't Veer
 * @version $Revision: 1.2 $
 */
public class ExternalConverter implements Converter
{
    static final Logger log = LoggerFactory.getLogger(ExternalConverter.class);
    static final Config config = ConfigFactory.get();

    /** config key prefix for converter definitions */
    public static final String CONVERTER_PREFIX = "converters.external";
    static final char keyDelimiter = '.';

    Request request = null;
    String outputFile = null;
    String converterName = null;

    volatile String errorMessage = null;
    volatile int exitValue = 0; // 0 means success
    volatile boolean finished = false;
    volatile boolean started = false;

    static Vector reservedParameters = new Vector();
    static
    {
	reservedParameters.add("outputtype");
	reservedParameters.add("cache");
    }
    static Hashtable inputTypeMap = new Hashtable();
    static Hashtable outputTypeMap = new Hashtable();
    static Hashtable parameterMap = new Hashtable();
    static Hashtable execMap = new Hashtable();
    static Hashtable formatMap = new Hashtable();
    static Hashtable ignoreExitstatMap = new Hashtable();
    static Hashtable inputSwitchMap = new Hashtable();
    static Hashtable outputSwitchMap = new Hashtable();
    static Hashtable silentSwitchMap = new Hashtable();
    static Hashtable stringSwitchMap = new Hashtable();
    static Hashtable numberSwitchMap = new Hashtable();
    static Hashtable booleanSwitchMap = new Hashtable();
    static HashSet blacklistSet = new HashSet();
    static
    {
	Enumeration keys = config.keys();
	while (keys.hasMoreElements())
	{
	    String key = (String) keys.nextElement();
	    if (key.startsWith(CONVERTER_PREFIX))
	    {
		String tail = key.substring(CONVERTER_PREFIX.length()+1);
		StringTokenizer keyst = new StringTokenizer(tail, ""+keyDelimiter);
		String name = keyst.nextToken();
		String type = keyst.nextToken();
		if (type.equals("exec"))
		{
		    final String val = config.getString(key);
		    execMap.put(name, val);
		    // blacklist it if executable does not exist
		    if (! (new File(val)).exists())
		    {
			blacklistSet.add(name);
		    }
		}
		else if (type.equals("format"))
		{
		    formatMap.put(name, config.getString(key));
		}
		else if (type.equals("ignore_exitstat"))
		{
		    ignoreExitstatMap.put(name, new Boolean(config.getBoolean(key)));
		}
		else if (type.equals("silent_switch"))
		{
		    silentSwitchMap.put(name, config.getString(key));
		}
		else if (type.equals("inputtypes"))
		{
		    StringTokenizer st = new StringTokenizer(config.getString(key));
		    while (st.hasMoreTokens())
		    {
			String mtype = st.nextToken();
			Set s = (Set) inputTypeMap.get(mtype);
			if (s == null)
			{
			    s = new HashSet();
			    inputTypeMap.put(mtype, s);
			}
			s.add(name);
		    }
		}
		else if (type.equals("outputtypes"))
		{
		    StringTokenizer st = new StringTokenizer(config.getString(key));
		    while (st.hasMoreTokens())
		    {
			String mtype = st.nextToken();
			Set s = (Set) outputTypeMap.get(mtype);
			if (s == null)
			{
			    s = new HashSet();
			    outputTypeMap.put(mtype, s);
			}
			s.add(name);
		    }
		}
		else if (type.equals("inputswitch"))
		{
		    if (keyst.hasMoreTokens())
		    {
			String mtype = keyst.nextToken();
			inputSwitchMap.put(name+keyDelimiter+mtype, config.getString(key));
		    }
		    else
		    {
			inputSwitchMap.put(name, config.getString(key));
		    }
		}
		else if (type.equals("outputswitch"))
		{
		    if (keyst.hasMoreTokens())
		    {
			String mtype = keyst.nextToken();
			outputSwitchMap.put(name+keyDelimiter+mtype, config.getString(key));
		    }
		    else
		    {
			outputSwitchMap.put(name, config.getString(key));
		    }
		}
		else if (type.equals("switch"))
		{
		    String stype = keyst.nextToken();
		    String sname = keyst.nextToken();

		    // for can convert
		    Set s = (Set) parameterMap.get(sname);
		    if (s == null)
		    {
			s = new HashSet();
			parameterMap.put(sname, s);
		    }
		    s.add(name);

		    // for commandline building
		    if (stype.equals("string"))
		    {
			Hashtable h = (Hashtable) stringSwitchMap.get(name);
			if (h == null)
			{
			    h = new Hashtable();
			    stringSwitchMap.put(name, h);
			}
			h.put(sname, config.getString(key));
		    }
		    else if (stype.equals("number"))
		    {
			Hashtable h = (Hashtable) numberSwitchMap.get(name);
			if (h == null)
			{
			    h = new Hashtable();
			    numberSwitchMap.put(name, h);
			}
			h.put(sname, config.getString(key));
		    }
		    else if (stype.equals("boolean"))
		    {
			Hashtable h = (Hashtable) booleanSwitchMap.get(name);
			if (h == null)
			{
			    h = new Hashtable();
			    booleanSwitchMap.put(name, h);
			}
			h.put(sname, config.getString(key));
		    }
		}
	    }
	}

	if (blacklistSet.size() > 0)
	{
	    log.error("excutable does not exist for: "+blacklistSet);
	}
    }

    public void convert ()
    {
	started = true;
	_convert();
	finished = true;
    }

    private void _convert ()
    {
	// build commandline
	Vector cmdline = new Vector();
	cmdline.add(execMap.get(converterName));

	// handle commandline format
	final String format = (String) formatMap.get(converterName);
	final int formatLen = format.length();
	for (int i = 0; i < formatLen; i++)
	{
	    switch (format.charAt(i))
	    {
		case 'i':
		    // add input file, optionally with switch
		    if (inputSwitchMap.containsKey(converterName))
		    {
			cmdline.add(inputSwitchMap.get(converterName));
		    }
		    else
		    {
			String mtype = request.getInputType();
			String btype = mtype.substring(0, mtype.indexOf('/'));
			mtype = converterName+keyDelimiter+mtype;
			btype = converterName+keyDelimiter+btype;
			if (inputSwitchMap.containsKey(mtype))
			{
			    cmdline.add(inputSwitchMap.get(mtype));
			}
			else if (inputSwitchMap.containsKey(btype))
			{
			    cmdline.add(inputSwitchMap.get(btype));
			}
		    }
		    cmdline.add(request.getInputFile());
		    break;
		case 'o':
		    // add output file, optionally with switch
		    if (outputSwitchMap.containsKey(converterName))
		    {
			cmdline.add(outputSwitchMap.get(converterName));
		    }
		    else
		    {
			String mtype = request.getOutputType();
			String btype = mtype.substring(0, mtype.indexOf('/'));
			mtype = converterName+keyDelimiter+mtype;
			btype = converterName+keyDelimiter+btype;
			if (outputSwitchMap.containsKey(mtype))
			{
			    cmdline.add(outputSwitchMap.get(mtype));
			}
			else if (outputSwitchMap.containsKey(btype))
			{
			    cmdline.add(outputSwitchMap.get(btype));
			}
		    }
		    cmdline.add(outputFile);
		    break;
		case 's':
		    // add silent switch
		    if (silentSwitchMap.containsKey(converterName))
		    {
			cmdline.add(silentSwitchMap.get(converterName));
		    }

		    // add switches to commandline
		    Hashtable sm = (Hashtable) stringSwitchMap.get(converterName);
		    Hashtable nm = (Hashtable) numberSwitchMap.get(converterName);
		    Hashtable bm = (Hashtable) booleanSwitchMap.get(converterName);
		    Iterator it = request.getParameters().iterator();
		    while (it.hasNext())
		    {
			RequestParameter rp = (RequestParameter) it.next();
			if (reservedParameters.indexOf(rp.key) == -1)
			{
			    // string
			    if (sm != null && sm.containsKey(rp.key))
			    {
				cmdline.add(sm.get(rp.key));
				cmdline.add(rp.val);
			    }
			    // number
			    else if (nm != null && nm.containsKey(rp.key))
			    {
				cmdline.add(nm.get(rp.key));
				cmdline.add(rp.val);
			    }
			    // boolean
			    else if (bm != null && bm.containsKey(rp.key))
			    {
				cmdline.add(bm.get(rp.key));
			    }
			}
		    }
		    break;
		default:
		    // TODO throw exception
		    break;
	    }
	}
	if (log.isDebugEnabled()) log.debug("cmdline: "+cmdline);

	// prepare command
	String[] cmd = (String[]) cmdline.toArray(new String[0]);
	File dir = null;

	// run system command
	QueuedProcess proc = null;
	try
	{
	    proc = new QueuedProcess(cmd, null, dir);
	    // wait for command to finish
	    exitValue = proc.exitValue();
	}
	catch (Exception e)
	{
	    errorMessage = e.getMessage();
	}
	if (proc != null)
	{
	    errorMessage = proc.getErrorLog();
	}
    }

    public boolean canConvert (Request req)
    {
	Set conv = new HashSet();
	Collection c;
	Iterator it;

	// can handle input?
	c = (Collection) inputTypeMap.get(req.getInputType());
	if (c == null) return false;
	conv.addAll(c);
	if (conv.size() == 0) return false;

	// can handle output?
	c = (Collection) inputTypeMap.get(req.getInputType());
	if (c == null) return false;
	conv.retainAll(c);
	if (conv.size() == 0) return false;

	// supports all parameters?
	it = req.getParameterKeys().iterator();
	while (it.hasNext())
	{
	    String key = (String) it.next();
	    if (reservedParameters.indexOf(key) == -1)
	    {
		c = (Collection) parameterMap.get(key);
		if (c == null) return false;
		conv.retainAll(c);
		if (conv.size() == 0) return false;
	    }
	}

	// determine which is "best"
	// TODO add weight parameter
	it = conv.iterator();
	converterName = (String) it.next();

	log.debug("appropriate converter: "+converterName);
	return true;
    }

    public void setRequest (Request req)
    {
	request = req;
    }

    public boolean exitSuccess ()
    {
	if (! finished) throw new IllegalStateException();
	return ignoreExitstatMap.containsKey(converterName)
		|| (exitValue == 0);
    }

    public String getErrorMessage ()
    {
	if (! finished) throw new IllegalStateException();
	return errorMessage;
    }

    public void setOutputFile (String fn)
    {
	if (started) throw new IllegalStateException();
	outputFile = fn;
    }

    public String getOutputFile ()
    {
	return outputFile;
    }

    public boolean hasFinished ()
    {
	return finished;
    }

}
