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
import java.util.Enumeration;
import java.util.Vector;

import org.scilla.*;
import org.scilla.core.*;

public abstract class Converter extends Thread
{
    static final String TEMP_PREFIX = "scilla";

    public String[] inputTypeList = null;
    public String[] outputTypeList = null;
    public String[] parameterList = null;

    String outputFile;
    String inputFile;
    String outputType;
    String inputType;
    Vector pars;

    static final int CONFIGURED_STATE = 0;
    static final int RUNNING_STATE = 1;
    static final int FINISHED_STATE = 2;

    int state = CONFIGURED_STATE;

    public Converter ()
    {
	// generate "save" temp filename: tmpdir/scillaHASHCODE
	StringBuffer b = new StringBuffer();
	b.append(System.getProperty("java.io.tmpdir"));
	b.append(File.separator);
	b.append(TEMP_PREFIX);
	int h = this.hashCode();
	// avoid - sign
	b.append("" + (h < 0 ? "0" : "1") + (h < 0 ? -h : h));
	outputFile = b.toString();
    }

    public void run ()
    {
	state = RUNNING_STATE;
	convert();
	state = FINISHED_STATE;
    }

    /** the conversion operation.  This method will be called by the
     * run() method.
     * @see #run()
     */
    public abstract void convert ();

    /** Test if convert is functional.  This method tries to determine
     * if the converter is able to run by testing the existence of
     * classes or executables it relies on.
     */
    public abstract boolean isFunctional ();

    public boolean hasFinished ()
    {
	return state == FINISHED_STATE;
    }

    public boolean isValidInputType (String type)
    {
	for (int i = 0; i < inputTypeList.length; i++)
	{
	    if (type.equals(inputTypeList[i])) return true;
	}
	return false;
    }

    public boolean isValidOutputType (String type)
    {
	for (int i = 0; i < outputTypeList.length; i++)
	{
	    if (type.equals(outputTypeList[i])) return true;
	}
	return false;
    }

    public boolean hasParameter (String name)
    {
	for (int i = 0; i < parameterList.length; i++)
	{
	    if (name.equals(parameterList[i])) return true;
	}
	return false;
    }

    public void setParameters (Vector p) { pars = p; }
    public Vector getParameters () { return pars; }
    public void setOutputFile (String f) { outputFile = f; }
    public String getOutputFile () { return outputFile; }
    public void setInputFile (String f) { inputFile = f; }
    public String getInputFile () { return inputFile; }
    public void setOutputType (String f) { outputType = f; }
    public String getOutputType () { return outputType; }
    public void setInputType (String f) { inputType = f; }
    public String getInputType () { return inputType; }
}
