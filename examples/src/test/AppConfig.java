package test;

import javax.naming.*;

/**
 * Simple configuration class for scilla example web application.
 */
public class AppConfig {
    /**
     * @return directory name of media files from env-entry
     * @throws NamingException when <tt>media/source</tt> entry
     * not available in <tt>java:comp/env</tt> context.
     */
    public static synchronized String getSourceDir ()
    throws NamingException {
	if (sourceDir == null) {
	    Context ctx = new InitialContext();
	    Context env = (Context) ctx.lookup("java:comp/env");
	    sourceDir = (String) env.lookup("example/source/directory");
	}
	return sourceDir;
    }
    private static String sourceDir = null;
}
