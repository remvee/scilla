import java.io.File;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.scilla.*;
import org.scilla.util.mp3.ID3v1;

public class SServlet extends org.scilla.util.Servlet
{
    public void doGet (HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
	Request req = null;
	try
	{
	    req = RequestFactory.createFromHttpServletRequest(request);

	    long len = req.getLength();
	    if (len != -1) response.setContentLength((int) len);
	    response.setContentType(req.getOutputType());

	    if (req.getOutputType().equals("audio/mpeg")
		    || req.getOutputType().equals("audio/mp3"))
	    {
		try
		{
		    ID3v1 id3 = new ID3v1(new File(req.getInputFile()));
		    String name = "";
		    if (id3.getArtist() != null && id3.getArtist().length() != 0) name += id3.getArtist() + " - ";
		    if (id3.getAlbum() != null && id3.getAlbum().length() != 0) name += id3.getAlbum() + " - ";
		    if (id3.getTitle() != null && id3.getTitle().length() != 0) name += id3.getTitle();
		    if (name.endsWith(" - ")) name = name.substring(0, name.lastIndexOf(" - "));
		    if (name.length() == 0)
		    {
			name = req.getInputFile();
			name = name.substring(name.lastIndexOf(File.separator)+1);
			name = name.substring(0, name.lastIndexOf('.'));
		    }

		    response.setHeader("icy-name", name);
		    response.setHeader("x-audiocast-name", name);
		}
		catch (Throwable t) { /* nop */ }
	    }
	    req.write(response.getOutputStream());
	}
	catch (ScillaNoOutputException ex)
	{
	    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
		    ex.getMessage());
	}
	catch (ScillaConversionFailedException ex)
	{
	    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
		    ex.getMessage());
	}
	catch (ScillaOutputIOException ex)
	{
	    /* probably and broken pipe */
	}
	catch (ScillaNoInputException ex)
	{
	    response.sendError(HttpServletResponse.SC_NOT_FOUND,
		    ex.getMessage());
	}
	catch (ScillaInputIOException ex)
	{
	    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
		    ex.getMessage());
	}
	catch (ScillaNoConverterException ex)
	{
	    response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,
		    ex.getMessage());
	}
	catch (ScillaIllegalRequestException ex)
	{
	    response.sendError(HttpServletResponse.SC_FORBIDDEN,
		    ex.getMessage());
	}
	catch (ScillaException ex)
	{
	    throw new ServletException("Scilla FAILED!", ex);
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}
    }
}
