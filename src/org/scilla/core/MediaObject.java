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

import java.io.OutputStream;

import org.scilla.*;

/**
 * Interface to a media object.
 *
 * @author R.W. van 't Veer
 * @version $Id: MediaObject.java,v 1.2 2001/09/21 12:38:27 remco Exp $
 */
public interface MediaObject
{
    /**
     * Write data to stream.
     * @param out stream to write to
     * @throws ScillaException when a read or write problem occures
     */
    public void write (OutputStream out) throws ScillaException;

    /**
     * @return true if this object may be cached
     */
    boolean allowCaching ();
}
