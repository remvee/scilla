/*
 * scilla
 *
 * Copyright (C) 2003  R.W. van 't Veer
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

import java.io.IOException;
import java.util.*;

/**
 * Tiff header/ Image File Directory (IFD) reader.
 *
 * @version $Revision: 1.1 $
 * @author R.W. van 't Veer
 */
public class TiffHeader {
    private boolean littleEndian;
    private byte[] data;
    private List fields;

    /**
     * Read directory from at given position of given byte order.
     * @param data byte array containing tiff header
     * @param pos position at which directory starts
     * @param littleEndian <tt>true</tt> when byte order is little endian otherwise big endian
     */
    public TiffHeader (byte[] data, int pos, boolean littleEndian) {
        this.data = data;
        this.littleEndian = littleEndian;
        this.fields = new ArrayList();
        
        readIfds(pos);
    }

    /**
     * Read TIFF header and root directory.
     * @param data byte array containg tiff header
     * @throws IOException when byte order can not be determined
     */    
    public TiffHeader (byte[] data)
    throws IOException {
        this.data = data;
        fields = new ArrayList();
        
        processByteOrder();
	readIfds(read4ByteInt(data, 4));
    }

    /**
     * @return list of directory entries
     */
    public List getFields () {
        return fields;
    }
    
    /**
     * @return <tt>true</tt> when byte order is little endian otherwise big endian
     */
    public boolean isLittleEndian () {
        return littleEndian;
    }

    /**
     * Image file directory field.
     */
    public class Field {
        private int tag;
        private int type;
        private int count;
        private int voffset;
        private Object val;
        
        /**
         * Construct new directory field.
         * @param data byte array to read from
         * @param pos position to start reading
         */
        public Field (byte[] data, int pos) {
            tag = read2ByteInt(data, pos);
            pos += 2;
            type = read2ByteInt(data, pos);
            pos += 2;
            count = read4ByteInt(data, pos);
            pos += 4;
            voffset = read4ByteInt(data, pos);
    
            // read value
            val = null;
            switch (type) {
                case 1: // BYTE
                case 6: // SIGNED BYTE
                    if (count == 1) {
                        short v = (short) (voffset & 0xff);
                        val = new Short(isSigned(type) ? signedByte(v) : v);
                    } else {
                        int p = count > 4 ? voffset : pos;
                        byte[] d = new byte[count];
                        for (int i = 0; i < count; i++) {
                            d[i] = data[p++];
                        }
                        val = d;
                    }
                    break;
                case 2: // ASCII
                    {
                        int i = count > 4 ? voffset : pos;
                        StringBuffer sb = new StringBuffer();
                        for (; data[i] != 0; i++) {
                            sb.append((char) data[i]);
                        }
                        val = sb.toString();
                    }
                    break;
                case 3: // SHORT 16-bit unsigned
                case 8: // SSHORT 16-bit signed
                    if (count == 1) {
                        int v = voffset & 0xffff;
                        val = new Integer(isSigned(type) ? signedShort(v) : v);
                    } else {
                        int p = count > 2 ? voffset : pos;
                        int[] d = new int[count];
                        for (int i = 0; i < count; i++) {
                            int v = read2ByteInt(data, p);
                            d[i] = isSigned(type) ? signedShort(v) : v;
                            p += 2;
                        }
                        val = d;
                    }
                    break;
                case 4: // LONG 32-bit unsigned
                case 9: // SLONG 32-bit signed
                    if (count == 1) {
                        long v = voffset & 0xffffffff;
                        val = new Long(isSigned(type) ? signedLong(v) : v);
                    } else {
                        int p = count > 1 ? voffset : pos;
                        long[] d = new long[count];
                        for (int i = 0; i < count; i++) {
                            long v = read4ByteInt(data, p);
                            d[i] = isSigned(type) ? signedLong(v) : v;
                            p += 4;
                        }
                        val = d;
                    }
                    break;
                case 5: // RATIONAL
                case 10: // SRATIONAL
                    if (count == 1) {
                        long numerator = (long) read4ByteInt(data, voffset);
                        long denominator = (long) read4ByteInt(data, voffset + 4);
                        if (isSigned(type)) {
                            numerator = signedLong(numerator);
                            denominator = signedLong(denominator);
                        }
                        val = new Rational(numerator, denominator);
                    }
                    break;
                case 7: // UNDEF
                    break;
                case 11: // FLOAT
                    // TODO IMPLEMENT FLOAT
                    break;
                case 12: // DOUBLE
                    // TODO IMPLEMENT DOUBLE
                    break;
                default: // UNKNOWN
            }
        }

