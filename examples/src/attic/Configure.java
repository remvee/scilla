import java.io.*;
import java.util.*;
import java.util.jar.*;

/*
    - executable jar
	- META-INF/MANIFEST.MF: making it execute Configure.class
	- Confugure.class
	- scilla.war
	- data.jar
    - Configure
	- get all init-param's from web.xml
	- confirm all values; descriptions, default values from
	  param-value (except source.dir which is cwd), empty
	  value illegal
	- test/create source.dir
	- copy data files to source.dir
	- write war and replace web.xml
*/
public class Configure
{
    public static void main (String[] args)
    throws Exception
    {
	if (args.length == 2)
	{
	    Configure thiz = new Configure();
	    thiz.source = new File(args[0]).getAbsolutePath();
	    thiz.cache = new File(args[1]).getAbsolutePath();
	    thiz.process();
	}
	else
	{
	    System.out.println("expect 2 arguments; source and cache directory location");
	}
    }

    String source = null;
    String cache = null;

    void process ()
    throws Exception
    {
	final ClassLoader cl = this.getClass().getClassLoader();

	// collection configuration parameters
	String webStr = null;
	{
	    System.out.println("Reading scilla configuration.");

	    // load war file and find web.xml
	    JarInputStream in = new JarInputStream(cl.getResourceAsStream("scilla.war"));
	    JarEntry e = null;
	    while ((e = in.getNextJarEntry()) != null)
	    {
		if ("WEB-INF/web.xml".equals(e.getName()))
		{
		    webStr = loadString(in);
		    break;
		}
	    }
	    in.close();
	}

	// set parameters
	if (webStr != null)
	{
	    System.out.println("Changing scilla configuration.");

	    System.out.println("  source="+source);
	    webStr = replace(webStr, "@source.dir@", source);

	    System.out.println("  cache="+cache);
	    webStr = replace(webStr, "@cache.dir@", cache);
	}
	else
	{
	    System.err.println("can't find web.xml entry..");
	    return;
	}

	// copy war file to file system
	{
	    System.out.println("Writing war-file.");

	    String outfn = "scilla.war";
	    System.out.println("  "+outfn);

	    JarInputStream in = new JarInputStream(cl.getResourceAsStream("scilla.war"));
	    JarOutputStream out = new JarOutputStream(new FileOutputStream(outfn), in.getManifest());
	    JarEntry e = null;
	    while ((e = in.getNextJarEntry()) != null)
	    {

		if ("WEB-INF/web.xml".equals(e.getName()))
		{
		    e.setTime(System.currentTimeMillis());
		    out.putNextEntry(e);

		    // write new web.xml file
		    out.write(webStr.getBytes());
		}
		else
		{
		    out.putNextEntry(e);

		    // copy file
		    while (in.available() > 0)
		    {
			byte[] data = new byte[1024];
			int len = in.read(data, 0, 1024);
			if (len != -1) out.write(data, 0, len);
		    }
		}
		in.closeEntry();
	    }
	    in.close();
	    out.close();
	}

	// copy all data files to source directory
	{
	    System.out.println("Preparing source directory.");

	    JarInputStream in = new JarInputStream(cl.getResourceAsStream("data.jar"));
	    JarEntry e = null;
	    while ((e = in.getNextJarEntry()) != null)
	    {
		String fn = source+File.separator+e.getName();
		File dir = (new File(fn)).getParentFile();
		if (! dir.isDirectory())
		{
		    System.out.println("  creating dir: "+dir);
		    dir.mkdirs();
		}

		System.out.println("  creating file: "+fn);
		File f = new File (fn);
		FileOutputStream out = new FileOutputStream(f);
		while (in.available() > 0)
		{
		    byte[] data = new byte[1024];
		    int len = in.read(data, 0, 1024);
		    if (len != -1) out.write(data, 0, len);
		}
	    }
	}
    }

    private static String replace (String source, String orig, String dest)
    {
	int i;
	String result = source;
	while ((i = result.indexOf(orig)) != -1)
	{
	    String head = result.substring(0, i);
	    String tail = result.substring(i+orig.length());
	    result = head + dest + tail;
	}
	return result;
    }

    private static String loadString (InputStream in)
    throws IOException
    {
	StringBuffer sb = new StringBuffer();
	while (in.available() > 0)
	{
	    byte[] data = new byte[1024];
	    int len = in.read(data, 0, 1024);
	    if (len != -1) sb.append(new String(data, 0, len));
	}
	return sb.toString();
    }
}
