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

	    response.setContentType(req.getOutputType());
	    if (req.getOutputType().equals("audio/mpeg")
		    || req.getOutputType().equals("audio/mp3"))
	    {
		try
		{
		    ID3v1 id3 = new ID3v1(new File(req.getInputFile()));
		    String name = id3.getArtist()+" - "+id3.getAlbum()+" - "+id3.getTitle();
		    response.setHeader("icy-name", name);
		    response.setHeader("x-audiocast-name", name);
		}
		catch (Throwable t) { /* nop */ }
	    }
	    req.write(response.getOutputStream());
	}
	catch (ScillaNoOutputException ex)
	{
	    response.sendError(
		    HttpServletResponse.SC_INTERNAL_SERVER_ERROR, req.toHTML());
	}
	catch (ScillaOutputIOException ex)
	{
	    /* probably and broken pipe */
	}
	catch (ScillaNoInputException ex)
	{
	    response.sendError(
		    HttpServletResponse.SC_NOT_FOUND, req.toHTML());
	}
	catch (ScillaInputIOException ex)
	{
	    response.sendError(
		    HttpServletResponse.SC_INTERNAL_SERVER_ERROR, req.toHTML());
	}
	catch (ScillaNoConverterException ex)
	{
	    response.sendError(
		    HttpServletResponse.SC_NOT_IMPLEMENTED, req.toHTML());
	}
	catch (ScillaIllegalRequestException ex)
	{
	    response.sendError(
		    HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
	}
	catch (ScillaException ex)
	{
	    throw new ServletException("Scilla FAILED!", ex);
	}
    }
}
