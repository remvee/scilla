package test;

public class HTMLUtil {
    public static String escape (Object obj) {
	char[] b = (obj+"").toCharArray();
	StringBuffer out = new StringBuffer();
	for (int i = 0; i < b.length; i++) {
	    char c = b[i];
	    switch (c) {
		case '<':
		    out.append("&lt;");
		    break;
		case '>':
		    out.append("&gt;");
		    break;
		case '&':
		    out.append("&amp;");
		    break;
		case '"':
		    out.append("&quot;");
		    break;
		default:
		    out.append(c);
	    }
	}
	return out.toString();
    }
}
