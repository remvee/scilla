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

/**
 * Converter interface.
 *
 * @author R.W. van 't Veer
 * @version $Revision: 1.8 $
 */
public interface Converter
{
    /**
     * start conversion
     */
    public void convert ();

    /**
     * @return true if converter exit status is successfull
     */
    public boolean exitSuccess ();

    /**
     * @return error message
     */
    public String getErrorMessage ();

    /**
     * @return output filename
     */
    public String getOutputFile ();

    /**
     * @param fn output filename
     */
    public void setOutputFile (String fn);

    /**
     * @return true if converter has finished
     */
    public boolean hasFinished ();

    /**
     * Determine if converter can handle request.
     * @param req request to be handled
     * @return true if converter can handle this quest
     */
    public boolean canConvert (Request req);

    /**
     * Set convertion details.
     * @param req request to be handled
     */
    public void setRequest (Request req);
}
