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

import java.io.IOException;

/**
 * Output IO exception wrapper.
 *
 * @author R.W. van 't Veer
 * @version $Id: ScillaOutputIOException.java,v 1.2 2001/09/21 12:38:27 remco Exp $
 */
public class ScillaOutputIOException extends ScillaException
{
    public ScillaOutputIOException (IOException ex)
    {
	cause = ex;
	msg = "problem writing stream: " + ex.getMessage();
    }
}
