package org.scilla.info;

/**
 * @author remco
 */
public class TiffException extends Exception {
    public TiffException (String msg) {
        super(msg);
    }
    public TiffException (String msg, Throwable ex) {
        super(msg, ex);
    }
}
