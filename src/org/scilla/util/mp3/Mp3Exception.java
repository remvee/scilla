package org.scilla.util.mp3;

/**
 * Exception thrown when reading a MP3 attribute failed.
 *
 * @author Remco van 't Veer
 * @version $Revision: 1.2 $
 */
public class Mp3Exception extends Exception
{
    public Mp3Exception (String msg)
    {
	super(msg);
    }
}
