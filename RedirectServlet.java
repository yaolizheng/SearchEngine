import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A servlet that redirects to the results links
 * 
 * @author Yaoli Zheng
 * 
 */
@SuppressWarnings("serial")
public class RedirectServlet extends BaseServlet
{
	/**
	 * Redirects GET requests to the {@link LoginServlet}.
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		prepareResponse("redirect", response);
		String url = "";
		try  {
			Map<String, String> cookies = getCookieMap(request);
			
			String login = cookies.get("login");
			String user  = cookies.get("name");
			
			if(login != null && login.equals("true") && user != null) {
				url = request.getParameter("url");
				db.saveVisitedPage(user, url);
				response.sendRedirect(url);
			}
			else {
				response.sendRedirect(url);
			}
		}
		catch (IOException ex) {
			log.debug("Unable to redirect to /login.", ex);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	

	public void doPost(HttpServletRequest request, HttpServletResponse response)
	{
		doGet(request, response);
	}
}
