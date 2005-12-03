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

import org.scilla.*;

/**
 * A file object is a media object somewhere on disk.
 *
 * @author R.W. van 't Veer
 * @version $Revision: 1.10 $
 */
public class FileObject implements MediaObject {
    static final int BUFFER_SIZE = 4096;

    String filename;
    FileInputStream in;

    /**
     * Create media object.
     * @param filename full name of result file
     */
    public FileObject (String filename)
    throws ScillaException {
        this.filename = filename;
        try {
            in = new FileInputStream(filename);
        } catch (IOException ex) {
            throw new ScillaInputIOException(ex);
        }
    }

    public InputStream getStream ()
    throws ScillaException {
        return in;
    }

    /**
     * @return file length
     */
    public long getLength () {
        return (new File(filename)).length();
    }

    /**
     * @return object filename
     */
    public String getFilename () {
        return filename;
    }
}
