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

package org.scilla.util;

import java.io.*;

import org.scilla.*;

/**
 * Simple main class for debugging scilla.
 *
 * @version $Revision: 1.3 $
 * @author R.W. van 't Veer
 */
public class Publish {
    /**
     * Writes requested data to standart output.
     * @param args conversion parameters
     * @see org.scilla.RequestFactory#createFromArgv(String[])
     */
    public static void main (String[] args)
    throws Exception {
        Request req = RequestFactory.createFromArgv(args);
        System.err.println("outputType='"+req.getOutputType()+"'");

        InputStream in = req.getStream();
        OutputStream out = System.out;
        int n;
        byte[] b = new byte[4096];
        while ((n = in.read(b)) != -1) {
            out.write(b, 0, n);
	}
        in.close();
        out.close();
    }
}