        /**
         * @return field identifier
         */        
        public int getTag() {
            return tag;
        }

        /**
         * @return field type
         */
        public int getType() {
            return type;
        }

        /**
         * @return number of values
         */
        public int getCount() {
            return count;
        }

        /**
         * @return field value
         */
        public Object getValue() {
            return val;
        }

        /**
         * @return value offset from start of tiff header
         */
        public int getOffset() {
            return voffset;
        }
    }

    public static class Rational {
        private long numerator;
        private long denominator;
    
        public Rational (long n, long d) {
            numerator = n;
            denominator = d;
        }
    
        public long getNumerator () {
            return numerator;
        }
    
        public long getDenominator () {
            return denominator;
        }
    
        public double getDouble () {
            return (double) numerator / (double) denominator;
        }
    
        public String toString () {
            return numerator + "/" + denominator;
        }
    }

    /**
     * Read start of tiff header to determine byte order.
     * @throws IOException when byte order cannot be determined
     */
    private void processByteOrder ()
    throws IOException {
        // determine byte order
        {
            int c1 = read1ByteInt(data, 0);
            int c2 = read1ByteInt(data, 1);
            if (c1 == 'I' && c2 == 'I') {
                littleEndian = true;
            } else if (c1 == 'M' && c2 == 'M') {
                littleEndian = false;
            } else {
                throw new IOException("not a tiff; missing II or MM");
            }
        }
        
        // verify endianess
        if (read2ByteInt(data, 2) != 42) {
            throw new IOException("not a tiff; byte order broken");
        }
    }
    
    private int read1ByteInt (byte[] data, int pos) {
	return data[pos] & 0xff;
    }

    private int read2ByteInt (byte[] data, int pos) {
	int n1 = data[pos++] & 0xff;
	int n2 = data[pos] & 0xff;

	return littleEndian
		? ((n2 << 8) | n1)
		: ((n1 << 8) | n2);
    }

    private int read4ByteInt (byte[] data, int pos) {
	int n1 = data[pos++] & 0xff;
	int n2 = data[pos++] & 0xff;
	int n3 = data[pos++] & 0xff;
	int n4 = data[pos] & 0xff;

	return littleEndian
		? ((n4 << 24) | (n3 << 16) | (n2 << 8) | n1)
		: ((n1 << 24) | (n2 << 16) | (n3 << 8) | n4);
    }

    private void readIfds(int pos) {
        int p = pos;
	while (p != 0) {
	    int num = read2ByteInt(data, p);
	    p += 2;

	    for (int i = 0; i < num; i++, p += 12) {
		fields.add(new Field(data, p));
	    }

	    p = read4ByteInt(data, p);
	}
    }

    private static boolean isSigned (int type) {
	switch (type) {
	    case 6: case 8: case 9: case 10:
		return true;
            default:
                return false;
	}
    }

    private static long signedLong (long v) {
	return (v & 0x80000000L) != 0 ?  v | 0xffffffff00000000L : v;
    }

    private static int signedShort (int v) {
	return (v & 0x8000) != 0 ?  v | 0xffff0000 : v;
    }

    private static short signedByte (short v) {
	return (v & 0x80) != 0 ? (short) (v | 0xff00) : v;
    }

}
