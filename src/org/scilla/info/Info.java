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

package org.scilla.info;

import java.util.Map;
import java.util.HashMap;

/**
 * The scilla media info base class.
 *
 * @version $Revision: 1.2 $
 * @author R.W. van 't Veer
 */
public class Info {
    /** map to hold property values */
    private Map infoMap = new HashMap();

    /** general property to denote the media encoding specs */
    public final static String CODEC = "codec";

// read methods
    /**
     * @return string info value or <tt>null</tt> when property not available
     */
    public String getString (String key) {
	return (String) infoMap.get(key);
    }
    /**
     * @return integer info value or <tt>-1</tt> when property not available
     */
    public int getInt (String key) {
	Integer i = (Integer) infoMap.get(key);
	return i == null ? -1 : i.intValue();
    }
    /**
     * @return boolean info value or <tt>false</tt> when property not available
     */
    public boolean getBoolean (String key) {
	Boolean b = (Boolean) infoMap.get(key);
	return b == null ? false : b.booleanValue();
    }

// set methods for extending classes
    /**
     * String setter method for extending classes.
     * @param key property name
     * @param val property value
     */
    protected void setString (String key, String val) {
	infoMap.put(key, val);
    }
    /**
     * Integer setter method for extending classes.
     * @param key property name
     * @param val property value
     */
    protected void setInt (String key, int val) {
	infoMap.put(key, new Integer(val));
    }
    /**
     * Boolean setter method for extending classes.
     * @param key property name
     * @param val property value
     */
    protected void setBoolean (String key, boolean val) {
	infoMap.put(key, new Boolean(val));
    }

// other stuff
    /**
     * Debugging..
     */
    public String toString () {
	return infoMap.toString();
    }
}
