import java.io.*;
import java.util.*;

import org.apache.regexp.*;

public class MXML
{
    InputStream in;

    class Tag
    {
	String raw;
	String tagName;
	Map attrs = new HashMap();
	int type;

	final static int OPEN = 0;
	final static int CLOSE = 1;
	final static int EMPTY = 2;
	final static int SPECIAL = 3;
	final static int DECLARATION = 4;

	/**
	 * The &lt; character was already read.
	 */
	Tag (InputStream in)
	throws IOException, RESyntaxException
	{
	    // try to read full tag
	    StringBuffer b = new StringBuffer();
	    int c;
	    while ((c = in.read()) != -1)
	    {
		// TODO allow > to be in attribute value
		if (c == '>') break;
		b.append((char) c);
	    }
	    // TODO handle if c == -1
	    raw = b.toString();

	    // determine tag type
	    {
		RE closeRe = new RE("^\\s*/");
		RE emptyRe = new RE("/\\s*$");
		RE specialRe = new RE("^\\s*\\?");
		RE declRe = new RE("^\\s*\\!");

		if (closeRe.match(raw)) type = CLOSE;
		else if (emptyRe.match(raw)) type = EMPTY;
		else if (specialRe.match(raw)) type = SPECIAL; // TODO handle it!!
		else if (declRe.match(raw)) type = DECLARATION; // TODO handle it!!
		else type = OPEN;
	    }

	    // determine tag name
	    {
		RE nameRe = new RE("([a-zA-Z0-9:]+)\\s*");

		nameRe.match(raw);
		tagName = nameRe.getParen(1);
	    }

	    // collect attributes
	    {
		RE attRe = new RE("\\s([a-zA-Z0-9]+)\\s*=");
		RE valRe1 = new RE("^\\s*=\\s*'([^']*)'");
		RE valRe2 = new RE("^\\s*=\\s*\"([^\"]*)\"");
		RE valRe3 = new RE("^\\s*=\\s*([^\\s]*)");

		int pos = 0;
		while (attRe.match(raw, pos))
		{
		    String attr = attRe.getParen(1);
		    pos = attRe.getParenEnd(1);
		    String valStr = raw.substring(pos);

		    String val = null;
		    RE valRe = null;
		    if (valRe1.match(valStr)) valRe = valRe1;
		    else if (valRe2.match(valStr)) valRe = valRe2;
		    else if (valRe3.match(valStr)) valRe = valRe3;
		    else break;
		    val = valRe.getParen(1); // TODO handle entity and char refs
		    pos += valRe.getParenEnd(1);

		    attrs.put(attr, val);
		}
	    }
	}

	public String toString ()
	{
	    StringBuffer sb = new StringBuffer();

	    sb.append('<');

	    if (type == CLOSE)
	    {
		sb.append('/');
		sb.append(tagName);
		sb.append('>');

		return sb.toString();
	    }
	    else if (type == SPECIAL)
	    {
		sb.append(raw);
		sb.append('>');

		return sb.toString();
	    }

	    sb.append(tagName);

	    Iterator it = attrs.keySet().iterator();
	    while (it.hasNext())
	    {
		String attr = (String) it.next();
		String val = (String) attrs.get(attr);

		sb.append(' ');
		sb.append(attr);
		sb.append('=');
		sb.append('"');  // TODO single or double quoting..
		sb.append(val);
		sb.append('"');
	    }

	    if (type == EMPTY) sb.append('/');
	    sb.append('>');

	    return sb.toString();
	}
    }

    class Text
    {
	String txt;

	Text (String txt)
	{
	    // TODO fix illegal characters
	    this.txt = txt;
	}

	public String toString ()
	{
	    return txt;
	}
    }

    public MXML (InputStream in)
    {
	this.in = in;
    }
    public MXML (File f)
    throws IOException
    {
	this(new FileInputStream(f));
    }

    public List tokenize ()
    throws IOException, RESyntaxException
    {
	List v = new Vector();
	StringBuffer b = new StringBuffer();
	int c;
	while ((c = in.read()) != -1)
	{
	    switch (c)
	    {
		case '<':
		    v.add(new Text(b.toString()));
		    v.add(new Tag(in));
		    b = new StringBuffer();
		    break;
		default:
		    b.append((char) c);
	    }
	}
	return v;
    }

    public static void main (String[] args)
    throws Exception
    {
	MXML mxml = new MXML(new File(args[0]));
	System.out.println(mxml.tokenize());
    }
}
