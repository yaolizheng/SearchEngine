import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * The base servlet class, extended by the other servlet classes in this
 * example.
 * 
 * @author Yaoli Zheng
 *  
 */
@SuppressWarnings("serial")
public class BaseServlet extends HttpServlet 
{
	protected static Logger log = Logger.getLogger(Driver.class);
	
	protected static InvertedIndex invertedIndex = InvertedIndex.getInstance();
	
	protected static DatabaseHandler db = DatabaseHandler.getInstance();

	/**
	 * Gets the cookies associated with the HTTP request, and returns the 
	 * cookies as a map of (name, value) pairs.
	 * 
	 * @param request HTTP request
	 * @return map of cookies as (name, value) pairs
	 */
	protected Map<String, String> getCookieMap(HttpServletRequest request)
	{
		HashMap<String, String> map = new HashMap<String, String>();
		Cookie[] cookies = request.getCookies();
		
		if(cookies != null)
			for(Cookie cookie : cookies)
				map.put(cookie.getName(), cookie.getValue());
		
		return map;
	}
	
	/**
	 * Retrieves the cookies associated with the HTTP request, and 
	 * indicates they should be deleted in the HTTP response.
	 * 
	 * @param request HTTP request
	 * @param response HTTP response
	 */
	protected void eraseCookies(HttpServletRequest request, HttpServletResponse response)
	{
		Cookie[] cookies = request.getCookies();

		for(Cookie cookie : cookies)
		{
			cookie.setMaxAge(0);
			response.addCookie(cookie);
		}
	}
	
	/**
	 * Given the name, returns the error message. Helps
	 * simplify error handling.
	 * 
	 * @param errorName name from enum
	 * @return error message from enum
	 */
	protected String getStatusMessage(String errorName)
	{
		Status status = null;
		
		try
		{
			// try to get associated status enum if possible
			status = Status.valueOf(errorName);
		}
		catch(IllegalArgumentException ex)
		{
			// if name is not one of the status enums,
			// return generic error type
			log.warn("Could not determine Status enum type.", ex);
			status = Status.ERROR;
		}
		catch(NullPointerException ex)
		{
			// if name is not provided,
			// return generic error type
			log.warn("No Status name provided.", ex);
			status = Status.ERROR;
		}
		
		// return String representation of Status enum
		return status.toString();
	}

	/**
	 * Prepares the HTTP response with the provided title.
	 * 
	 * @param title web page title
	 * @param response HTTP response
	 */
	protected void prepareResponse(String title, HttpServletResponse response)
	{
		try
		{
			PrintWriter out = response.getWriter();

			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/html");
			response.setCharacterEncoding("utf-8");

			out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n");
			out.println("<html>");
			out.println("");
			out.println("<head>");
			out.println("\t<title>" + title + "</title>" );
			out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"css/style1.css\">");
			out.println("</head>");
			out.println("");
			out.println("<body>");
			out.println("");
		} 
		catch(IOException ex)
		{
			log.warn("Exception encountered preparing response.", ex);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}		
	}
	
	/**
	 * Prepares the HTTP response with the provided title and theme setting.
	 * 
	 * @param title web page title
	 * @param response HTTP response
	 * @param theme	the number of theme
	 */
	protected void prepareResponseWithTheme(String title, HttpServletResponse response, String theme)
	{
		try
		{
			PrintWriter out = response.getWriter();

			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/html");
			response.setCharacterEncoding("utf-8");

			out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n");
			out.println("<html>");
			out.println("");
			out.println("<head>");
			out.println("\t<title>" + title + "</title>" );
			out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"css/style" + theme + ".css\">");
			out.println("</head>");
			out.println("");
			out.println("<body>");
			out.println("");
		} 
		catch(IOException ex)
		{
			log.warn("Exception encountered preparing response.", ex);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}		
	}
	
	/**
	 * Prepares the HTTP response with the provided title and js.
	 * 
	 * @param title web page title
	 * @param response HTTP response
	 * @param js	javascripts clip
	 */
	protected void prepareResponseWithJS(String title, HttpServletResponse response, String js)
	{
		try
		{
			PrintWriter out = response.getWriter();

			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/html");
			response.setCharacterEncoding("utf-8");

			out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n");
			out.println("<html>");
			out.println("");
			out.println("<head>");
			out.println("\t<title>" + title + "</title>" );
			out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"css/style.css\">");
			out.println(js);
			out.println("</head>");
			out.println("");
			out.println("<body>");
			out.println("");
		} 
		catch(IOException ex)
		{
			log.warn("Exception encountered preparing response.", ex);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}		
	}
	
	/**
	 * Finishes the HTTP response.
	 * 
	 * @param response HTTP response
	 */
	protected void finishResponse(HttpServletResponse response)
	{
		try
		{
			PrintWriter out = response.getWriter();
			
			out.println("");
			out.println("</body>");
			out.println("</html>");
			out.flush();
			
			response.flushBuffer();
		}
		catch(IOException ex)
		{
			log.warn("Exception encountered finishing response.", ex);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}